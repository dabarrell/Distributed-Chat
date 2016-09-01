package xyz.AlastairPaterson.ChatServer.Concepts;

/**
 * Represents a lock on a proposed new object
 */
public class EntityLock {
    private final String identity;

    private final String lockingServer;

    private final LockType lockType;

    /**
     * Creates a new EntityLock
     * @param identity The object being locked
     * @param lockingServer The server requesting the lock
     */
    public EntityLock(String identity, String lockingServer, LockType lockType) {
        this.identity = identity;
        this.lockingServer = lockingServer;
        this.lockType = lockType;
    }

    /**
     * Gets the identity being locked
     * @return The identity currently locked
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * Gets the lock type
     * @return The type of lock
     */
    public LockType getLockType() {
        return lockType;
    }

    /**
     * Gets the server requesting the lock
     * @return The requesting server ID
     */
    public String getLockingServer() {
        return lockingServer;
    }

    /**
     * Determines if an identity is locked by this lock
     * @param identity The identity being requested
     * @param lockType The type of lock
     * @return True if the two are equal, false otherwise
     */
    public boolean isLocked(String identity, LockType lockType) {
        return identity.equalsIgnoreCase(this.identity)
                && lockType == this.lockType;
    }

    /**
     * Determines if two EntityLocks are equal
     * @param obj The second EntityLock
     * @return True if they are equivalent, false otherwise
     */
    public boolean equals(EntityLock obj) {
        return identity.equalsIgnoreCase(obj.identity)
                && lockingServer.equalsIgnoreCase(obj.lockingServer)
                && this.lockType.equals(obj.lockType);
    }
}
