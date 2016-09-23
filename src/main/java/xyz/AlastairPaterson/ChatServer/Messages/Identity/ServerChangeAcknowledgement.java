package xyz.AlastairPaterson.ChatServer.Messages.Identity;

import xyz.AlastairPaterson.ChatServer.Messages.Message;
import xyz.AlastairPaterson.ChatServer.StateManager;

/**
 * Created by atp on 23/9/16.
 */
public class ServerChangeAcknowledgement extends Message {
    private final String approved = "true";
    private final String serverid;

    public ServerChangeAcknowledgement() {
        super("serverchange");
        serverid = StateManager.getInstance().getThisServerId();
    }
}
