package xyz.AlastairPaterson.ChatServer.Messages.Identity;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * Created by atp on 31/08/2016.
 */
public class NewIdentityClientResponse extends Message {
    private String approved;

    public NewIdentityClientResponse(boolean approved) {
        super("newidentity");
        this.approved = approved ? "true" : "false";
    }
}
