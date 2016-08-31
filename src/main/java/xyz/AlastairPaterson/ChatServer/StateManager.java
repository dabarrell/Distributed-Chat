package xyz.AlastairPaterson.ChatServer;

import xyz.AlastairPaterson.ChatServer.Concepts.ChatRoom;
import xyz.AlastairPaterson.ChatServer.Concepts.Identity;
import xyz.AlastairPaterson.ChatServer.Exceptions.IdentityInUseException;
import xyz.AlastairPaterson.ChatServer.Servers.CoordinationServer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Manages the coordination server's state
 */
public class StateManager {
    private static StateManager ourInstance = new StateManager();

    /**
     * Gets the StateManager singleton
     * @return The global StateManager instance
     */
    public static StateManager getInstance() {
        return ourInstance;
    }

    private final List<ChatRoom> hostedRooms = new ArrayList<>();

    private final List<Identity> hostedIdentities = new LinkedList<>();

    private final List<CoordinationServer> servers = new LinkedList<>();

    private String thisServerId;

    private StateManager() { }

    /* Adders */

    /**
     * Adds a coordination server to the configuration
     * @param server The new server being added
     */
    public void addServer(CoordinationServer server) {
        this.servers.add(server);
    }

    /* Getters */

    /**
     * Gets the rooms hosted on this coordination server
     * @return A list of ChatRoom objects
     */
    public List<ChatRoom> getHostedRooms() {
        return hostedRooms;
    }

    /**
     * Gets the identities hosted on this coordination server
     * @return A list of Identity objects
     */
    public List<Identity> getHostedIdentities() {
        return hostedIdentities;
    }

    /**
     * Gets the coordination servers known by this server
     * @return A list of CoordinationServer objects
     */
    public List<CoordinationServer> getServers() {
        return servers;
    }

    /**
     * Gets the name of the local coordination server
     * @return The local coordination server name
     */
    public String getThisServerId() {
        return thisServerId;
    }

    /* Setters */

    /**
     * Sets the local coordination server ID
     * @param thisServerId The local coordination server ID
     */
    public void setThisServerId(String thisServerId) {
        this.thisServerId = thisServerId;
    }

    /* Validators */

    /**
     * Validates a proposed identity is OK
     * @param name The proposed client identity
     * @throws IdentityInUseException if the identity is in use on this server already
     */
    public void validateIdentityOk(String name) throws IdentityInUseException {
        if (hostedIdentities.stream().anyMatch(x -> x.getScreenName().equalsIgnoreCase(name))) {
            throw new IdentityInUseException(name);
        }
    }

    /**
     * Validates a proposed room name is OK
     * @param name The proposed room name
     * @throws IdentityInUseException If the room name is in use on this server already
     */
    public void validateRoomOk(String name) throws IdentityInUseException {
        if(hostedRooms.stream().anyMatch(x -> x.getRoomId().equalsIgnoreCase(name))) {
            throw new IdentityInUseException(name);
        }
    }
}
