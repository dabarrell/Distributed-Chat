package xyz.AlastairPaterson.ChatServer.Messages.NewServer;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * Request from a new server to be added to system
 */
public class GlobalReleaseMessage extends Message {
    private String serverId;

    /**
     * Creates a new message
     * @param serverId
     */
    public GlobalReleaseMessage(String serverId) {
        super("globalrelease");
        this.serverId = serverId;
    }

    /**
     * Gets serverId
     * @return the id of the sending server
     */
    public String getServerId() {
        return serverId;
    }

}
