package xyz.AlastairPaterson.ChatServer.Servers;

/**
 * Created by atp on 30/08/2016.
 */
public class CoordinationServer {
    private boolean connected = false;

    private int coordinationPort;

    private String hostname;

    private String id;

    private int clientPort;

    public CoordinationServer(String id, String hostname, int coordinationPort, int clientPort, boolean localInstance) {
        this.id = id;
        this.hostname = hostname;
        this.coordinationPort = coordinationPort;
        this.clientPort = clientPort;

        Thread workerThread;

        if(localInstance) {
            // Start server
            workerThread = new Thread();
            connected = true;
        }
        else {
            workerThread = new Thread(this::validateConnectivity);
            workerThread.setName(id + "ConnectionValidator");
        }

        workerThread.start();
    }

    public boolean isConnected() {
        return connected;
    }

    public String getId() {
        return id;
    }

    private void validateConnectivity() {
        connected = true;
    }
}
