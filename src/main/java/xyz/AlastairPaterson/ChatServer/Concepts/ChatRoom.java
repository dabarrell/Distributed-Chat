package xyz.AlastairPaterson.ChatServer.Concepts;

import xyz.AlastairPaterson.ChatServer.Servers.CoordinationServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a chat room
 */
public class ChatRoom {
    private final String roomId;

    private final String ownerId;

    private final CoordinationServer ownerServer;

    private final List<Identity> members = new ArrayList<>();

    /**
     * Creates a new chat room with the specified ID
     * @param roomId The ID of the room
     */
    public ChatRoom(String roomId, String ownerId, CoordinationServer ownerServer) {
        this.roomId = roomId;
        this.ownerId = ownerId;
        this.ownerServer = ownerServer;
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
     * Gets the owning coordination server
     * @return The owning coordination server
     */
    public CoordinationServer getOwnerServer() {
        return ownerServer;
    }

    /**
     * Gets the members of this room
     * @return A list of Identities
     */
    public List<Identity> getMembers() {
        return members;
    }
}
