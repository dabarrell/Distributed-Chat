package xyz.AlastairPaterson.ChatServer.Exceptions;

/**
 * Created by atp on 30/08/2016.
 */
public class IdentityInUseException extends Exception {
    public IdentityInUseException(String identity) {
        super("The identity is in use: " + identity);
    }
}
