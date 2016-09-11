package xyz.AlastairPaterson.ChatServer.Messages.Room;

import xyz.AlastairPaterson.ChatServer.Messages.Message;
import xyz.AlastairPaterson.ChatServer.StateManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by atp on 12/9/16.
 */
public class ListClientResponse extends Message {
    private final List<String> rooms = new ArrayList<>();

    public ListClientResponse() {
        super("roomlist");

        StateManager.getInstance().getRooms().forEach(x -> rooms.add(x.getRoomId()));
    }
}
