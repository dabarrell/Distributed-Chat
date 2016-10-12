package xyz.AlastairPaterson.ChatServer.Servers;

import com.google.gson.Gson;
import org.pmw.tinylog.Logger;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by atp on 12/10/16.
 */
public class HeartbeatServer {
    private final int POLL_INTERVAL_SECONDS = 10;

    private int heartbeatPort;

    private SSLServerSocket serverSocket;
    private SSLSocketFactory clientSocketFactory;

    private final Gson jsonSerializer = new Gson();

    /**
     * Creates a new heartbeat server listening and communicating on the specified port
     *
     * @param heartbeatPort
     */
    public HeartbeatServer(int heartbeatPort) throws Exception {
        serverSocket = SocketServices.buildServerSocket(heartbeatPort);

        clientSocketFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();

        this.heartbeatPort = heartbeatPort;
        this.runPoll();

        Thread heartbeatThread = new Thread(this::respondHeartbeat, "HeartBeatThread");
        heartbeatThread.run();
    }

    private void runPoll() {
        Timer pollTimer = new Timer();
        pollTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Logger.debug("Beginning heartbeat");
            }
        }, 0, POLL_INTERVAL_SECONDS * 1000);
    }

    private void respondHeartbeat() {
        try {
            while (true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                String heartbeat = SocketServices.readFromSocket(clientSocket);

                SocketServices.writeToSocket(clientSocket, heartbeat);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
