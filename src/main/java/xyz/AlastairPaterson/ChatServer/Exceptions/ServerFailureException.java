package xyz.AlastairPaterson.ChatServer.Exceptions;

/**
 * Created by atp on 12/10/16.
 */
public class ServerFailureException extends Exception {
    public ServerFailureException() {
        super("Server failed!");
    }
}
