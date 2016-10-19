package xyz.AlastairPaterson.ChatServer.Messages.NewServer;

import xyz.AlastairPaterson.ChatServer.Messages.Message;
import xyz.AlastairPaterson.ChatServer.Servers.CoordinationServer;

/**
 * Represents global lock message
 */
public class GlobalLockMessage extends Message {
    private String serverId;
    private String newServerId;
    private String host;
    private int coordPort;
    private int clientPort;
    private int heartbeatPort;
    private int userAdditionPort;
    private String approved;

    /**
     * Creates a new coordination request message
     * @param serverId The server requesting coordination
     * @param newServer The new coordination server
     */
    public GlobalLockMessage(String serverId, CoordinationServer newServer) {
        super("globallock");
        this.serverId = serverId;
        this.newServerId = newServer.getId();
        this.host = newServer.getHostname();
        this.coordPort = newServer.getCoordinationPort();
        this.clientPort = newServer.getClientPort();
        this.heartbeatPort = newServer.getHeartbeatPort();
        this.userAdditionPort = newServer.getUserAdditionPort();
    }

    /**
     * Creates a new coordination request message
     * @param serverId The server requesting coordination
     * @param newServerId The new serverId
     * @param host The new host
     * @param coordPort The new coordination port
     * @param clientPort The new client port
     * @param heartbeatPort The new client heartbeat port
     * @param userAdditionPort The new client user addition port
     */
    public GlobalLockMessage(String serverId, String newServerId, String host, int coordPort, int clientPort, int heartbeatPort, int userAdditionPort) {
        super("globallock");
        this.serverId = serverId;
        this.newServerId = newServerId;
        this.host = host;
        this.coordPort = coordPort;
        this.clientPort = clientPort;
        this.heartbeatPort = heartbeatPort;
        this.userAdditionPort = userAdditionPort;
    }

    /**
     * Creates a new coordination reply message
     * @param serverId The server requesting coordination
     * @param newServerId The new serverId
     * @param host The new host
     * @param coordPort The new coordination port
     * @param clientPort The new client port
     * @param heartbeatPort The new client heartbeat port
     * @param userAdditionPort The new client user addition port
     * @param isApproved True if identity approved, false otherwise
     */
    public GlobalLockMessage(String serverId, String newServerId, String host, int coordPort, int clientPort, int heartbeatPort, int userAdditionPort, boolean isApproved) {
        super("globallock");
        this.serverId = serverId;
        this.newServerId = newServerId;
        this.host = host;
        this.coordPort = coordPort;
        this.clientPort = clientPort;
        this.heartbeatPort = heartbeatPort;
        this.userAdditionPort = userAdditionPort;
        this.approved = isApproved ? "true" : "false";
    }

    /**
     * Creates a new empty coordination request for JSON serialization
     */
    public GlobalLockMessage() {
        super("globallock");
    }

    /**
     * Whether or not the request is approved
     * @return True if request approved, false otherwise
     */
    public boolean isApproved() {
        return approved.equalsIgnoreCase("true");
    }

    /**
     * Gets serverId
     * @return the id of the requesting server
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
     * Gets newServerId
     * @return the serverId of the new server
     */
    public String getNewServerId() {
        return newServerId;
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

    /**
     * Sets approved
     * @param approved True if request approved, false otherwise
     */
    public void setApproved(boolean approved) {
        this.approved = approved ? "true" : "false";
    }
}
