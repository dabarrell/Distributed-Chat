package xyz.AlastairPaterson.ChatServer.Servers;

import com.google.gson.Gson;
import org.pmw.tinylog.Logger;
import xyz.AlastairPaterson.ChatServer.Concepts.ChatRoom;
import xyz.AlastairPaterson.ChatServer.Concepts.Identity;
import xyz.AlastairPaterson.ChatServer.Exceptions.RemoteChatRoomException;
import xyz.AlastairPaterson.ChatServer.Messages.Identity.ServerChangeCoordinationMessage;
import xyz.AlastairPaterson.ChatServer.Messages.Message;
import xyz.AlastairPaterson.ChatServer.Messages.MessageMessage;
import xyz.AlastairPaterson.ChatServer.Messages.Room.*;
import xyz.AlastairPaterson.ChatServer.Messages.Room.Lifecycle.RoomCreateClientRequest;
import xyz.AlastairPaterson.ChatServer.Messages.Room.Lifecycle.RoomCreateLockMessage;
import xyz.AlastairPaterson.ChatServer.Messages.Room.Lifecycle.RoomReleaseLockMessage;
import xyz.AlastairPaterson.ChatServer.Messages.Room.Membership.RoomChangeClientRequest;
import xyz.AlastairPaterson.ChatServer.Messages.Room.Membership.RoomChangeRouteResponse;
import xyz.AlastairPaterson.ChatServer.StateManager;

import java.io.*;
import java.net.Socket;

/**
 * Created by atp on 13/9/16.
 */
public class ClientConnection {
    private final Identity identity;

    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    private Thread communicationThread;

    private final Gson jsonSerializer = new Gson();


    public ClientConnection(Socket socket, Identity identity) throws IOException {
        this.identity = identity;
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        communicationThread = new Thread(this::communicate);
        communicationThread.setName(this.identity.getScreenName() + "Communications");
        communicationThread.start();
    }

    /**
     * Runs client communications
     */
    private void communicate() {
        boolean shouldRun = true;
        try {
            while (shouldRun && this.socket.isConnected() && !this.communicationThread.isInterrupted()) {
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
                        this.processSwitchRoom(jsonSerializer.fromJson(inputString, RoomChangeClientRequest.class));
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

    private void processSwitchRoom(RoomChangeClientRequest roomChangeClientRequest) throws IOException {

        ChatRoom destinationRoom = StateManager.getInstance().getRoom(roomChangeClientRequest.getRoomId());

        try {
            destinationRoom.join(this.identity);
        } catch (RemoteChatRoomException e) {
            destinationRoom.getOwnerServer().sendMessage(new ServerChangeCoordinationMessage(this.identity.getCurrentRoom(), destinationRoom, this.identity));
            this.sendMessage(new RoomChangeRouteResponse(destinationRoom));
            this.identity.getCurrentRoom().leave(this.identity, destinationRoom);
        }
    }

    /**
     * Handles the creation of a new room
     *
     * @param roomCreateClientRequest The client room creation message
     * @throws IOException If sub-requests fail, IO exception is thrown
     */
    private void processCreateRoom(RoomCreateClientRequest roomCreateClientRequest) throws IOException {
        if (this.identity.getOwnedRoom() != null) {
            // Check user doesn't own another room
            roomCreateClientRequest.setApproved(false);
        }

        // My regex skills are lacking
        // Validate room name conventions
        else if (roomCreateClientRequest.getRoomid().matches("\\A[^A-Za-z]")
                || roomCreateClientRequest.getRoomid().length() < 3
                || roomCreateClientRequest.getRoomid().length() > 16) {
            roomCreateClientRequest.setApproved(false);
        }
        else {
            // Start asking servers if we're all G
            RoomCreateLockMessage lockRequest = new RoomCreateLockMessage(roomCreateClientRequest.getRoomid(),
                    StateManager.getInstance().getThisServerId());

            boolean allServersApprove = true;

            // Ask servers if the name is available
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
            // RemoteChatRoomException can't occur if we're creating on this server
            try {
                ChatRoom newRoom = StateManager.getInstance().getRoom(roomCreateClientRequest.getRoomid());
                newRoom.setOwner(this.identity);
                newRoom.join(this.identity);
                this.identity.setOwnedRoom(newRoom);
            } catch (RemoteChatRoomException ignored) { }
        }
    }

    /**
     * Processes the 'list chat rooms' request
     */
    private void processList() {
        try {
            this.sendMessage(new ListClientResponse());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Processes a client's disconnection
     */
    private void processQuit() {
        try {
            this.identity.getCurrentRoom().leave(this.identity);

            StateManager.getInstance().getHostedIdentities().remove(this.identity);
            //TODO: remove rooms that are owned by this identity
        } catch (IOException e) {
            Logger.error("IO exception occurred during cleanup - state may be invalid!");
        }

        try {
            this.sendMessage(null);
            communicationThread.interrupt();
            this.socket.shutdownInput();
            this.socket.shutdownOutput();
            this.socket.close();
        } catch (IOException e) {
            Logger.warn("IO exception occurred during client disconnect");
        }

    }

    /**
     * Processes a request to send a chat message
     *
     * @param messageMessage The message being broadcast
     * @throws IOException If sending causes IO exception, this is re-thrown
     */
    private void processMessage(MessageMessage messageMessage) throws IOException {
        messageMessage.setIdentity(this.identity.getScreenName());

        this.identity.getCurrentRoom().broadcast(messageMessage, this.identity);
    }

    /**
     * Sends a data message to a client
     *
     * @param message The data message being sent
     * @throws IOException If an error occurs, throws IO exception
     */
    public void sendMessage(Object message) throws IOException {
        BufferedWriter streamWriter = new BufferedWriter(new OutputStreamWriter(this.outputStream));
        streamWriter.write(jsonSerializer.toJson(message));
        streamWriter.write('\n');
        streamWriter.flush();
    }

    /**
     * Processes a 'who' request
     *
     * @return The list of people in the current room
     */
    private RoomContentsClientResponse processWho() {
        return new RoomContentsClientResponse(this.identity.getCurrentRoom());
    }
}
