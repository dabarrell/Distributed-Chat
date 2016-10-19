package xyz.AlastairPaterson.ChatServer.Messages.NewServer;

import xyz.AlastairPaterson.ChatServer.Messages.Message;

/**
 * Request from a new server to be added to system
 */
public class GlobalReleaseMessage extends Message {
    private String serverId;
    private String approved;

    /**
     * Creates a new message
     * @param serverId
     * @param isApproved
     */
    public GlobalReleaseMessage(String serverId, Boolean isApproved) {
        super("globalrelease");
        this.serverId = serverId;
        this.approved = isApproved ? "true" : "false";
    }

    /**
     * Gets serverId
     * @return the id of the sending server
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * Sets approved
     * @param approved True if request approved, false otherwise
     */
    public void setApproved(boolean approved) {
        this.approved = approved ? "true" : "false";
    }

    /**
     * Whether or not the request is approved
     * @return True if request approved, false otherwise
     */
    public boolean isApproved() {
        return approved.equalsIgnoreCase("true");
    }

}
