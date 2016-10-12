package xyz.AlastairPaterson.ChatServer.Messages.addRegisteredUser;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * Represents a new user to be registered by the server
 */
public class AddRegisteredUserMessage extends Message {
    private String serverid;
    private String identity;
    private String password;

    /**
     * Creates a new message
     * @param identity The identity requested
     */
    public AddRegisteredUserMessage(String identity, String password) {
        super("addRegisteredUser");
        this.identity = identity;
        this.password = password;
    }

    /**
     * The requested identity
     * @return The requested identity
     */
    public String getIdentity() {
        return identity;
    }

    public String getPassword() {
        return password;
    }

}
