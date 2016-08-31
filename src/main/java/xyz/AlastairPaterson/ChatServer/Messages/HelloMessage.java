package xyz.AlastairPaterson.ChatServer.Messages;

/**
 * Sent from one server to another to validate connectivity
 */
public class HelloMessage extends Message {
    public HelloMessage() {
        super("hello");
    }
}
