package xyz.AlastairPaterson.ChatServer.Servers;

import com.google.gson.Gson;
import org.pmw.tinylog.Logger;
import xyz.AlastairPaterson.ChatServer.Concepts.ChatRoom;
import xyz.AlastairPaterson.ChatServer.Concepts.Identity;
import xyz.AlastairPaterson.ChatServer.Exceptions.IdentityOwnsRoomException;
import xyz.AlastairPaterson.ChatServer.Exceptions.RemoteChatRoomException;
import xyz.AlastairPaterson.ChatServer.Messages.Identity.*;
import xyz.AlastairPaterson.ChatServer.Messages.Message;
import xyz.AlastairPaterson.ChatServer.Messages.Room.Membership.RoomChangeClientResponse;
import xyz.AlastairPaterson.ChatServer.StateManager;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Listens to and processes client requests
 */
public class ClientListener {
    private SSLServerSocket listener;

    private final Gson jsonSerializer = new Gson();

    private final ArrayBlockingQueue<Socket> incomingConnections = new ArrayBlockingQueue<>(1024);

    /**
     * Creates a new ClientListener
     * @param port The port the ClientListener will bind to
     * @throws IOException If any thread operations fail
     */
    public ClientListener(int port) throws Exception {
        listener = SocketServices.buildServerSocket(port);

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
                SSLSocket socket = (SSLSocket)listener.accept();

                this.establishClient(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void establishClient(SSLSocket connection) throws IOException {
        String clientRequest = SocketServices.readFromSocket(connection);
        Message clientMessage = jsonSerializer.fromJson(clientRequest, Message.class);

        if (clientMessage == null) {
            throw new IOException("Request is null");
        }

        switch(clientMessage.getType()) {
            case "newidentity":
                this.processNewIdentity(jsonSerializer.fromJson(clientRequest, NewIdentityRequest.class), connection);
                break;
            case "movejoin":
                this.processMoveIdentity(jsonSerializer.fromJson(clientRequest, MoveJoinClientRequest.class), connection);
                break;
        }
    }

    private void processMoveIdentity(MoveJoinClientRequest moveJoinClientRequest, SSLSocket connection) throws IOException {
        ChatRoom destination = StateManager.getInstance().getRoom(moveJoinClientRequest.getRoomId());
        SocketServices.writeToSocket(connection, jsonSerializer.toJson(new ServerChangeAcknowledgement()));

        this.createValidClient(moveJoinClientRequest.getIdentity(), connection, destination);
    }

    private void processNewIdentity(NewIdentityRequest newIdentityRequest, SSLSocket connection) throws IOException {
        boolean identityOk = this.validateIdentity(newIdentityRequest.getIdentity());

        if (identityOk) {
            Logger.info("Identity OK - continuing");
            createValidClient(newIdentityRequest.getIdentity(), connection, StateManager.getInstance().getMainhall());
        }

        SocketServices.writeToSocket(connection, jsonSerializer.toJson(new NewIdentityResponse(identityOk)));

        this.unlockIdentity(newIdentityRequest.getIdentity());
    }

    private void createValidClient(String identity, Socket connection, ChatRoom room) throws IOException {
        ClientConnection newClientConnection = new ClientConnection(connection);

        Identity newIdentity = new Identity(identity, room, newClientConnection);

        StateManager.getInstance().getHostedIdentities().add(newIdentity);
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
