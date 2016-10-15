package au.edu.unimelb.tcp.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import au.edu.unimelb.tcp.client.forms.Chat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.*;

public class MessageReceiveThread extends SwingWorker<Void, String>
{

    private SSLSocket socket;
    private State state;
    private boolean debug;

    private BufferedReader in;

    private JSONParser parser = new JSONParser();

    private boolean run = true;

    private Chat chat;

    public MessageReceiveThread(SSLSocket socket, State state, boolean debug, Chat chat) throws IOException {
        this.socket = socket;
        this.state = state;
        this.debug = debug;
        this.chat = chat;
    }

    @Override
    protected Void doInBackground() throws Exception {
        try {
            this.in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), "UTF-8"));
            JSONObject message;

            // Process messages
            while (!isCancelled() && run) {
                message = (JSONObject) parser.parse(in.readLine());
                if (debug) {
                    publish("Receiving: " + message.toJSONString() + '\n');
                    publish("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
                }
                MessageReceive(socket, message);
            }

            // Close socket
            in.close();
            socket.close();
        } catch (ParseException e) {
            publish("Message Error: " + e.getMessage() + '\n');
        } catch (IOException e) {
            publish("Communication Error: " + e.getMessage() + '\n');
        }
        return null;
    }

    @Override
    protected void process(List<String> strings) {
        for (String s : strings) {
            chat.appendText(s);
        }
    }

    public void MessageReceive(SSLSocket socket, JSONObject message)
            throws IOException, ParseException {
        String type = (String) message.get("type");

        // server reply of #newidentity
        if (type.equals("newidentity")) {
            boolean approved = Boolean.parseBoolean((String) message.get("approved"));

            // terminate program if failed
            if (!approved) {
                publish("Authentication failed with username " + state.getIdentity() + '\n');
                run = false;
            }
            return;
        }

        // server reply of #list
        if (type.equals("roomlist")) {
            JSONArray array = (JSONArray) message.get("rooms");
            // print all the rooms
            publish("List of chat rooms:");
            for (int i = 0; i < array.size(); i++) {
                publish(" " + array.get(i));
            }
            publish("\n");
            publish("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
            return;
        }

        // server sends roomchange
        if (type.equals("roomchange")) {

            // identify whether the user has quit!
            if (message.get("roomid").equals("")) {
                // quit initiated by the current client
                if (message.get("identity").equals(state.getIdentity())) {
                    publish(message.get("identity") + " has quit!\n");
                    in.close();
                    run = false;
                } else {
                    publish(message.get("identity") + " has quit!\n");
                    publish("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
                }
            // identify whether the client is new or not
            } else if (message.get("former").equals("")) {
                // change state if it's the current client
                if (message.get("identity").equals(state.getIdentity())) {
                    state.setRoomId((String) message.get("roomid"));
                }
                publish(message.get("identity") + " moves to "
                        + (String) message.get("roomid") + '\n');
                publish("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
            // identify whether roomchange actually happens
            } else if (message.get("former").equals(message.get("roomid"))) {
                publish("room unchanged\n");
                publish("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
            }
            // print the normal roomchange message
            else {
                // change state if it's the current client
                if (message.get("identity").equals(state.getIdentity())) {
                    state.setRoomId((String) message.get("roomid"));
                }

                publish(message.get("identity") + " moves from " + message.get("former") + " to "
                        + message.get("roomid") + '\n');
                publish("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
            }
            return;
        }

        // server reply of #who
        if (type.equals("roomcontents")) {
            JSONArray array = (JSONArray) message.get("identities");
            publish(message.get("roomid") + " contains");
            for (int i = 0; i < array.size(); i++) {
                publish(" " + array.get(i));
                if (message.get("owner").equals(array.get(i))) {
                    publish("*");
                }
            }
            publish("\n");
            publish("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
            return;
        }

        // server forwards message
        if (type.equals("message")) {
            publish(message.get("identity") + ": "
                    + message.get("content") + '\n');
            publish("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
            return;
        }


        // server reply of #createroom
        if (type.equals("createroom")) {
            boolean approved = Boolean.parseBoolean((String)message.get("approved"));
            String temp_room = (String)message.get("roomid");
            if (!approved) {
                publish("Create room " + temp_room + " failed.\n");
                publish("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
            }
            else {
                publish("Room " + temp_room + " is created.\n");
                publish("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
            }
            return;
        }

        // server reply of # deleteroom
        if (type.equals("deleteroom")) {
            boolean approved = Boolean.parseBoolean((String)message.get("approved"));
            String temp_room = (String)message.get("roomid");
            if (!approved) {
                publish("Delete room " + temp_room + " failed.\n");
                publish("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
            }
            else {
                publish("Room " + temp_room + " is deleted.\n");
                publish("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
            }
            return;
        }

        // server directs the client to another server
        if (type.equals("route")) {
            String temp_room = (String)message.get("roomid");
            String host = (String)message.get("host");
            int port = Integer.parseInt((String)message.get("port"));

            // connect to the new server
            if (debug) {
                publish("Connecting to server " + host + ":" + port + '\n');
                publish("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
            }

            SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();

            SSLSocket temp_socket = (SSLSocket)factory.createSocket(host, port);

            // send #movejoin
            DataOutputStream out = new DataOutputStream(temp_socket.getOutputStream());
            JSONObject request = ClientMessages.getMoveJoinRequest(state.getIdentity(), state.getRoomId(), temp_room);
            if (debug) {
                publish("Sending: " + request.toJSONString() + '\n');
                publish("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
            }
            send(out, request);

            // wait to receive serverchange
            BufferedReader temp_in = new BufferedReader(new InputStreamReader(temp_socket.getInputStream()));
            JSONObject obj = (JSONObject) parser.parse(temp_in.readLine());

            if (debug) {
                publish("Receiving: " + obj.toJSONString() + '\n');
                publish("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
            }

            // serverchange received and switch server
            if (obj.get("type").equals("serverchange") && obj.get("approved").equals("true")) {
                chat.switchServer(temp_socket, out);
                switchServer(temp_socket, temp_in);
                String serverid = (String)obj.get("serverid");
                publish(state.getIdentity() + " switches to server " + serverid + '\n');
                publish("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
            }
            // receive invalid message
            else {
                temp_in.close();
                out.close();
                temp_socket.close();
                publish("Server change failed\n");
                publish("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
            }
            return;
        }

        if (debug) {
            publish("Unknown Message: " + message + '\n');
            publish("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
        }
    }

    public void switchServer(SSLSocket temp_socket, BufferedReader temp_in) throws IOException {
        in.close();
        in = temp_in;
        socket.close();
        socket = temp_socket;
    }

    private void send(DataOutputStream out, JSONObject obj) throws IOException {
        out.write((obj.toJSONString() + "\n").getBytes("UTF-8"));
        out.flush();
    }
}
