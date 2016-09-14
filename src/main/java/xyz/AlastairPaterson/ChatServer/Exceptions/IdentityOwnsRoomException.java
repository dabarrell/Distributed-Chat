package xyz.AlastairPaterson.ChatServer.Exceptions;

/**
 * Created by atp on 14/9/16.
 */
public class IdentityOwnsRoomException extends Exception {
    public IdentityOwnsRoomException() {
        super("This identity owns a chat room");
    }
}
