package xyz.AlastairPaterson.ChatServer.Messages.Room;

import xyz.AlastairPaterson.ChatServer.Concepts.ChatRoom;
import xyz.AlastairPaterson.ChatServer.Messages.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by atp on 1/09/2016.
 */
public class RoomContentsClientResponse extends Message{
    private final String roomid;

    private final String owner;

    private final List<String> identities = new ArrayList<>();

    public RoomContentsClientResponse(ChatRoom target) {
        super("roomcontents");
        this.roomid = target.getRoomId();
        this.owner = target.getOwnerId();
        target.getMembers().forEach(x -> identities.add(x.getScreenName()));
    }
}
