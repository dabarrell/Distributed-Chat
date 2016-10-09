package xyz.AlastairPaterson.ChatServer.Servers;

import org.pmw.tinylog.Logger;
import sun.security.ssl.SSLSocketFactoryImpl;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;

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
    static void writeToSocket(SSLSocket socket, Object content) throws IOException {
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
    static String readFromSocket(SSLSocket socket) throws IOException {
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

    /**
     * Creates a new SSL socket
     *
     * @param serverName The name of the host
     * @param port The port of the host
     * @return A new SSL socket
     * @throws Exception Exception
     */
    static SSLSocket buildClientSocket(String serverName, int port) throws Exception {
        SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
        return (SSLSocket)factory.createSocket(serverName, port);
    }

    /**
     * Creates a new SSL server socket
     *
     * @param port The port the server will listen on
     * @return A new SSL server socket
     * @throws Exception Exception
     */
    static SSLServerSocket buildServerSocket(int port) throws Exception {
        return (SSLServerSocket)SSLServerSocketFactory.getDefault().createServerSocket(port);
    }
}
