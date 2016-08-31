package xyz.AlastairPaterson.ChatServer.Messages;

/**
 * Created by atp on 31/08/2016.
 */
public class Message {
    private String type;

    public Message(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
