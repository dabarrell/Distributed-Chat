package xyz.AlastairPaterson.ChatServer.Messages;

/**
 * Pings another server to verify it is still alive
 */
public class HeartbeatMessage extends Message {

    /**
     * Creates a new heartbeat
     */
    public HeartbeatMessage() {
        super("heartbeat");
    }
}
