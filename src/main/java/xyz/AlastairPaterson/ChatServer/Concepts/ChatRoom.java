package xyz.AlastairPaterson.ChatServer.Concepts;

/**
 * Created by atp on 30/08/2016.
 */
public class ChatRoom {
    private String roomId;

    public ChatRoom(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
