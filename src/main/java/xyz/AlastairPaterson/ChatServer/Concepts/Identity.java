package xyz.AlastairPaterson.ChatServer.Concepts;

import xyz.AlastairPaterson.ChatServer.Servers.ClientConnection;
import java.io.IOException;

/**
 * Represents a user's identity on this server
 */
public class Identity {
    private String screenName;
    private ChatRoom currentRoom;
    private ChatRoom ownedRoom;
    private ClientConnection connection;

    /**
     * Creates a new identity for a user
     *
     * @param screenName  The user's screen name
     * @param currentRoom The current room of the user
     */
    public Identity(String screenName, ChatRoom currentRoom) throws IOException {
        this.screenName = screenName;
        this.currentRoom = currentRoom;
    }

    /**
     * Get the user's screen name
     *
     * @return The user's screen name
     */
    public String getScreenName() {
        return screenName;
    }

    public void setConnection(ClientConnection connection) {
        this.connection = connection;
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
    void setCurrentRoom(ChatRoom currentRoom) {
        this.currentRoom = currentRoom;
    }

    public ChatRoom getOwnedRoom() {
        return ownedRoom;
    }

    public void setOwnedRoom(ChatRoom ownedRoom) {
        this.ownedRoom = ownedRoom;
    }

    public void sendMessage(Object message) throws IOException {
        this.connection.sendMessage(message);
    }
}
