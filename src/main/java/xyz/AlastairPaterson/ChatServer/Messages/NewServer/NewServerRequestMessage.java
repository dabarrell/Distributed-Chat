package xyz.AlastairPaterson.ChatServer.Messages.NewServer;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * Request from a new server to be added to system
 */
public class NewServerRequestMessage extends Message {
    private String serverId;
    private String host;
    private int coordPort;

    /**
     * Creates a new message
     * @param host
     * @param coordPort
     * @param serverId
     */
    public NewServerRequestMessage(String serverId, String host, int coordPort) {
        super("newServerRequest");
        this.serverId = serverId;
        this.host = host;
        this.coordPort = coordPort;
    }

    /**
     * Gets serverId
     * @return the id of the new server
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * Gets host
     * @return the host of the new server
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets coordPort
     * @return the coordPort of the new server
     */
    public int getCoordPort() {
        return coordPort;
    }
}
