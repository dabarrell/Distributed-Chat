package xyz.AlastairPaterson.ChatServer.Servers;

import com.google.gson.Gson;
import org.pmw.tinylog.Logger;
import xyz.AlastairPaterson.ChatServer.Messages.HelloMessage;
import xyz.AlastairPaterson.ChatServer.Messages.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by atp on 30/08/2016.
 */
public class CoordinationServer {
    private boolean connected = false;

    private int coordinationPort;

    private String hostname;

    private String id;

    private int clientPort;

    private ServerSocket socket;

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
            workerThread = new Thread(this::validateConnectivity);
            workerThread.setName(id + "CoordinationValidator");
        }

        workerThread.start();
    }

    public boolean isConnected() {
        return connected;
    }

    public String getId() {
        return id;
    }

    private String sendMessage(Message message) throws IOException {
        Socket remoteServer = new Socket(this.hostname, this.coordinationPort);
        SocketServices.writeToSocket(remoteServer, new Gson().toJson(message));
        return SocketServices.readFromSocket(remoteServer);
    }

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

    private Object processHelloMessage(String message) {
        return new HelloMessage();
    }
}
