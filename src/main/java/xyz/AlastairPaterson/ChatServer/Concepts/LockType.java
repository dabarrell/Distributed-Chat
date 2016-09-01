package xyz.AlastairPaterson.ChatServer.Concepts;

/**
 * Represents types of locked distributed objects
 */
public enum LockType {
    /**
     * Locks an identity
     */
    IdentityLock,
    /**
     * Locks a room
     */
    RoomLock
}
