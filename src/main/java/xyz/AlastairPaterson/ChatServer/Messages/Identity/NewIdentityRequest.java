package xyz.AlastairPaterson.ChatServer.Messages.Identity;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * A request from a client concerning a new identity
 */
public class NewIdentityRequest extends Message {
    private String identity;

    /**
     * Creates a NewIdentityRequest
     */
    public NewIdentityRequest() {
        super("newidentity");
    }

    /**
     * Gets the requested identity
     * @return The requested identity
     */
    public String getIdentity() {
        return identity;
    }
}
