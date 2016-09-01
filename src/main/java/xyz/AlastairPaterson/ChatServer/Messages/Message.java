package xyz.AlastairPaterson.ChatServer.Messages;

/**
 * The superclass for messages. Should not be initialized directly
 */
public class Message {
    private final String type;

    /**
     * The type of message being send (from specification)
     * @param type The message type
     */
    public Message(String type) {
        this.type = type;
    }

    /**
     * Gets the type of message being sent
     * @return The message type
     */
    public String getType() {
        return type;
    }
}
