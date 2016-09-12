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
     *
     * @param socket  The destination socket
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
     *
     * @param socket The source socket
     * @return The string that is read
     * @throws IOException Thrown if reading fails
     */
    static String readFromSocket(Socket socket) throws IOException {
        return readFromSocket(socket.getInputStream());
    }

    /**
     * Reads a string from an input stream
     *
     * @param stream The stream being read
     * @return The string that is read
     * @throws IOException Thrown if reading fails
     */
    private static String readFromSocket(InputStream stream) throws IOException {
        BufferedReader socketReader = new BufferedReader(new InputStreamReader(stream));
        String readData = socketReader.readLine();

        Logger.debug("Read {}", readData);
        return readData;
    }
}
