package xyz.AlastairPaterson.ChatServer.Messages.Room;

import xyz.AlastairPaterson.ChatServer.Concepts.ChatRoom;
import xyz.AlastairPaterson.ChatServer.Messages.Message;
import xyz.AlastairPaterson.ChatServer.Servers.CoordinationServer;

/**
 * Created by atp on 12/9/16.
 */
public class RoomChangeRouteResponse extends Message {
    private String roomid;
    private String host;
    private String port;

    public RoomChangeRouteResponse(ChatRoom room) {
        super("route");
        this.roomid = room.getRoomId();
        this.host = room.getOwnerServer().getHostname();
        this.port = String.valueOf(room.getOwnerServer().getClientPort());
    }
}
