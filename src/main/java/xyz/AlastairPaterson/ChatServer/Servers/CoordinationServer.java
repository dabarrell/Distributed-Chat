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
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Coordinates client actions between servers
 */
public class CoordinationServer {
    private boolean connected = false;

    private final int coordinationPort;

    private final String hostname;

    private final String id;

    private final int clientPort;

    private SSLServerSocket socket;

    private final Gson jsonSerializer = new Gson();

    /**
     * Creates a new coordination server
     *
     * @param id               The specified server ID
     * @param hostname         The host name or address
     * @param coordinationPort The port for coordination operations
     * @param clientPort       The port for client operations
     * @param localInstance    If this is a locally running server
     * @throws IOException Thrown if initialization fails for some reason
     */
    public CoordinationServer(String id, String hostname, int coordinationPort, int clientPort, boolean localInstance) throws Exception {
        this.id = id;
        this.hostname = hostname;
        this.coordinationPort = coordinationPort;
        this.clientPort = clientPort;

        Thread workerThread;

        if (localInstance) {
            // Start server
            workerThread = new Thread(this::runServer);
            workerThread.setName(id + "CoordinationListener");
            socket = SocketServices.buildServerSocket(this.coordinationPort);
            Logger.info("Recieving co-ordination port is {}", this.coordinationPort);
            connected = true;

            //FIXME: Not sure if this is the right place to do this?
            ChatRoom mainHall = new ChatRoom("MainHall-" + id, this);
            StateManager.getInstance().getRooms().add(mainHall);
            StateManager.getInstance().setMainHall(mainHall);
        } else {
            // Check we can talk to this server
            workerThread = new Thread(this::validateConnectivity);
            workerThread.setName(id + "CoordinationValidator");
        }

        workerThread.start();
    }

    /**
     * Determines if the server is reachable
     *
     * @return True if available or local
     */
    public boolean isConnected() {
        return connected;
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

    /**
     * Validates this server is reachable
     */
    private void validateConnectivity() {
        while (true) {
            try {
                this.sendMessage(new HelloMessage());
                break;
            } catch (ConnectException e) {
                Logger.debug("Couldn't reach {} - error {}", this.id, e.getMessage());
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
      if (!StateManager.getInstance().isUserRegistered(message.getIdentity())){
        StateManager.getInstance().addRegisteredUser(message.getIdentity());

        try{
          for(CoordinationServer server : StateManager.getInstance().getServers()) {
            server.sendMessage(message);
          }
        }catch( Exception e ){
          Logger.error(e);
        }

      }else{
        Logger.info( "User {} is allready registered", message.getIdentity() );
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
}
