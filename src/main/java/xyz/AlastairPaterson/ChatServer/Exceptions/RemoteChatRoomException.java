package xyz.AlastairPaterson.ChatServer.Exceptions;

import xyz.AlastairPaterson.ChatServer.Concepts.ChatRoom;

/**
 * Created by atp on 13/9/16.
 */
public class RemoteChatRoomException extends Exception {
    private ChatRoom target;

    public RemoteChatRoomException(ChatRoom target) {
        super("The chat room requested is hosted on a different server");
        this.target = target;
    }

    public ChatRoom getTargetRoom() {
        return target;
    }
}
