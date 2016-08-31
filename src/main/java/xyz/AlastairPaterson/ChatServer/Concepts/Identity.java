package xyz.AlastairPaterson.ChatServer.Concepts;

/**
 * Represents a user's identity on this server
 */
public class Identity {
    private String screenName;

    /**
     * Creates a new identity for a user
     * @param screenName The user's screen name
     */
    public Identity(String screenName) {
        this.screenName = screenName;
    }

    /**
     * Get the user's screen name
     * @return The user's screen name
     */
    public String getScreenName() {
        return screenName;
    }

    /**
     * Set the user's screen name
     * @param screenName The user's desired screen name
     */
    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }
}
