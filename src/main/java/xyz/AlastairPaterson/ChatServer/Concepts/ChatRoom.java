package xyz.AlastairPaterson.ChatServer.Concepts;

import xyz.AlastairPaterson.ChatServer.Exceptions.IdentityOwnsRoomException;
import xyz.AlastairPaterson.ChatServer.Exceptions.RemoteChatRoomException;
import xyz.AlastairPaterson.ChatServer.Messages.Message;
import xyz.AlastairPaterson.ChatServer.Messages.Room.Membership.RoomChangeClientResponse;
import xyz.AlastairPaterson.ChatServer.Servers.CoordinationServer;
import xyz.AlastairPaterson.ChatServer.StateManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a chat room
 */
public class ChatRoom {
    private final String roomId;

    private String ownerId;

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

    /**
     * Checks if the room is hosted on another server
     *
     * @return True if room is on a different server, false otherwise
     */
    private boolean isForeignRoom() {
        return ! this.ownerServer.getId().equals(StateManager.getInstance().getThisServerId());
    }

    public void join(Identity identity) throws RemoteChatRoomException, IOException {
        identity.getCurrentRoom().leave(identity, this);

        if (this.isForeignRoom()) {
            throw new RemoteChatRoomException(this);
        }

        this.getMembers().add(identity);
        RoomChangeClientResponse clientMessage = new RoomChangeClientResponse(identity, identity.getCurrentRoom(), this);
        this.broadcast(clientMessage);
        identity.getCurrentRoom().broadcast(clientMessage);
        identity.setCurrentRoom(this);
    }

    public void leave(Identity identity) throws IOException {
        this.leave(identity, null);
    }

    public void leave(Identity identity, ChatRoom destination) throws IOException {
        this.getMembers().remove(identity);

        if (this.getOwnerId().equalsIgnoreCase(identity.getScreenName())) {
            this.destroy();
        }
        else {
            RoomChangeClientResponse roomChange;
            if (destination == null) {
                roomChange = new RoomChangeClientResponse(identity.getScreenName(), this.getRoomId(), "");
            }
            else {
                roomChange = new RoomChangeClientResponse(identity.getScreenName(), this.getRoomId(), destination.getRoomId());
            }

            this.broadcast(roomChange);
        }
    }

    public void broadcast(Message message) throws IOException {
        this.broadcast(message, null);
    }

    public void broadcast(Message message, Identity ignore) throws IOException {
        for (Identity member : this.members) {
            if (!member.equals(ignore)) {
                member.sendMessage(message);
            }
        }
    }

    public void destroy() {
        throw new NotImplementedException();
    }

    public void setOwner(Identity owner) {
        this.ownerId = owner.getScreenName();
    }
}
