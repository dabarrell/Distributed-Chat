package xyz.AlastairPaterson.ChatServer.Servers;

import com.google.gson.Gson;
import org.pmw.tinylog.Logger;
import xyz.AlastairPaterson.ChatServer.Exceptions.ServerFailureException;
import xyz.AlastairPaterson.ChatServer.Messages.HeartbeatMessage;
import xyz.AlastairPaterson.ChatServer.StateManager;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * Listens for, replies to and generates heartbeat messages at a set interval
 */
class HeartbeatServer {
    private SSLServerSocket serverSocket;
    private String id;

    private final Gson jsonSerializer = new Gson();

    /**
     * Creates a new heartbeat server listening and communicating on the specified port
     *
     * @param heartbeatPort The port to listen on
     */
    HeartbeatServer(int heartbeatPort, String id) throws Exception {
        serverSocket = SocketServices.buildServerSocket(heartbeatPort);
        this.id = id;

        Thread heartbeatThread = new Thread(this::respondHeartbeat, "HeartBeatThread");
        heartbeatThread.start();
    }

    /**
     * Commences the polling timer
     */
    void startPolling() {
        int POLL_INTERVAL_SECONDS = 10;

        Timer pollTimer = new Timer("HeartbeatPollThread");
        pollTimer.scheduleAtFixedRate(new HeartBeatPollTask(this.id), 0, POLL_INTERVAL_SECONDS * 1000);
    }

    public class HeartBeatPollTask extends TimerTask{
      String id;
      public HeartBeatPollTask(String id){
        this.id = id;
      }
      @Override
      public void run() {
          Logger.debug("Beginning heartbeat");
          for (CoordinationServer s: StateManager.getInstance().getServers().stream()
              .filter(x -> !x.getId().equalsIgnoreCase(this.id))
              .collect(Collectors.toList())){
              try {
                  sendHeartbeat(s);
              } catch (ServerFailureException e) {
                  failServer(s);
              }
              catch (Exception e) {
                  e.printStackTrace();
              }
          }
      }
    } 



    /**
     * Fails a server, removing it from the list
     *
     * @param s The server to fail
     */
    private void failServer(CoordinationServer s) {
        Logger.error("Server {} has failed!", s.getHostname());
        StateManager.getInstance().removeServer(s);
    }
    
    /**
     * Sends a heartbeat message to a coordination server
     *
     * @param server The server to communicate with
     * @throws ServerFailureException If the server communication fails, the server is assumed to have failed
     */
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

    /**
     * Listens for and responds to heartbeat messages
     */
    private void respondHeartbeat() {
        while (true) {
            try {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                String heartbeat = SocketServices.readFromSocket(clientSocket);

                SocketServices.writeToSocket(clientSocket, heartbeat);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
