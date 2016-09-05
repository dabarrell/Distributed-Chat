package xyz.AlastairPaterson.ChatServer.Servers;

import com.google.gson.Gson;
import org.pmw.tinylog.Logger;
import xyz.AlastairPaterson.ChatServer.Concepts.ChatRoom;
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
    }

    /**
     * Listens for new connections and queues them for processing
     */
    private void runServer() {
        while(true) {
            try {
                Thread runnerThread = new Thread(this::processConnection);
                runnerThread.setName("ClientRunner");
                runnerThread.start();

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
        Socket connection;

        while (!listener.isClosed()) {
            try {
                connection = incomingConnections.take();

                String clientRequest = SocketServices.readFromSocket(connection);
                Message clientMessage = jsonSerializer.fromJson(clientRequest, Message.class);

                if (clientMessage == null) {
                    throw new IOException("Request is null");
                }

                if (!clientMessage.getType().equalsIgnoreCase("newidentity")) {
                    throw new IOException("Sequence invalid");
                }

                Identity newIdentity = processIdentityRequest(clientRequest, connection);
                Object response = processNewClient(connection, newIdentity);

                newIdentity.getCurrentRoom().getMembers().forEach(x -> {
                    try {
                        x.sendMessage(response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                SocketServices.writeToSocket(connection, jsonSerializer.toJson(response));

            } catch (IOException | InterruptedException e) {
                //TODO: disconnect the client
                e.printStackTrace();
            }
        }
    }

    //TODO: rename
    /**
     * Processes a brand new client
     * @param connection The client's connection
     * @param newIdentity The new identity or null if the request was denied
     * @return A RoomChangeClientResponse to be returned to the client
     * @throws IOException If any IO exceptions occur these are passed to the caller
     */
    private Object processNewClient(Socket connection, Identity newIdentity) throws IOException {
        if(newIdentity == null) {
            // Request denied
            return new NewIdentityClientResponse(false);
        }

        SocketServices.writeToSocket(connection, jsonSerializer.toJson(new NewIdentityClientResponse(true)));

        //TODO: needs to work if user changes room
        return new RoomChangeClientResponse(newIdentity.getScreenName(),
                "",
                "MainHall-" + StateManager.getInstance().getThisServerId());
    }

    /**
     * Processes a new identity request
     * @param requestString The identity request
     * @return A NewIdentityClientResponse to send to the client
     * @throws IOException If IO errors occur, thrown to caller
     */
    private Identity processIdentityRequest(String requestString, Socket clientSocket) throws IOException {
        NewIdentityClientRequest request = jsonSerializer.fromJson(requestString, NewIdentityClientRequest.class);

        if (request.getIdentity().length() > 16 || request.getIdentity().length() < 3) {
            return null;
        }

        boolean idRequestApproved = true;
        Identity newId = null;

        IdentityCoordinationMessage coordinationRequest = new IdentityCoordinationMessage(StateManager.getInstance().getThisServerId(), request.getIdentity());

        for(CoordinationServer s: StateManager.getInstance().getServers()) {
            IdentityCoordinationMessage response = jsonSerializer.fromJson(s.sendMessage(coordinationRequest), IdentityCoordinationMessage.class);
            if (!response.isApproved()) {
                idRequestApproved = false;
                break;
            }
        }

        if (idRequestApproved) {
            ChatRoom defaultRoom = StateManager.getInstance().getHostedRooms().get(0);
            newId = new Identity(request.getIdentity(), defaultRoom, clientSocket);

            StateManager.getInstance().getHostedIdentities().add(newId);
            defaultRoom.getMembers().add(newId);
        }

        IdentityUnlockMessage unlockMessage = new IdentityUnlockMessage(request.getIdentity());

        for(CoordinationServer s: StateManager.getInstance().getServers()) {
            s.sendMessage(unlockMessage);
        }

        return newId;
    }
}
