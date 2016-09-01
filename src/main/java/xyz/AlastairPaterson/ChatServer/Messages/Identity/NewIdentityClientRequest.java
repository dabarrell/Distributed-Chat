package xyz.AlastairPaterson.ChatServer.Messages.Identity;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * Created by atp on 31/08/2016.
 */
public class NewIdentityClientRequest extends Message {
    private String identity;

    public NewIdentityClientRequest() {
        super("newidentity");
    }

    public String getIdentity() {
        return identity;
    }
}
