package xyz.AlastairPaterson.ChatServer.Messages.NewServer;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * Request from a new server to be added to system
 */
public class NewServerRequestMessage extends Message {
    private String serverId;
    private String host;
    private int coordPort;
    private int clientPort;
    private int heartbeatPort;
    private int userAdditionPort;

    /**
     * Creates a new message
     * @param host new server's host
     * @param coordPort new server's coordination port
     * @param serverId new server's server Id
     * @param clientPort new server's client port
     * @param heartbeatPort new server's heartbeat port
     * @param userAdditionPort new server's user addition port
     */
    public NewServerRequestMessage(String serverId, String host, int coordPort, int clientPort, int heartbeatPort, int userAdditionPort) {
        super("newServerRequest");
        this.serverId = serverId;
        this.host = host;
        this.coordPort = coordPort;
        this.clientPort = clientPort;
        this.heartbeatPort = heartbeatPort;
        this.userAdditionPort = userAdditionPort;
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

    /**
     * Gets clientPort
     * @return the clientPort of the new server
     */
    public int getClientPort() {
        return clientPort;
    }

    /**
     * Gets heartbeatPort
     * @return the heartbeat port of the new server
     */
    public int getHeartbeatPort() {
        return heartbeatPort;
    }

    /**
     * Gets userAdditionPort
     * @return the user addition port of the new server
     */
    public int getUserAdditionPort() {
        return userAdditionPort;
    }
}
