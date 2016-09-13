package xyz.AlastairPaterson.ChatServer.Messages.Identity;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * A response to a client regarding an identity request
 */
public class NewIdentityResponse extends Message {
    private final String approved;

    /**
     * Creates a NewIdentityResponse
     * @param approved True if the request is approved, false otherwise
     */
    public NewIdentityResponse(boolean approved) {
        super("newidentity");
        this.approved = approved ? "true" : "false";
    }
}
