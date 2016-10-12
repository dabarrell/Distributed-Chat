package xyz.AlastairPaterson.ChatServer.Messages;

import java.util.Random;

/**
 * Pings another server to verify it is still alive
 */
public class HeartbeatMessage extends Message {
    private int sequence;

    /**
     * Creates a new heartbeat
     */
    public HeartbeatMessage() {
        super("heartbeat");
        this.sequence = new Random().nextInt();
    }

    public int getSequence() {
        return sequence;
    }
}
