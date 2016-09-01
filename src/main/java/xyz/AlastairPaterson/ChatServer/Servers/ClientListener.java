package xyz.AlastairPaterson.ChatServer.Servers;

import com.google.gson.Gson;
import org.pmw.tinylog.Logger;
import xyz.AlastairPaterson.ChatServer.Concepts.Identity;
import xyz.AlastairPaterson.ChatServer.Messages.Identity.IdentityCoordinationMessage;
import xyz.AlastairPaterson.ChatServer.Messages.Identity.IdentityUnlockMessage;
import xyz.AlastairPaterson.ChatServer.Messages.Message;
import xyz.AlastairPaterson.ChatServer.Messages.Identity.NewIdentityClientRequest;
import xyz.AlastairPaterson.ChatServer.Messages.Identity.NewIdentityClientResponse;
import xyz.AlastairPaterson.ChatServer.Messages.Room.RoomChangeClientResponse;
import xyz.AlastairPaterson.ChatServer.StateManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Listens to and processes client requests
 */
public class ClientListener {
    private ServerSocket listener;

    private final Gson jsonSerializer = new Gson();

    private final ArrayBlockingQueue<Socket> incomingConnections = new ArrayBlockingQueue<>(1024);

    /**
     * Creates a new ClientListener
     * @param port The port the ClientListener will bind to
     * @throws IOException If any thread operations fail
     */
    public ClientListener(int port) throws IOException {
        listener = new ServerSocket(port);

        Thread listenerThread = new Thread(this::runServer);
        listenerThread.setName("ClientListener");
        listenerThread.start();

        Thread runnerThread = new Thread(this::processConnection);
        runnerThread.setName("ClientRunner");
        runnerThread.start();
    }

    /**
     * Listens for new connections and queues them for processing
     */
    private void runServer() {
        while(true) {
            try {
                incomingConnections.add(listener.accept());
            } catch (IOException e) {
                Logger.warn("An IO exception occurred: {}", e.getMessage());
            }
        }
    }

    /**
     * Processes incoming connections from clients
     */
    private void processConnection() {
        while(true) {
            Socket connection = null;
            try {
                connection = incomingConnections.take();
                String clientRequest = SocketServices.readFromSocket(connection);
                Message clientMessage = jsonSerializer.fromJson(clientRequest, Message.class);

                Object response;

                switch (clientMessage.getType()) {
                    case "newidentity":
                        response = processNewClient(connection, clientRequest);
                        break;
                    default:
                        throw new IOException("Oops");
                }

                SocketServices.writeToSocket(connection, jsonSerializer.toJson(response));

            } catch (IOException e) {
                Logger.error("Coordination server exception! {} {}", e.getMessage());
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Processes a brand new client
     * @param connection The client's connection
     * @param clientRequest The new identity information sent by the client
     * @return A RoomChangeClientResponse to be returned to the client
     * @throws IOException If any IO exceptions occur these are passed to the caller
     */
    private Object processNewClient(Socket connection, String clientRequest) throws IOException {
        Object response;NewIdentityClientRequest requestObject = jsonSerializer.fromJson(clientRequest, NewIdentityClientRequest.class);
        response = this.processIdentityRequest(requestObject);
        SocketServices.writeToSocket(connection, jsonSerializer.toJson(response));
        response = new RoomChangeClientResponse(requestObject.getIdentity(),
                "",
                "MainHall-" + StateManager.getInstance().getThisServerId());
        return response;
    }

    /**
     * Processes a new identity request
     * @param request The identity request
     * @return A NewIdentityClientResponse to send to the client
     * @throws IOException If IO errors occur, thrown to caller
     */
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

        if (idRequestApproved) {
            Identity newId = new Identity(request.getIdentity());
            StateManager.getInstance().getHostedIdentities().add(newId);
            StateManager.getInstance().getHostedRooms().get(0).getMembers().add(newId);
        }

        IdentityUnlockMessage unlockMessage = new IdentityUnlockMessage(request.getIdentity());

        for(CoordinationServer s: StateManager.getInstance().getServers()) {
            s.sendMessage(unlockMessage);
        }

        return new NewIdentityClientResponse(idRequestApproved);
    }
}
