package xyz.AlastairPaterson.ChatServer.Messages.NewServer;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * Request for a list of all rooms
 */
public class RequestRoomListMessage extends Message {
    private String serverId;
    private String roomId;
    private String hostServerId;

    /**
     * Creates a new message
     * @param serverId
     */
    public RequestRoomListMessage(String serverId) {
        super("requestrooms");
        this.serverId = serverId;
    }

    /**
     * Creates a new message
     * @param serverId
     * @param roomId
     * @param hostServerId
     */
    public RequestRoomListMessage(String serverId, String roomId, String hostServerId) {
        super("requestrooms");
        this.serverId = serverId;
        this.roomId = roomId;
        this.hostServerId = hostServerId;
    }

    /**
     * Gets serverId
     * @return the id of the sending server
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * Gets roomId
     * @return the id of the room
     */
    public String getRoomId() {
        return roomId;
    }

    /**
     * Gets hostServerId
     * @return the id of the room's host server
     */
    public String getHostServerId() {
        return hostServerId;
    }


}
