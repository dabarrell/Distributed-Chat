package xyz.AlastairPaterson.ChatServer.Servers;

import com.google.gson.Gson;
import xyz.AlastairPaterson.ChatServer.Messages.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by atp on 30/08/2016.
 */
public class ClientListener {
    private int port;

    private ServerSocket listener;

    private final ArrayBlockingQueue<Socket> incomingConnections = new ArrayBlockingQueue<>(1024);

    public ClientListener(int port) throws IOException {
        this.port = port;

        listener = new ServerSocket(port);

        Thread listenerThread = new Thread(this::runServer);
        listenerThread.setName("ClientListener");
        listenerThread.start();

        Thread runnerThread = new Thread(this::processConnection);
        runnerThread.setName("ClientRunner");
        runnerThread.start();
    }

    private void runServer() {
        while(true) {
            try {
                incomingConnections.add(listener.accept());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processConnection() {
        while(true) {
            try {
                Socket connection = incomingConnections.take();
                String clientRequest = SocketServices.readFromSocket(connection);
                Message clientMessage = new Gson().fromJson(clientRequest, Message.class);

                Object response = null;

                SocketServices.writeToSocket(connection, new Gson().toJson(response));

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
