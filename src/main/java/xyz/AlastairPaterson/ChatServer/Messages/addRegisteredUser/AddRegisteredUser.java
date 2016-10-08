package xyz.AlastairPaterson.ChatServer.Messages.addRegisteredUser;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * Represents a new user to be registered by the server
 */
public class AddRegisteredUser extends Message {
    private String serverid;
    private String identity;

    /**
     * Creates a new message
     * @param identity The identity requested
     */
    public AddRegisteredUser(String identity) {
        super("addRegisteredUser");
        this.identity = identity;
    }

    /**
     * The requested identity
     * @return The requested identity
     */
    public String getIdentity() {
        return identity;
    }

}
