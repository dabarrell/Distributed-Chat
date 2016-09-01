package xyz.AlastairPaterson.ChatServer.Concepts;

/**
 * Represents a user's identity on this server
 */
public class Identity {
    private String screenName;
    private ChatRoom currentRoom;

    /**
     * Creates a new identity for a user
     * @param screenName The user's screen name
     * @param currentRoom The current room of the user
     */
    public Identity(String screenName, ChatRoom currentRoom) {
        this.screenName = screenName;
        this.currentRoom = currentRoom;
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
}
