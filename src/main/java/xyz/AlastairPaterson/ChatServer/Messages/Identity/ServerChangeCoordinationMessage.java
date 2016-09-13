package xyz.AlastairPaterson.ChatServer.Messages.Identity;

import xyz.AlastairPaterson.ChatServer.Concepts.ChatRoom;
import xyz.AlastairPaterson.ChatServer.Concepts.Identity;
import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * Created by atp on 13/9/16.
 */
public class ServerChangeCoordinationMessage extends Message {
    private String former;
    private String roomid;
    private String identity;

    public ServerChangeCoordinationMessage(ChatRoom from, ChatRoom to, Identity identity) {
        super("movejoin");
        this.former = from.getRoomId();
        this.roomid = to.getRoomId();
        this.identity = identity.getScreenName();
    }
}
