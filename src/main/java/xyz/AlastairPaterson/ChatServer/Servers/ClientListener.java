package xyz.AlastairPaterson.ChatServer.Servers;

import com.google.gson.Gson;
import org.pmw.tinylog.Logger;
import xyz.AlastairPaterson.ChatServer.Concepts.ChatRoom;
import xyz.AlastairPaterson.ChatServer.Concepts.Identity;
import xyz.AlastairPaterson.ChatServer.Messages.Identity.*;
import xyz.AlastairPaterson.ChatServer.Messages.Message;
import xyz.AlastairPaterson.ChatServer.Messages.Room.Membership.RoomChangeClientResponse;
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

                this.establishClient(connection);

            } catch (IOException | InterruptedException e) {
                //TODO: disconnect the client
                e.printStackTrace();
            }
        }
    }

    private void establishClient(Socket connection) throws IOException {
        String clientRequest = SocketServices.readFromSocket(connection);
        Message clientMessage = jsonSerializer.fromJson(clientRequest, Message.class);

        if (clientMessage == null) {
            throw new IOException("Request is null");
        }

        switch(clientMessage.getType()) {
            case "newidentity":
                this.processNewIdentity(jsonSerializer.fromJson(clientRequest, NewIdentityRequest.class), connection);
            case "movejoin":
                this.processMoveIdentity(jsonSerializer.fromJson(clientRequest, MoveJoinClientRequest.class), connection);
        }
    }

    private void processMoveIdentity(MoveJoinClientRequest moveJoinClientRequest, Socket connection) {
    }

    private void processNewIdentity(NewIdentityRequest newIdentityRequest, Socket connection) throws IOException {
        boolean identityOk = this.validateIdentity(newIdentityRequest.getIdentity());

        if (identityOk) {
            Logger.info("Identity OK - continuing");
            createValidClient(newIdentityRequest, connection);
        }

        SocketServices.writeToSocket(connection, jsonSerializer.toJson(new NewIdentityResponse(identityOk)));

        this.unlockIdentity(newIdentityRequest.getIdentity());

        if (identityOk) {
            this.broadcastNewUser(newIdentityRequest.getIdentity(), StateManager.getInstance().getMainhall());
        }
    }

    private void broadcastNewUser(String identity, ChatRoom mainhall) throws IOException {
        mainhall.broadcast(new RoomChangeClientResponse(identity, "", mainhall.getRoomId()));
    }

    private void unlockIdentity(String identity) throws IOException {
        IdentityUnlockMessage unlockMessage = new IdentityUnlockMessage(identity);
        for (CoordinationServer coordinationServer : StateManager.getInstance().getServers()) {
            coordinationServer.sendMessage(unlockMessage);
        }
    }

    private void createValidClient(NewIdentityRequest newIdentityRequest, Socket connection) throws IOException {
        ChatRoom mainHall = StateManager.getInstance().getMainhall();

        Identity newIdentity = new Identity(newIdentityRequest.getIdentity(), mainHall);

        ClientConnection newClientConnection = new ClientConnection(connection, newIdentity);
        newIdentity.setConnection(newClientConnection);

        StateManager.getInstance().getHostedIdentities().add(newIdentity);

        mainHall.getMembers().add(newIdentity);
    }

    private boolean validateIdentity(String identity) throws IOException {
        if (identity.length() < 3 || identity.length() > 16) {
            return false;
        }

        IdentityLockMessage lockMessage = new IdentityLockMessage(StateManager.getInstance().getThisServerId(), identity);

        for (CoordinationServer coordinationServer : StateManager.getInstance().getServers()) {
            IdentityLockMessage reply = jsonSerializer.fromJson(coordinationServer.sendMessage(lockMessage), IdentityLockMessage.class);

            if (!reply.isApproved()) {
                return false;
            }
        }

        return true;
    }
}
