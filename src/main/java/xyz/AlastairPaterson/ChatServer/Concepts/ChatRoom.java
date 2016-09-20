package xyz.AlastairPaterson.ChatServer.Concepts;

import xyz.AlastairPaterson.ChatServer.Exceptions.IdentityOwnsRoomException;
import xyz.AlastairPaterson.ChatServer.Exceptions.RemoteChatRoomException;
import xyz.AlastairPaterson.ChatServer.Messages.Message;
import xyz.AlastairPaterson.ChatServer.Messages.Room.Lifecycle.RoomDelete;
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

    public ChatRoom(String roomId, CoordinationServer ownerServer) {
        this.roomId = roomId;
        this.ownerServer = ownerServer;
        this.ownerId = "";
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
     * Sets the owner ID
     *
     * @param ownerId The owner ID
     */
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
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

    /**
     * Controls joining a room and leaving the previous room, if applicable
     *
     * @param identity The identity joining this room
     * @throws RemoteChatRoomException If the server is remote, should be handled differently
     * @throws IOException If IO exception occurs
     * @throws IdentityOwnsRoomException If user already owns a room, can't join another one
     */
    public void join(Identity identity) throws RemoteChatRoomException, IOException, IdentityOwnsRoomException {
        if (this.getOwnerId().equalsIgnoreCase(identity.getScreenName())) {
            throw new IdentityOwnsRoomException();
        }

        if (identity.getCurrentRoom() != null) {
            identity.getCurrentRoom().leave(identity, this);
        }

        if (this.isForeignRoom()) {
            throw new RemoteChatRoomException(this);
        }

        this.getMembers().add(identity);

        RoomChangeClientResponse clientMessage = new RoomChangeClientResponse(identity, identity.getCurrentRoom(), this);

        this.broadcast(clientMessage);

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
            RoomChangeClientResponse roomChange = new RoomChangeClientResponse(identity, this, destination);

            this.broadcast(roomChange);
        }
    }

    /**
     * Sends a message to all members of the group
     *
     * @param message The message being sent
     * @throws IOException If an IO exception occurs
     */
    public void broadcast(Message message) throws IOException {
        this.broadcast(message, null);
    }

    /**
     * Sends a message to all members of the group except the member specified
     *
     * @param message The message to be sent
     * @param ignore The identity not to send the message to
     * @throws IOException If an IO exception occurs
     */
    public void broadcast(Message message, Identity ignore) throws IOException {
        for (Identity member : this.members) {
            if (!member.equals(ignore)) {
                member.sendMessage(message);
            }
        }
    }

    /**
     * Destroys a chat room
     *
     * @throws IOException If an IO exception occurs
     */
    public void destroy() throws IOException {
        for (Identity identity : this.getMembers()) {
            try {
                StateManager.getInstance().getMainhall().join(identity);
            } catch (RemoteChatRoomException | IdentityOwnsRoomException ignore) { }
        }

        RoomDelete deleteMessage = new RoomDelete();
        deleteMessage.setServerId(StateManager.getInstance().getThisServerId());
        deleteMessage.setRoomId(this.getRoomId());

        for (CoordinationServer coordinationServer : StateManager.getInstance().getServers()) {
            coordinationServer.sendMessage(deleteMessage);
        }
    }
}
