package xyz.AlastairPaterson.ChatServer.Messages.Identity;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * Created by atp on 13/9/16.
 */
public class ServerChangeClientMessage extends Message {
    public final String approved = "true";
    public String serverid;

    public ServerChangeClientMessage(String serverId) {
        super("serverchange");
        this.serverid = serverId;
    }
}
