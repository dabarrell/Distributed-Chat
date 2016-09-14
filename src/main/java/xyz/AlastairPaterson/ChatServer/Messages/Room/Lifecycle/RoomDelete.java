package xyz.AlastairPaterson.ChatServer.Messages.Room.Lifecycle;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * Created by atp on 14/9/16.
 */
public class RoomDelete extends Message {
    private String roomid;

    private String approved;

    private String serverid;

    public RoomDelete() {
        super("deleteroom");
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

    public void setRoomId(String roomId) {
        this.roomid = roomId;
    }

    public void setServerId(String serverId) {
        this.serverid = serverId;
    }

    public void setApproved(boolean approved) {
        this.approved = approved ? "true" : "false";
    }
}
