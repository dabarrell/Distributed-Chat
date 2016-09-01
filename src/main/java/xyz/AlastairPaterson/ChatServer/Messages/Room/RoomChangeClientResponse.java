package xyz.AlastairPaterson.ChatServer.Messages.Room;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * Created by atp on 1/09/2016.
 */
public class RoomChangeClientResponse extends Message {
    private String identity;

    private String former;

    private String roomid;

    public RoomChangeClientResponse(String identity, String from, String to) {
        super("roomchange");

        this.identity = identity;
        this.former = from;
        this.roomid = to;
    }
}
