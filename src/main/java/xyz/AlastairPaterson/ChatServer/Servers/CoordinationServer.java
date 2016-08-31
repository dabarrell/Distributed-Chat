package xyz.AlastairPaterson.ChatServer.Servers;

import com.google.gson.Gson;
import org.pmw.tinylog.Logger;
import xyz.AlastairPaterson.ChatServer.Messages.HelloMessage;
import xyz.AlastairPaterson.ChatServer.Messages.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Coordinates client actions between servers
 */
public class CoordinationServer {
    private boolean connected = false;

    private int coordinationPort;

    private String hostname;

    private String id;

    private int clientPort;

    private ServerSocket socket;

    /**
     * Creates a new coordination server
     * @param id The specified server ID
     * @param hostname The host name or address
     * @param coordinationPort The port for coordination operations
     * @param clientPort The port for client operations
     * @param localInstance If this is a locally running server
     * @throws IOException Thrown if initialization fails for some reason
     */
    public CoordinationServer(String id, String hostname, int coordinationPort, int clientPort, boolean localInstance) throws IOException {
        this.id = id;
        this.hostname = hostname;
        this.coordinationPort = coordinationPort;
        this.clientPort = clientPort;

        Thread workerThread;

        if(localInstance) {
            // Start server
            workerThread = new Thread(this::runServer);
            workerThread.setName(id + "CoordinationListener");
            socket = new ServerSocket(this.coordinationPort);
            connected = true;
        }
        else {
            // Check we can talk to this server
            workerThread = new Thread(this::validateConnectivity);
            workerThread.setName(id + "CoordinationValidator");
        }

        workerThread.start();
    }

    /**
     * Determines if the server is reachable
     * @return True if available or local
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * The 'id' of the server
     * @return The server's id
     */
    public String getId() {
        return id;
    }

    /**
     * Sends a message to the specified coordination server
     * @param message The message being sent
     * @return A JSON-encoded string with the result
     * @throws IOException Thrown if reading or writing fails
     */
    private String sendMessage(Message message) throws IOException {
        Socket remoteServer = new Socket(this.hostname, this.coordinationPort);
        SocketServices.writeToSocket(remoteServer, new Gson().toJson(message));
        return SocketServices.readFromSocket(remoteServer);
    }

    /**
     * Validates this server is reachable
     */
    private void validateConnectivity() {
        while(true) {
            try {
                this.sendMessage(new HelloMessage());
                break;
            } catch (IOException e) {
                Logger.debug("Couldn't reach {} - error {}", this.id, e.getMessage());
                try {
                    Thread.sleep(3000);
                }
                catch(InterruptedException ex) {
                    Logger.error("Interrupted! {} {}", ex.getMessage(), ex.getStackTrace());
                }
            }
        }
        Logger.debug("Validated connectivity to {}", this.id);
        connected = true;
    }

    /**
     * Runs a local coordination server
     */
    private void runServer() {
        while(true) {
            try {
                Socket newConnection = socket.accept();

                //TODO: move to different thread?
                this.processCommand(newConnection);
            } catch (IOException e) {
                Logger.warn("IO exception occurred: {}", e.getMessage());
            }
        }
    }

    /**
     * Processes a client connection
     * @param client The connection received
     */
    private void processCommand(Socket client) {
        try {
            String receivedData = SocketServices.readFromSocket(client);
            Message messageType = new Gson().fromJson(receivedData, Message.class);

            Object replyObject = null;
            switch(messageType.getType()) {
                case "hello":
                    replyObject = processHelloMessage(receivedData);
                    break;
            }

            SocketServices.writeToSocket(client, new Gson().toJson(replyObject));
        } catch (IOException e) {
            Logger.warn("IO exception occurred: {}", e.getMessage());
        }
    }

    /**
     * Processes a 'hello' message (out of spec, to validate connectivity)
     * @param message The hello message received
     * @return A response hello message
     */
    private Object processHelloMessage(String message) {
        return new HelloMessage();
    }
}
