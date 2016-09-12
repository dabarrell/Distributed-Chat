package xyz.AlastairPaterson.ChatServer.Concepts;

import com.google.gson.Gson;
import org.pmw.tinylog.Logger;
import xyz.AlastairPaterson.ChatServer.Messages.Message;
import xyz.AlastairPaterson.ChatServer.Messages.MessageMessage;
import xyz.AlastairPaterson.ChatServer.Messages.Room.*;
import xyz.AlastairPaterson.ChatServer.Servers.CoordinationServer;
import xyz.AlastairPaterson.ChatServer.StateManager;

import java.io.*;
import java.net.Socket;

/**
 * Represents a user's identity on this server
 */
public class Identity {
    private String screenName;
    private ChatRoom currentRoom;
    private ChatRoom ownedRoom;
    private final Socket communicationSocket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final Gson jsonSerializer = new Gson();
    private Thread communicationThread;

    /**
     * Creates a new identity for a user
     *
     * @param screenName  The user's screen name
     * @param currentRoom The current room of the user
     */
    public Identity(String screenName, ChatRoom currentRoom, Socket communicationSocket) throws IOException {
        this.screenName = screenName;
        this.currentRoom = currentRoom;
        this.communicationSocket = communicationSocket;
        this.inputStream = communicationSocket.getInputStream();
        this.outputStream = communicationSocket.getOutputStream();

        communicationThread = new Thread(this::communicate);
        communicationThread.setName(this.screenName + "Communications");
        communicationThread.start();
    }

    /**
     * Get the user's screen name
     *
     * @return The user's screen name
     */
    public String getScreenName() {
        return screenName;
    }

    /**
     * Set the user's screen name
     *
     * @param screenName The user's desired screen name
     */
    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    /**
     * Gets the user's current room
     *
     * @return The current room of the user
     */
    public ChatRoom getCurrentRoom() {
        return currentRoom;
    }

    /**
     * Sets the user's current room
     *
     * @param currentRoom The current room of the user
     */
    public void setCurrentRoom(ChatRoom currentRoom) {
        this.currentRoom = currentRoom;
    }

    private void communicate() {
        boolean shouldRun = true;
        try {
            while (shouldRun && this.communicationSocket.isConnected() && !this.communicationThread.isInterrupted()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(this.inputStream));

                String inputString = reader.readLine();
                if (inputString == null) break;

                String messageType = jsonSerializer.fromJson(inputString, Message.class).getType();
                switch (messageType) {
                    case "who":
                        this.sendMessage(processWho());
                        break;
                    case "message":
                        this.processMessage(jsonSerializer.fromJson(inputString, MessageMessage.class));
                        break;
                    case "quit":
                        shouldRun = false;
                        break;
                    case "list":
                        this.processList();
                        break;
                    case "createroom":
                        this.processCreateRoom(jsonSerializer.fromJson(inputString, RoomCreateClientRequest.class));
                        break;
                    case "join":
                        // TODO: implement
                        break;
                    case "deleteroom":
                        // TODO: implement
                        break;
                }
            }
        } catch (IOException e) {
            Logger.error("IOException occurred during client communication - terminating client");
        }
        finally {
            this.processQuit();
        }
    }

    private void processCreateRoom(RoomCreateClientRequest roomCreateClientRequest) throws IOException {
        if (this.ownedRoom != null) {
            // Check user doesn't own another room
            roomCreateClientRequest.setApproved(false);
        }
        else if (roomCreateClientRequest.getRoomid().matches("\\A[^A-Za-z]")
                || roomCreateClientRequest.getRoomid().length() < 3
                || roomCreateClientRequest.getRoomid().length() > 16) {
            // Validate room name conventions
            roomCreateClientRequest.setApproved(false);
        }
        else {
            // Start asking servers if we're all G
            RoomCreateLockMessage lockRequest = new RoomCreateLockMessage(roomCreateClientRequest.getRoomid(),
                    StateManager.getInstance().getThisServerId());

            boolean allServersApprove = true;

            for(CoordinationServer i : StateManager.getInstance().getServers()) {
                RoomCreateLockMessage response = jsonSerializer.fromJson(i.sendMessage(lockRequest), RoomCreateLockMessage.class);
                if (!response.getLocked()) {
                    allServersApprove = false;
                    break;
                }
            }
            roomCreateClientRequest.setApproved(allServersApprove);

            RoomReleaseLockMessage roomUnlockMessage = new RoomReleaseLockMessage(StateManager.getInstance().getThisServerId(),
                    roomCreateClientRequest.getRoomid(),
                    allServersApprove);

            for(CoordinationServer i : StateManager.getInstance().getServers()) {
                i.sendMessage(roomUnlockMessage);
            }
        }

        this.sendMessage(roomCreateClientRequest);

        if (roomCreateClientRequest.getApproved()) {
            this.changeRoom(StateManager.getInstance().getRoom(roomCreateClientRequest.getRoomid()));
        }
    }

    private void changeRoom(ChatRoom to) throws IOException {
        RoomChangeClientResponse roomChange = new RoomChangeClientResponse(this.getScreenName(),
                this.currentRoom.getRoomId(),
                to.getRoomId());

        for (Identity identity : this.currentRoom.getMembers()) {
            identity.sendMessage(roomChange);
        }

        this.currentRoom.getMembers().remove(this);
        to.getMembers().add(this);

        for (Identity identity: this.currentRoom.getMembers()) {
            identity.sendMessage(roomChange);
        }
    }

    private void processList() {
        try {
            this.sendMessage(new ListClientResponse());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processQuit() {
        try {
            this.sendMessage(null);
            communicationThread.interrupt();
            this.communicationSocket.shutdownInput();
            this.communicationSocket.shutdownOutput();
            this.communicationSocket.close();
        } catch (IOException e) {
            Logger.warn("IO exception occurred during client disconnect");
        }

        try {
            for (Identity i : this.getCurrentRoom().getMembers()) {
                if (i.equals(this)) continue;

                i.sendMessage(new RoomChangeClientResponse(this.getScreenName(), this.getCurrentRoom().getRoomId(), ""));
            }

            this.getCurrentRoom().getMembers().remove(this);
            StateManager.getInstance().getHostedIdentities().remove(this);
            //TODO: remove rooms that are owned by this identity
        } catch (IOException e) {
            Logger.error("IO exception occurred during cleanup - state may be invalid!");
        }

    }

    private void processMessage(MessageMessage messageMessage) {
        messageMessage.setIdentity(this.getScreenName());
        this.getCurrentRoom().getMembers().forEach(x -> {
            try {
                if (!x.equals(this)) {
                    x.sendMessage(messageMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendMessage(Object message) throws IOException {
        BufferedWriter streamWriter = new BufferedWriter(new OutputStreamWriter(this.outputStream));
        streamWriter.write(jsonSerializer.toJson(message));
        streamWriter.write('\n');
        streamWriter.flush();
    }

    private Object processWho() {
        return new RoomContentsClientResponse(this.getCurrentRoom());
    }
}
