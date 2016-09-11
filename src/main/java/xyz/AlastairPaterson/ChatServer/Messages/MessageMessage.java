package xyz.AlastairPaterson.ChatServer.Messages;

/**
 * Created by atp on 5/09/2016.
 */
public class MessageMessage extends Message {
    private String content;
    private String identity;

    public MessageMessage() {
        super("message");
    }

    public String getIdentity() {
        return identity;
    }

    public String getContent() {
        return content;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
