package xyz.AlastairPaterson.ChatServer.Concepts;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a chat room
 */
public class ChatRoom {
    private String roomId;

    private String ownerId;

    private final List<Identity> members = new ArrayList<>();

    /**
     * Creates a new chat room with the specified ID
     * @param roomId The ID of the room
     */
    public ChatRoom(String roomId, String ownerId) {
        this.roomId = roomId;
        this.ownerId = ownerId;
    }

    /**
     * Gets the chat room ID
     * @return The chat room ID
     */
    public String getRoomId() {
        return roomId;
    }

    /**
     * Gets the owner ID
     * @return The owner ID
     */
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * Sets the chat room ID
     * @param roomId The chat room ID
     */
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    /**
     * Gets the members of this room
     * @return A list of Identities
     */
    public List<Identity> getMembers() {
        return members;
    }
}
