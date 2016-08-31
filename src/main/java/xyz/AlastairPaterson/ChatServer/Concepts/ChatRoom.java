package xyz.AlastairPaterson.ChatServer.Concepts;

/**
 * Represents a chat room
 */
public class ChatRoom {
    private String roomId;

    /**
     * Creates a new chat room with the specified ID
     * @param roomId
     */
    public ChatRoom(String roomId) {
        this.roomId = roomId;
    }

    /**
     * Gets the chat room ID
     * @return The chat room ID
     */
    public String getRoomId() {
        return roomId;
    }

    /**
     * Sets the chat room ID
     * @param roomId The chat room ID
     */
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
