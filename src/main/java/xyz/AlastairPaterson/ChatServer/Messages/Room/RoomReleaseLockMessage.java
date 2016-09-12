package xyz.AlastairPaterson.ChatServer.Messages.Room;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * Created by atp on 12/9/16.
 */
public class RoomReleaseLockMessage extends Message {
    private String serverid;

    private String roomid;

    private String approved;

    public RoomReleaseLockMessage(String serverId, String roomId, boolean approved) {
        super("releaseroomid");
        this.serverid = serverId;
        this.roomid = roomId;
        this.approved = approved ? "true" : "false";
    }

    public String getRoomId() {
        return roomid;
    }

    public String getServerId() {
        return serverid;
    }

    public boolean getApproved() {
        return this.approved.equalsIgnoreCase("true");
    }
}
