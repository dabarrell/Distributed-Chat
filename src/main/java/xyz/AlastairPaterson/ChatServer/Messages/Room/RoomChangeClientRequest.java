package xyz.AlastairPaterson.ChatServer.Messages.Room;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * Created by atp on 12/9/16.
 */
public class RoomChangeClientRequest extends Message {
    public String roomid;

    public RoomChangeClientRequest() {
        super("join");
    }

    /**
     * Gets the requested room ID
     *
     * @return The ID of the requested room
     */
    public String getRoomId() {
        return roomid;
    }
}
