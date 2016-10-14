package xyz.AlastairPaterson.ChatServer;

import xyz.AlastairPaterson.ChatServer.Concepts.ChatRoom;
import xyz.AlastairPaterson.ChatServer.Concepts.EntityLock;
import xyz.AlastairPaterson.ChatServer.Concepts.Identity;
import xyz.AlastairPaterson.ChatServer.Concepts.LockType;
import xyz.AlastairPaterson.ChatServer.Exceptions.IdentityInUseException;
import xyz.AlastairPaterson.ChatServer.Servers.CoordinationServer;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;

/**
 * Manages the coordination server's state
 */
public class StateManager {
    private static final StateManager ourInstance = new StateManager();

    /**
     * Gets the StateManager singleton
     *
     * @return The global StateManager instance
     */
    public static synchronized StateManager getInstance() {
        return ourInstance;
    }

    private final List<ChatRoom> hostedRooms = new ArrayList<>();

    private final List<Identity> hostedIdentities = new LinkedList<>();

    private final List<EntityLock> lockedEntities = new LinkedList<>();

    private final List<CoordinationServer> servers = new LinkedList<>();

    private final HashMap<String, String> registeredUsers = new HashMap<>();

    private ChatRoom mainHall;

    private String thisServerId;

    private StateManager() { }

    /* Adders */

    /**
     * Adds a coordination server to the configuration
     *
     * @param server The new server being added
     */
    void addServer(CoordinationServer server) {
        this.servers.add(server);
    }

    /**
     * Adds a lock to the StateManager
     *
     * @param identity The identity being locked
     * @param type     The type of entity being locked
     */
    public void addLock(String identity, String serverName, LockType type) {
        this.lockedEntities.add(new EntityLock(identity, serverName, type));
    }

    /* Removers */

    /**
     * Removes a lock
     *
     * @param lock The lock to remove
     */
    public void removeLock(EntityLock lock) {
        this.lockedEntities.removeIf(x -> x.equals(lock));
    }

    /* Getters */

    /**
     * Gets the rooms available in this system
     *
     * @return A list of ChatRoom objects
     */
    public List<ChatRoom> getRooms() {
        return hostedRooms;
    }

    /**
     * Gets the identities hosted on this coordination server
     *
     * @return A list of Identity objects
     */
    public List<Identity> getHostedIdentities() {
        return hostedIdentities;
    }

    /**
     * Gets the coordination servers known by this server
     *
     * @return A list of CoordinationServer objects
     */
    public List<CoordinationServer> getServers() {
        return servers;
    }

    /**
     * Gets the name of the local coordination server
     *
     * @return The local coordination server name
     */
    public String getThisServerId() {
        return thisServerId;
    }

    /* Setters */

    /**
     * Sets the local coordination server ID
     *
     * @param thisServerId The local coordination server ID
     */
    void setThisServerId(String thisServerId) {
        this.thisServerId = thisServerId;
    }

    /* Validators */

    /**
     * Validates a proposed identity is OK
     *
     * @param name The proposed client identity
     * @throws IdentityInUseException if the identity is in use on this server already
     */
    public void validateIdentityOk(String name) throws IdentityInUseException {
        if (hostedIdentities.stream().anyMatch(x -> x.getScreenName().equalsIgnoreCase(name))
                || lockedEntities.stream().anyMatch(x -> x.isLocked(name, LockType.IdentityLock))) {
            throw new IdentityInUseException(name);
        }
    }

    /**
     * Validates a proposed room name is OK
     *
     * @param name The proposed room name
     * @throws IdentityInUseException If the room name is in use on this server already
     */
    public void validateRoomOk(String name) throws IdentityInUseException {
        if (hostedRooms.stream().anyMatch(x -> x.getRoomId().equalsIgnoreCase(name))
                || lockedEntities.stream().anyMatch(x -> x.isLocked(name, LockType.RoomLock))) {
            throw new IdentityInUseException(name);
        }
    }

    /**
     * Finds a ChatRoom by ID
     *
     * @param roomId The chat room ID being requested
     * @return The chat room, or the MainHall if it doesn't exist
     */
    public ChatRoom getRoom(String roomId) {
        return hostedRooms.stream()
                .filter(x -> x.getRoomId().equalsIgnoreCase(roomId))
                .findFirst()
                .orElse(this.getMainhall());
    }

    /**
     * Gets the local coordination server instance
     *
     * @return The local coordination server reference
     */
    public CoordinationServer getThisCoordinationServer() {
        return this.servers.stream().filter(x -> x.getId().equals(this.thisServerId)).findFirst().get();
    }

    /**
     * Gets the MainHall room for this server
     *
     * @return The MainHall room for this server
     */
    public ChatRoom getMainhall() {
        return this.mainHall;
    }

    public void setMainHall(ChatRoom mainHall) {
        this.mainHall = mainHall;
    }
    
    public boolean isUserRegistered(String name){
      return this.registeredUsers.containsKey(name);
    }

    /* Setters */

    /**
     * Adds a user to the registered user hashmap with corresponding password
     *
     * @param name The user name
     * @param name The password of the user
     *
     * @return True if the user was added successfully, false otherwise.
     */
    public synchronized boolean addRegisteredUser(String name, String password) {
      if(!this.registeredUsers.containsKey(name)){
        Logger.info("Adding user {} to registered user list", name);
        this.registeredUsers.put(name, password);
        return true;
      }else{
        Logger.debug("User {} allready exists as registered user", name);
        return false;
      }
    }

    /**
     * Checks whether a user with the given name and password is registered.
     *
     * @param name The user name
     * @param name The password of the user
     *
     * @return if the users doesn't exist or the password is incorrect false is returned.
     *         True returned if user exists and password is correct
     */
    public synchronized boolean checkPasswordForUser(String name, String password){
      if(!this.registeredUsers.containsKey(name)){
        return false;
      }else{
        return this.registeredUsers.get(name).compareTo(password) == 0 ? true : false;
      }
    }

}
