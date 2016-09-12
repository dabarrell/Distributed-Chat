package xyz.AlastairPaterson.ChatServer.Messages.Room;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * Created by atp on 12/9/16.
 */
public class RoomCreateLockMessage extends Message {
    private String serverid;

    private String roomid;

    private String locked;

    public RoomCreateLockMessage() {
        super("lockroomid");
    }

    public RoomCreateLockMessage(String roomId, String serverId) {
        this();
        this.roomid = roomId;
        this.serverid = serverId;
    }

    public String getRoomid() {
        return roomid;
    }

    public String getServerid() {
        return serverid;
    }

    public Boolean getLocked() {
        return locked.equals("true");
    }

    public void setLocked(boolean locked) {
        this.locked = locked ? "true" : "false";
    }
}
