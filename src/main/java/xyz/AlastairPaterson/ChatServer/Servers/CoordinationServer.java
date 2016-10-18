package xyz.AlastairPaterson.ChatServer.Servers;

import com.google.gson.Gson;
import org.pmw.tinylog.Logger;
import xyz.AlastairPaterson.ChatServer.Concepts.ChatRoom;
import xyz.AlastairPaterson.ChatServer.Concepts.EntityLock;
import xyz.AlastairPaterson.ChatServer.Concepts.LockType;
import xyz.AlastairPaterson.ChatServer.Exceptions.IdentityInUseException;
import xyz.AlastairPaterson.ChatServer.Messages.HelloMessage;
import xyz.AlastairPaterson.ChatServer.Messages.Identity.IdentityLockMessage;
import xyz.AlastairPaterson.ChatServer.Messages.Identity.IdentityUnlockMessage;
import xyz.AlastairPaterson.ChatServer.Messages.NewServer.GlobalLockMessage;
import xyz.AlastairPaterson.ChatServer.Messages.NewServer.GlobalReleaseMessage;
import xyz.AlastairPaterson.ChatServer.Messages.NewServer.NewServerRequestMessage;
import xyz.AlastairPaterson.ChatServer.Messages.addRegisteredUser.AddRegisteredUserMessage;
import xyz.AlastairPaterson.ChatServer.Messages.Message;
import xyz.AlastairPaterson.ChatServer.Messages.Room.Lifecycle.RoomCreateLockMessage;
import xyz.AlastairPaterson.ChatServer.Messages.Room.Lifecycle.RoomDelete;
import xyz.AlastairPaterson.ChatServer.Messages.Room.Lifecycle.RoomReleaseLockMessage;
import xyz.AlastairPaterson.ChatServer.StateManager;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.ConnectException;
import java.util.stream.Collectors;

/**
 * Coordinates client actions between servers
 */
public class CoordinationServer {
    private boolean connected = false;

    private boolean localInstance = false;

    private final int coordinationPort;

    private final int heartbeatPort;

    private final int userAdditionPort;

    private final String hostname;

    private final String id;

    private final int clientPort;

    private SSLServerSocket socket;

    private final Gson jsonSerializer = new Gson();

    private HeartbeatServer heartbeatServer;

    private UserAdditionServer userAdditionServer;

    private Boolean begun = false;


    /**
     * Creates a new coordination server
     *
     * @param id               The specified server ID
     * @param hostname         The host name or address
     * @param coordinationPort The port for coordination operations
     * @param clientPort       The port for client operations
     * @param localInstance    If this is a locally running server
     */
    public CoordinationServer(String id, String hostname, int coordinationPort, int clientPort, int heartbeatPort, int userAdditionPort, boolean localInstance) {
        this.id = id;
        this.hostname = hostname;
        this.coordinationPort = coordinationPort;
        this.clientPort = clientPort;
        this.heartbeatPort = heartbeatPort;
        this.userAdditionPort = userAdditionPort;
        this.localInstance = localInstance;
    }

    public void begin() throws Exception {
        if (begun) {
            Logger.warn("Already begun");
            return;
        }
        Thread workerThread;

        if (localInstance) {
            // Start server
            workerThread = new Thread(this::runServer);
            workerThread.setName(id + "CoordinationListener");
            socket = SocketServices.buildServerSocket(this.coordinationPort);
            Logger.info("Receiving co-ordination port is {}", this.coordinationPort);
            connected = true;

            //FIXME: Not sure if this is the right place to do this?
            ChatRoom mainHall = new ChatRoom("MainHall-" + id, this);
            StateManager.getInstance().getRooms().add(mainHall);
            StateManager.getInstance().setMainHall(mainHall);

            this.heartbeatServer = new HeartbeatServer(this.heartbeatPort);
            this.userAdditionServer = new UserAdditionServer(StateManager.getInstance().getThisServerId(),this.userAdditionPort);

        } else {
            // Check we can talk to this server
            workerThread = new Thread(this::validateConnectivity);
            workerThread.setName(id + "CoordinationValidator");
        }

        workerThread.start();
        begun = true;
    }

    public void finishLoad() throws Exception {
        informServersOfMainHall();

        new ClientListener(StateManager.getInstance().getThisCoordinationServer().getClientPort());

        Logger.debug("All servers reached. Chat service now available");

        Logger.debug("Finished config processing");
    }

    private void informServersOfMainHall() throws Exception {
        String mainHallId = StateManager.getInstance().getMainhall().getRoomId();
        RoomCreateLockMessage lockMessage = new RoomCreateLockMessage(mainHallId, "");
        RoomReleaseLockMessage unlockMessage = new RoomReleaseLockMessage(StateManager.getInstance().getThisServerId(),mainHallId, true);

        for (CoordinationServer coordinationServer : StateManager.getInstance().getServers()) {
            if (coordinationServer.equals(StateManager.getInstance().getThisCoordinationServer())) {
                continue;
            }

            coordinationServer.sendMessage(lockMessage);
            coordinationServer.sendMessage(unlockMessage);
        }
    }

