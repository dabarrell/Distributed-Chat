package xyz.AlastairPaterson.ChatServer.Concepts;

import org.pmw.tinylog.Logger;
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

    private Identity owner;

    private final CoordinationServer ownerServer;

    private final List<Identity> members = new ArrayList<>();

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
     * Sets the owner
     *
     * @param owner The owner
     */
    public void setOwner(Identity owner) {
        this.ownerId = owner.getScreenName();
        this.owner = owner;
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
    public void join(Identity identity) throws Exception {

        if (identity.getCurrentRoom() != null) {
            if (identity.getCurrentRoom().getOwnerId().equalsIgnoreCase(identity.getScreenName())) {
                throw new IdentityOwnsRoomException();
            }

            identity.getCurrentRoom().leave(identity, this);
        }

        if (this.isForeignRoom()) {
            throw new RemoteChatRoomException(this);
        }

        this.members.add(identity);

        RoomChangeClientResponse clientMessage = new RoomChangeClientResponse(identity, identity.getCurrentRoom(), this);

        this.broadcast(clientMessage);

        identity.setCurrentRoom(this);
    }

    public void leave(Identity identity) throws Exception {
        this.leave(identity, null);
    }

    public void leave(Identity identity, boolean forceful) {
        if (forceful) {
            try {
                this.leave(identity, null);
            } catch (Exception e) {
                Logger.debug("Ignoring exception in client leave");
            }
        }
    }

    public void leave(Identity identity, ChatRoom destination) throws Exception {

        if (this.getOwnerId().equalsIgnoreCase(identity.getScreenName())) {
            this.destroy();
        }
        else {
            RoomChangeClientResponse roomChange = new RoomChangeClientResponse(identity, this, destination);

            this.broadcast(roomChange);
        }

        this.getMembers().remove(identity);
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
    public void destroy() throws Exception {
        Logger.debug("Destroying room");
        owner.setOwnedRoom(null);
        this.ownerId = "";

        // Pro tip - don't modify an array in Java while iterating over it
        while (this.members.size() != 0) {
            try {
                StateManager.getInstance().getMainhall().join(this.members.get(0));
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
