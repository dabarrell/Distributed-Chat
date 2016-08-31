package xyz.AlastairPaterson.ChatServer.Servers;

import com.google.gson.Gson;
import org.pmw.tinylog.Logger;
import xyz.AlastairPaterson.ChatServer.Messages.HelloMessage;
import xyz.AlastairPaterson.ChatServer.Messages.Message;

import java.io.IOException;
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

    public CoordinationServer(String id, String hostname, int coordinationPort, int clientPort, boolean localInstance) {
        this.id = id;
        this.hostname = hostname;
        this.coordinationPort = coordinationPort;
        this.clientPort = clientPort;

        Thread workerThread;

        if(localInstance && false) {
            // Start server
            workerThread = new Thread();
            connected = true;
        }
        else {
            workerThread = new Thread(this::validateConnectivity);
            workerThread.setName(id + "ConnectionValidator");
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
}
