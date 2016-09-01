package xyz.AlastairPaterson.ChatServer.Exceptions;

/**
 * Thrown if an identity (user or room) is already in use on this server
 */
public class IdentityInUseException extends Exception {
    /**
     * Creates an exception for an identity in use error
     * @param identity The identity in error
     */
    public IdentityInUseException(String identity) {
        super("The identity is in use: " + identity);
    }
}
