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
import java.util.stream.Collectors;

/**
 * Coordinates client actions between servers
 */
public class UserAdditionServer{

    private boolean connected = false;

    private int port;

    private SSLServerSocket socket;

    private final Gson jsonSerializer = new Gson();

    private String id;

    /**
     * Creates a new userAdditionServer
     *
     * @param id               The specified server ID
     * @throws IOException Thrown if initialization fails for some reason
     */
    public UserAdditionServer(String id,int port) throws Exception {
        this.id = id;
        this.port = port;

        Thread workerThread;

        // Start server
        workerThread = new Thread(this::runServer);
        workerThread.setName(id + "UserAdditionListener");
        socket = SocketServices.buildServerSocket(port);

        workerThread.start();
    }


    /**
     * Runs a local coordination server
     */
    private void runServer() {
        while (true) {
            try {
                SSLSocket newConnection = (SSLSocket)socket.accept();
                new AddRegisteredUserThread(newConnection, id).start();
            } catch (IOException e) {
                Logger.warn("IO exception occurred: {}", e.getMessage());
            }
        }
    }

    /**
     * The 'id' of the server
     * @return The server's id
     */
    public String getId() {
        return id;
    }

  public class AddRegisteredUserThread extends Thread{

    private SSLSocket socket;
    private final Gson jsonSerializer = new Gson();
    private String callingHost;

    public AddRegisteredUserThread(SSLSocket socket, String callingHost){
      this.socket = socket;
      this.callingHost = callingHost;
    }

    @Override
    public void run() {
        try {
            String receivedData = SocketServices.readFromSocket(socket);

            // Must be correct type of message
            if( jsonSerializer.fromJson(receivedData, Message.class).getType().compareTo("addRegisteredUser") != 0 ){
              Logger.error("Received a unexpected message on the addUserPort");
              return;
            }

            AddRegisteredUserMessage message = jsonSerializer.fromJson(receivedData, AddRegisteredUserMessage.class);

            if ( StateManager.getInstance().addRegisteredUser(message.getIdentity(), message.getPassword()) ){
              try{
                for(CoordinationServer server : StateManager.getInstance().getServers().stream()
                    .filter(x -> !x.getId().equalsIgnoreCase(this.callingHost)).collect(Collectors.toList())){
                  server.sendMessageWithoutReply(message);
                }
              }catch( Exception e ){
                Logger.error(e);
              }
            }else{
              Logger.info( "User {} is allready registered", message.getIdentity() );
            }

            //SocketServices.writeToSocket(socket, jsonSerializer.toJson(replyObject));
        } catch (IOException e) {
            Logger.warn("IO exception occurred: {}", e.getMessage());
        }
    }

  }
}


