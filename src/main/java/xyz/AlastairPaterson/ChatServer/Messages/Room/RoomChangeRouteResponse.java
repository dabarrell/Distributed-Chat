package xyz.AlastairPaterson.ChatServer.Messages.Room;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * Created by atp on 12/9/16.
 */
public class RoomChangeRouteResponse extends Message {
    private String roomid;
    private String host;
    private String port;

    public RoomChangeRouteResponse (String roomId, String host, int port) {
        super("route");
        this.roomid = roomId;
        this.host = host;
        this.port = Integer.toString(port);
    }
}