    /**
     * Determines if the server is reachable
     *
     * @return True if available or local
     */
    public boolean isConnected() {
        return connected;
    }

    public void startHeartbeatServer() {
        this.heartbeatServer.startPolling();
    }

    /**
     * The 'id' of the server
     *
     * @return The server's id
     */
    public String getId() {
        return id;
    }

    /**
     * The host name of the server
     *
     * @return The server hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * The client listening port of the server
     *
     * @return The client port
     */
    public int getClientPort() {
        return clientPort;
    }

    /**
     * The coordination port of the server
     *
     * @return The coordination port
     */
    public int getCoordinationPort() {
        return coordinationPort;
    }

    /**
     * The heartbeat listening port of the server
     *
     * @return The heartbeat port
     */
    public int getHeartbeatPort() {
        return heartbeatPort;
    }

    /**
     * The user addition port of the server
     *
     * @return The user addition port
     */
    public int getUserAdditionPort() {
        return userAdditionPort;
    }


    /**
     * Sends a message to the specified coordination server
     *
     * @param message The message being sent
     * @return A JSON-encoded string with the result
     * @throws IOException Thrown if reading or writing fails
     */
    public String sendMessage(Message message) throws Exception {
        SSLSocket remoteServer = SocketServices.buildClientSocket(this.hostname, this.coordinationPort);
        Logger.debug("Sending message {} to {} on port {}", message.toString(), this.hostname, this.coordinationPort);
        SocketServices.writeToSocket(remoteServer, new Gson().toJson(message));
        return SocketServices.readFromSocket(remoteServer);
    }

    public void sendMessageWithoutReply(Message message) throws Exception{
        SSLSocket remoteServer = SocketServices.buildClientSocket(this.hostname, this.coordinationPort);
        SocketServices.writeToSocket(remoteServer, new Gson().toJson(message));
        Logger.debug("Sent message {} to {} on port {}", message.toString(), this.hostname, this.coordinationPort);
    }

