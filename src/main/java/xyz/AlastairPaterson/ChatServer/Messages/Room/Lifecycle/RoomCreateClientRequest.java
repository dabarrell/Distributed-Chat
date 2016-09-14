package xyz.AlastairPaterson.ChatServer.Messages.Room.Lifecycle;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * Created by atp on 12/9/16.
 */
public class RoomCreateClientRequest extends Message {
    private String roomid;

    private String approved;

    public RoomCreateClientRequest() {
        super("createroom");
    }

    public void setApproved(boolean approved) {
        this.approved = approved ? "true" : "false";
    }

    public boolean getApproved() {
        return this.approved.equalsIgnoreCase("true");
    }

    public String getRoomid() {
        return roomid;
    }
}
