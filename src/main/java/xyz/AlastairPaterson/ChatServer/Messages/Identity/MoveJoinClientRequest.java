package xyz.AlastairPaterson.ChatServer.Messages.Identity;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * Created by atp on 13/9/16.
 */
public class MoveJoinClientRequest extends Message {
    private String former;
    private String roomid;
    private String identity;

    public MoveJoinClientRequest() {
        super("movejoin");
    }
}
