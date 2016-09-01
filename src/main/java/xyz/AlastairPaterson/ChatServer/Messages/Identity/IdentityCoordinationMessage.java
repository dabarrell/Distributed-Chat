package xyz.AlastairPaterson.ChatServer.Messages.Identity;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * Represents an identity request coordination message
 */
public class IdentityCoordinationMessage extends Message {
    private String serverid;

    private String identity;

    private String approved;

    /**
     * Creates a new coordination request message
     * @param serverId The server requesting coordination
     * @param identity The identity requested
     */
    public IdentityCoordinationMessage(String serverId, String identity) {
        super("lockidentity");
        this.serverid = serverId;
        this.identity = identity;
    }

    /**
     * Creates a new coordination reply message
     * @param serverId The server replying to coordination
     * @param identity The identity requested
     * @param isApproved True if identity approved, false otherwise
     */
    public IdentityCoordinationMessage(String serverId, String identity, boolean isApproved) {
        super("lockidentity");
        this.serverid = serverId;
        this.identity = identity;
        this.approved = isApproved ? "true" : "false";
    }

    /**
     * Creates a new empty coordination request for JSON serialization
     */
    public IdentityCoordinationMessage() {
        super("lockidentity");
    }

    /**
     * Whether or not the request is approved
     * @return True if request approved, false otherwise
     */
    public boolean isApproved() {
        return approved.equalsIgnoreCase("true");
    }

    /**
     * The requested identity
     * @return The requested identity
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * The server ID sending the message
     * @return The server ID
     */
    public String getServerid() {
        return serverid;
    }
}
