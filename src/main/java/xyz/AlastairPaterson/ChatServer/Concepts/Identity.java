package xyz.AlastairPaterson.ChatServer.Concepts;

import com.google.gson.Gson;
import xyz.AlastairPaterson.ChatServer.Messages.Message;
import xyz.AlastairPaterson.ChatServer.Messages.Room.RoomContentsClientResponse;

import java.io.*;
import java.net.Socket;

/**
 * Represents a user's identity on this server
 */
public class Identity {
    private String screenName;
    private ChatRoom currentRoom;
    private final Socket communicationSocket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final Gson jsonSerializer = new Gson();

    /**
     * Creates a new identity for a user
     * @param screenName The user's screen name
     * @param currentRoom The current room of the user
     */
    public Identity(String screenName, ChatRoom currentRoom, Socket communicationSocket) throws IOException {
        this.screenName = screenName;
        this.currentRoom = currentRoom;
        this.communicationSocket = communicationSocket;
        this.inputStream = communicationSocket.getInputStream();
        this.outputStream = communicationSocket.getOutputStream();

        Thread communicationThread = new Thread(this::communicate);
        communicationThread.setName(this.screenName + "Communications");
        communicationThread.start();
    }

    /**
     * Get the user's screen name
     * @return The user's screen name
     */
    public String getScreenName() {
        return screenName;
    }

    /**
     * Set the user's screen name
     * @param screenName The user's desired screen name
     */
    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    /**
     * Gets the user's current room
     * @return The current room of the user
     */
    public ChatRoom getCurrentRoom() {
        return currentRoom;
    }

    /**
     * Sets the user's current room
     * @param currentRoom The current room of the user
     */
    public void setCurrentRoom(ChatRoom currentRoom) {
        this.currentRoom = currentRoom;
    }

    private void communicate() {
        while(this.communicationSocket.isConnected()) {
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
