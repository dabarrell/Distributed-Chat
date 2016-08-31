package xyz.AlastairPaterson.ChatServer.Servers;

import org.pmw.tinylog.Logger;

import java.io.*;
import java.net.Socket;

/**
 * Created by atp on 31/08/2016.
 */
public class SocketServices {
    public static void writeToSocket(Socket socket, Object content) throws IOException {
        BufferedWriter socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        Logger.debug("Writing {} to {}", content.toString(), socket.getInetAddress().toString());

        socketWriter.write(content.toString());
        socketWriter.write('\n');
        socketWriter.flush();
    }

    public static String readFromSocket(Socket socket) throws IOException {
        BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String readData = socketReader.readLine();

        Logger.debug("Read {} from {}", readData, socket.getInetAddress().toString());
        return readData;
    }
}
