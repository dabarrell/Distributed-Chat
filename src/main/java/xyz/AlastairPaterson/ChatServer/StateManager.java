package xyz.AlastairPaterson.ChatServer;

import xyz.AlastairPaterson.ChatServer.Concepts.ChatRoom;
import xyz.AlastairPaterson.ChatServer.Concepts.Identity;
import xyz.AlastairPaterson.ChatServer.Exceptions.IdentityInUseException;
import xyz.AlastairPaterson.ChatServer.Servers.CoordinationServer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by atp on 30/08/2016.
 */
public class StateManager {
    private static StateManager ourInstance = new StateManager();

    public static StateManager getInstance() {
        return ourInstance;
    }

    private final List<ChatRoom> hostedRooms = new ArrayList<>();

    private final List<Identity> hostedIdentities = new LinkedList<>();

    private final List<CoordinationServer> servers = new LinkedList();

    private String thisServerId;

    private StateManager() { }

    /* Adders */

    public void addServer(CoordinationServer server) {
        this.servers.add(server);
    }

    /* Getters */

    public List<ChatRoom> getHostedRooms() {
        return hostedRooms;
    }

    public List<Identity> getHostedIdentities() {
        return hostedIdentities;
    }

    public List<CoordinationServer> getServers() {
        return servers;
    }

    public String getThisServerId() {
        return thisServerId;
    }

    /* Setters */

    public void setThisServerId(String thisServerId) {
        this.thisServerId = thisServerId;
    }

    /* Validators */

    public void validateIdentityOk(String name) throws IdentityInUseException {
        if (hostedIdentities.stream().anyMatch(x -> x.getScreenName().equalsIgnoreCase(name))) {
            throw new IdentityInUseException(name);
        }
    }

    public void validateRoomOk(String name) throws IdentityInUseException {
        if(hostedRooms.stream().anyMatch(x -> x.getRoomId().equalsIgnoreCase(name))) {
            throw new IdentityInUseException(name);
        }
    }
}
