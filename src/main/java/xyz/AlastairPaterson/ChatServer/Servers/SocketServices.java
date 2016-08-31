package xyz.AlastairPaterson.ChatServer.Servers;

import org.pmw.tinylog.Logger;

import java.io.*;
import java.net.Socket;

/**
 * Provides basic services for reading from/writing to sockets
 */
class SocketServices {
    /**
     * Writes an object to a socket
     * @param socket The destination socket
     * @param content The item to be written
     * @throws IOException Thrown if writing fails
     */
     static void writeToSocket(Socket socket, Object content) throws IOException {
        BufferedWriter socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        Logger.debug("Writing {} to {}", content.toString(), socket.getInetAddress().toString());

        socketWriter.write(content.toString());
        socketWriter.write('\n');
        socketWriter.flush();
    }

    /**
     * Reads a string from a socket
     * @param socket The source socket
     * @return The string that is read
     * @throws IOException Thrown if reading fails
     */
    static String readFromSocket(Socket socket) throws IOException {
        BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String readData = socketReader.readLine();

        Logger.debug("Read {} from {}", readData, socket.getInetAddress().toString());
        return readData;
    }
}
