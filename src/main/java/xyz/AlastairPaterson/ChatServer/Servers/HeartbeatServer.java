package xyz.AlastairPaterson.ChatServer.Servers;

import com.google.gson.Gson;
import org.pmw.tinylog.Logger;
import xyz.AlastairPaterson.ChatServer.Exceptions.ServerFailureException;
import xyz.AlastairPaterson.ChatServer.Messages.HeartbeatMessage;
import xyz.AlastairPaterson.ChatServer.StateManager;

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

        Thread heartbeatThread = new Thread(this::respondHeartbeat, "HeartBeatThread");
        heartbeatThread.start();
    }

    public void startPolling() {
        Timer pollTimer = new Timer("HeartbeatPollThread");
        pollTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Logger.debug("Beginning heartbeat");
                for (CoordinationServer s: StateManager.getInstance().getServers()) {
                    try {
                        sendHeartbeat(s);
                    } catch (ServerFailureException e) {
                        Logger.error("Server {} has failed!", s.getHostname());
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, POLL_INTERVAL_SECONDS * 1000);
    }

    private void sendHeartbeat(CoordinationServer server) throws ServerFailureException {
        try {
            HeartbeatMessage message = new HeartbeatMessage();
            SSLSocket clientSocket = SocketServices.buildClientSocket(server.getHostname(), server.getHeartbeatPort());
            SocketServices.writeToSocket(clientSocket, jsonSerializer.toJson(message));
            HeartbeatMessage response = jsonSerializer.fromJson(SocketServices.readFromSocket(clientSocket), HeartbeatMessage.class);
            if (message.getSequence() != response.getSequence()) throw new ServerFailureException();
        } catch (Exception e) {
            throw new ServerFailureException();
        }
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
