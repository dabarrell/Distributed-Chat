package xyz.AlastairPaterson.ChatServer.Messages.Identity;

import xyz.AlastairPaterson.ChatServer.Messages.Message;
import xyz.AlastairPaterson.ChatServer.StateManager;

/**
 * Represents a request to unlock a locked identity
 */
public class IdentityUnlockMessage extends Message {
    private String serverid;

    private String identity;

    /**
     * Creates a new identity unlock message
     * @param identity The identity being unlocked
     */
    public IdentityUnlockMessage(String identity) {
        super("releaseidentity");
        this.serverid = StateManager.getInstance().getThisServerId();
        this.identity = identity;
    }

    /**
     * Gets the requesting server ID
     * @return The requesting server ID
     */
    public String getServerid() {
        return serverid;
    }

    /**
     * Gets the target ID
     * @return The target ID
     */
    public String getIdentity() {
        return identity;
    }
}
