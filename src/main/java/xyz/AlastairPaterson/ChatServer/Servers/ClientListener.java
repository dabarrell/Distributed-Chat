package xyz.AlastairPaterson.ChatServer.Servers;

import com.google.gson.Gson;
import xyz.AlastairPaterson.ChatServer.Concepts.Identity;
import xyz.AlastairPaterson.ChatServer.Messages.Identity.IdentityCoordinationMessage;
import xyz.AlastairPaterson.ChatServer.Messages.Message;
import xyz.AlastairPaterson.ChatServer.Messages.Identity.NewIdentityClientRequest;
import xyz.AlastairPaterson.ChatServer.Messages.Identity.NewIdentityClientResponse;
import xyz.AlastairPaterson.ChatServer.StateManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by atp on 30/08/2016.
 */
public class ClientListener {
    private ServerSocket listener;

    private final Gson jsonSerializer = new Gson();

    private final ArrayBlockingQueue<Socket> incomingConnections = new ArrayBlockingQueue<>(1024);

    public ClientListener(int port) throws IOException {
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
                Message clientMessage = jsonSerializer.fromJson(clientRequest, Message.class);

                Object response = null;

                switch (clientMessage.getType()) {
                    case "newidentity":
                        response = this.processIdentityRequest(jsonSerializer.fromJson(clientRequest, NewIdentityClientRequest.class));
                        break;
                    default:
                        throw new IOException("Oops");
                }

                SocketServices.writeToSocket(connection, new Gson().toJson(response));

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private NewIdentityClientResponse processIdentityRequest(NewIdentityClientRequest request) throws IOException {
        if (request.getIdentity().length() > 16 || request.getIdentity().length() < 3) {
            return new NewIdentityClientResponse(false);
        }

        boolean idRequestApproved = true;

        IdentityCoordinationMessage coordinationRequest = new IdentityCoordinationMessage(StateManager.getInstance().getThisServerId(), request.getIdentity());

        for(CoordinationServer s: StateManager.getInstance().getServers()) {
            IdentityCoordinationMessage response = jsonSerializer.fromJson(s.sendMessage(coordinationRequest), IdentityCoordinationMessage.class);
            if (!response.isApproved()) {
                idRequestApproved = false;
                break;
            }
        }
        StateManager.getInstance().getHostedIdentities().add(new Identity(request.getIdentity()));

        return new NewIdentityClientResponse(idRequestApproved);
    }
}
