package au.edu.unimelb.tcp.client.forms;

import au.edu.unimelb.tcp.client.*;
import org.json.simple.JSONObject;

import javax.net.ssl.SSLSocket;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.ExecutionException;

public class Chat
{
    private JTextArea chatArea;
    private JPanel chatPanel;
    private JButton whoButton;
    private JButton quitButton;
    private JButton listButton;
    private JButton deleteButton;
    private JButton joinButton;
    private JButton createButton;
    private JTextField inputField;
    private JButton sendButton;

    private SSLSocket socket;
    private State state;
    private DataOutputStream out;
    private boolean debug;

    public Chat(JFrame frame, SSLSocket socket, State state, boolean debug) throws IOException {
        this.socket = socket;
        this.state = state;
        this.debug = debug;
        out = new DataOutputStream(socket.getOutputStream());

        // Start processing messages
        SwingWorker<Void, String> receiveThread = new MessageReceiveThread(socket, state, debug, this);
        receiveThread.execute();

        try {
            // Send the #newidentity command
            send(ClientMessages.getNewIdentityRequest(state.getIdentity(), state.getPassword()));
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(FormUtilities.getFrame(chatPanel), e1.getMessage());
        }

        // Button handlers
        whoButton.addActionListener(e -> {
            sendMessage(ClientMessages.getWhoRequest());
        });

        quitButton.addActionListener(e -> {
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        });

        listButton.addActionListener(e -> {
            sendMessage(ClientMessages.getListRequest());
        });

        deleteButton.addActionListener(e -> {
            String room = (String)JOptionPane.showInputDialog(frame,
                    "Room to delete:", "Room Deletion", JOptionPane.PLAIN_MESSAGE);
            if (room != null) {
                sendMessage(ClientMessages.getDeleteRoomRequest(room));
            }
        });

        joinButton.addActionListener(e -> {
            String room = (String)JOptionPane.showInputDialog(frame,
                    "Room to join:", "Room Change", JOptionPane.PLAIN_MESSAGE);
            if (room != null) {
                sendMessage(ClientMessages.getJoinRoomRequest(room));
            }
        });

        createButton.addActionListener(e -> {
            String room = (String)JOptionPane.showInputDialog(frame,
                    "Room to create:", "Room Creation", JOptionPane.PLAIN_MESSAGE);
            if (room != null) {
                sendMessage(ClientMessages.getCreateRoomRequest(room));
            }
        });

        // Send message action
        Action action = new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = inputField.getText();

                appendText(input);
                sendMessage(ClientMessages.getMessage(input));

                inputField.setText("");
                appendText("\n[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
            }
        };
        sendButton.addActionListener(action);
        inputField.addActionListener(action);

        // Window close listener
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    // Send the quit message and wait for it to be processed
                    sendMessage(ClientMessages.getQuitRequest());
                    receiveThread.get();
                } catch(InterruptedException | ExecutionException e1) {
                    e1.printStackTrace();
                }
                super.windowClosing(e);
            }
        });
    }

    public JPanel getChatPanel() {
        return chatPanel;
    }

    /**
     * Append text to the chat room text area.
     * @param text The text to append.
     */
    public synchronized void appendText(String text) {
        chatArea.append(text);
        chatArea.setCaretPosition(chatArea.getText().length() - 1);
    }

    private void send(JSONObject obj) throws IOException {
        if (debug) {
            appendText("Sending: " + obj.toJSONString() + '\n');
            appendText("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
        }
        out.write((obj.toJSONString() + "\n").getBytes("UTF-8"));
        out.flush();
    }

    private void sendMessage(JSONObject message) {
        try {
            send(message);
        } catch (SocketException e) {
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void switchServer(SSLSocket temp_socket, DataOutputStream temp_out) throws IOException {
        // switch server initiated by the receiving thread
        // need to use synchronize
        synchronized(out) {
            out.close();
            out = temp_out;
        }
        socket = temp_socket;
    }
}