    /**
     * Validates this server is reachable
     */
    private void validateConnectivity() {
        while (true) {
            try {
                this.sendMessage(new HelloMessage());
                break;
            } catch (ConnectException e) {
                Logger.debug("Couldn't reach {} at {} on port {} - error {}", this.id, this.hostname, this.coordinationPort, e.getMessage());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.error("Interrupted! {} {}", ex.getMessage(), ex.getStackTrace());
                }
            } catch (Exception e) {
                Logger.error("Unexpected exception {} {}", e.getMessage(), e.getStackTrace());
            }
        }
        Logger.debug("Validated connectivity to {}", this.id);
        connected = true;
    }

    /**
     * Runs a local coordination server
     */
    private void runServer() {
        while (true) {
            try {
                SSLSocket newConnection = (SSLSocket)socket.accept();

                //TODO: move to different thread?
                this.processCommand(newConnection);
            } catch (IOException e) {
                Logger.warn("IO exception occurred: {}", e.getMessage());
            }
        }
    }

    /**
     * Processes a client connection
     *
     * @param client The connection received
     */
    private void processCommand(SSLSocket client) {
        try {
            String receivedData = SocketServices.readFromSocket(client);

            Object replyObject = null;
            switch (jsonSerializer.fromJson(receivedData, Message.class).getType()) {
                case "hello":
                    replyObject = processHelloMessage();
                    break;
                case "lockidentity":
                    replyObject = processIdentityRequest(jsonSerializer.fromJson(receivedData, IdentityLockMessage.class));
                    break;
                case "releaseidentity":
                    processUnlockIdentityRequest(jsonSerializer.fromJson(receivedData, IdentityUnlockMessage.class));
                    break;
                case "lockroomid":
                    replyObject = processLockRoomRequest(jsonSerializer.fromJson(receivedData, RoomCreateLockMessage.class));
                    break;
                case "releaseroomid":
                    processUnlockRoomRequest(jsonSerializer.fromJson(receivedData, RoomReleaseLockMessage.class));
                    break;
                case "addRegisteredUser":
                    processAddRegisteredUser(jsonSerializer.fromJson(receivedData, AddRegisteredUserMessage.class));
                    break;
                case "deleteroom":
                    processDeleteRoomRequest(jsonSerializer.fromJson(receivedData, RoomDelete.class));
                    break;
                case "newServerRequest":
                    processNewServerRequest(jsonSerializer.fromJson(receivedData, NewServerRequestMessage.class));
                    break;
                case "globallock":
                    replyObject = processGlobalLockRequest(jsonSerializer.fromJson(receivedData, GlobalLockMessage.class));
                    break;
                case "globalrelease":
                    processUnlockGlobalRequest(jsonSerializer.fromJson(receivedData, GlobalReleaseMessage.class));
                    break;

            }

            SocketServices.writeToSocket(client, jsonSerializer.toJson(replyObject));
        } catch (IOException e) {
            Logger.warn("IO exception occurred: {}", e.getMessage());
        }
    }

    private void processDeleteRoomRequest(RoomDelete roomDelete) {
        StateManager.getInstance().getRooms().remove(StateManager.getInstance().getRoom(roomDelete.getRoomId()));
    }

    /**
     *
     */
    private void processAddRegisteredUser(AddRegisteredUserMessage message){
      if ( StateManager.getInstance().addRegisteredUser(message.getIdentity(), message.getPassword()) ){
        // User didn't exist and was added
        try{
          for(CoordinationServer server : StateManager.getInstance().getServers().stream()
              .filter(x -> !x.getId().equalsIgnoreCase(this.id)).collect(Collectors.toList())){
//            server.sendMessageWithoutReply(message);
              // TODO: 19/10/16 Why is this not sent? Has this been moved to user addition server?
          }
        }catch( Exception e ){
          Logger.error(e);
        }

      }else{
        Logger.info( "User {} is already registered", message.getIdentity() );
      }
    }

    /**
     * Processes a request to relinquish a room name lock
     *
     * @param roomReleaseLockMessage The release lock request
     */
    private void processUnlockRoomRequest(RoomReleaseLockMessage roomReleaseLockMessage) {
        StateManager.getInstance().removeLock(new EntityLock(roomReleaseLockMessage.getRoomId(), roomReleaseLockMessage.getServerId(), LockType.RoomLock));

        if (roomReleaseLockMessage.getApproved()) {
            CoordinationServer owningServer = StateManager.getInstance().getServers()
                    .stream()
                    .filter(x -> x.getId().equalsIgnoreCase(roomReleaseLockMessage.getServerId()))
                    .findFirst()
                    .get();

            StateManager.getInstance().getRooms().add(new ChatRoom(roomReleaseLockMessage.getRoomId(), owningServer));
        }
    }

    /**
     * Processes a room lock request
     *
     * @param roomCreateLockMessage The request to lock the room
     * @return A response to the requesting coordination server
     */
    private RoomCreateLockMessage processLockRoomRequest(RoomCreateLockMessage roomCreateLockMessage) {
        try {
            StateManager.getInstance().validateRoomOk(roomCreateLockMessage.getRoomid());
            StateManager.getInstance().addLock(roomCreateLockMessage.getRoomid(), roomCreateLockMessage.getServerid(), LockType.RoomLock);
            roomCreateLockMessage.setLocked(true);
        } catch (IdentityInUseException e) {
            roomCreateLockMessage.setLocked(false);
        }

        return roomCreateLockMessage;
    }

    /**
     * Processes a releaseidentity message
     *
     * @param identityUnlockMessage The received message
     */
    private void processUnlockIdentityRequest(IdentityUnlockMessage identityUnlockMessage) {
        StateManager.getInstance().removeLock(new EntityLock(identityUnlockMessage.getIdentity(),
                identityUnlockMessage.getServerId(),
                LockType.IdentityLock));
    }

    /**
     * Process a lockidentity request
     *
     * @param identityCoordinationMessage The message received from the coordination server
     * @return A reply to send to the origin server
     */
    private IdentityLockMessage processIdentityRequest(IdentityLockMessage identityCoordinationMessage) {
        boolean approved;
        try {
            StateManager.getInstance().validateIdentityOk(identityCoordinationMessage.getIdentity());
            approved = true;
        } catch (IdentityInUseException e) {
            approved = false;
        }

        StateManager.getInstance().addLock(identityCoordinationMessage.getIdentity(),
                identityCoordinationMessage.getServerId(),
                LockType.IdentityLock);

        return new IdentityLockMessage(StateManager.getInstance().getThisServerId(),
                identityCoordinationMessage.getIdentity(),
                approved);
    }

    /**
     * Processes a 'hello' message (out of spec, to validate connectivity)
     *
     * @return A response hello message
     */
    private Object processHelloMessage() {
        return new HelloMessage();
    }


    /**
     * Processes a newServerRequest message
     *
     * @param newServerRequestMessage The received message
     */
    private void processNewServerRequest(NewServerRequestMessage newServerRequestMessage) {
        Logger.info("New server request received");

        CoordinationServer newServer = new CoordinationServer(newServerRequestMessage.getServerId(),
                newServerRequestMessage.getHost(),
                newServerRequestMessage.getCoordPort(),
                newServerRequestMessage.getClientPort(),
                newServerRequestMessage.getHeartbeatPort(),
                newServerRequestMessage.getUserAdditionPort(),
                false);

        GlobalLockMessage lockRequest = new GlobalLockMessage(this.id, newServer);

        if (StateManager.getInstance().addServer(newServer)) {
            // Add lock on server
            StateManager.getInstance().addLock(newServer.getId(), newServer.getId(), LockType.ServerLock);

            // Server didn't exist and was added
            try{
                boolean allServersApprove = true;

                // Ask servers if the name is available
                for(CoordinationServer server : StateManager.getInstance().getServers().stream()
                        .filter(x -> !x.getId().equalsIgnoreCase(this.id))
                        .filter(x -> !x.getId().equalsIgnoreCase(newServer.getId()))
                        .collect(Collectors.toList())){
                    GlobalLockMessage response = jsonSerializer.fromJson(server.sendMessage(lockRequest), GlobalLockMessage.class);
                    if (!response.isApproved()) {
                        allServersApprove = false;
                        break;
                    }
                }

                lockRequest.setApproved(allServersApprove);
                newServer.sendMessageWithoutReply(lockRequest);


            }catch( Exception e ){
                Logger.error(e);
            }

        }else{
            Logger.info( "Server {} is already registered", newServerRequestMessage.getServerId() );
            lockRequest.setApproved(false);
            try {
                newServer.sendMessage(lockRequest);
            } catch (Exception e) {
                Logger.error("Unexpected exception {} {}", e.getMessage(), e.getStackTrace());
            }
        }
    }

    /**
     * Processes a global lock message
     *
     * @param globalLockMessage Incoming lock request
     * @return Updated lock request, or null if this is the server in question
     */
    private GlobalLockMessage processGlobalLockRequest(GlobalLockMessage globalLockMessage) {
        if (globalLockMessage.getNewServerId().equalsIgnoreCase(this.id)) {
            if (!globalLockMessage.isApproved()) {
                Logger.error("This serverId already exists - shutting down");
                System.exit(1);
            } else {
                Logger.info("New serverId approved - sending global release");

                GlobalReleaseMessage releaseMessage = new GlobalReleaseMessage(this.id);

                for(CoordinationServer server : StateManager.getInstance().getServers().stream()
                        .filter(x -> !x.getId().equalsIgnoreCase(this.id))
                        .collect(Collectors.toList())){

                    try {
                        server.sendMessageWithoutReply(releaseMessage);
                    } catch (Exception e) {
                        Logger.error("Unexpected exception {} {}", e.getMessage(), e.getStackTrace());
                    }

                }

                try {
                    Logger.debug("Attempting to begin and finish load");
                    begin();
                    finishLoad();
                } catch (Exception e) {
                    Logger.error("Unexpected exception {} {}", e.getMessage(), e.getStackTrace());
                }

            }

            return null;

        } else if (!globalLockMessage.getServerId().equalsIgnoreCase(this.id)) {
            // This server is receiving a lock request
            CoordinationServer newServer = new CoordinationServer(globalLockMessage.getNewServerId(),
                    globalLockMessage.getHost(),
                    globalLockMessage.getCoordPort(),
                    globalLockMessage.getClientPort(),
                    globalLockMessage.getHeartbeatPort(),
                    globalLockMessage.getUserAdditionPort(),
                    false);

            if (StateManager.getInstance().addServer(newServer)) {
                // Server didn't already exist, was added

                // Add lock on server
                StateManager.getInstance().addLock(newServer.getId(), newServer.getId(), LockType.ServerLock);

                Logger.info("Server {} added and locked", newServer.getId());

                globalLockMessage.setApproved(true);
            } else {
                Logger.info("Server {} already exists - lock denied", newServer.getId());
                globalLockMessage.setApproved(false);
            }

            return globalLockMessage;
        } else {
            return null;
        }
    }

    /**
     * Processes a global release message
     *
     * @param releaseMessage Incoming release
     */
    private void processUnlockGlobalRequest(GlobalReleaseMessage releaseMessage) {
        // Remove lock
        StateManager.getInstance().removeLock(new EntityLock(releaseMessage.getServerId(),
                releaseMessage.getServerId(),
                LockType.ServerLock));

        Logger.debug("Lock removed for server {}", releaseMessage.getServerId());

        CoordinationServer newServer = StateManager.getInstance().getServers().stream()
                .filter(x -> x.getId().equals(releaseMessage.getServerId())).findFirst().get();

        try {
            newServer.begin();
        } catch (Exception e) {
            Logger.error("Unexpected exception {} {}", e.getMessage(), e.getStackTrace());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoordinationServer that = (CoordinationServer) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
