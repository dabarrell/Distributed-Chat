package au.edu.unimelb.tcp.client.forms;

import au.edu.unimelb.tcp.client.*;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.json.simple.JSONObject;

import javax.net.ssl.SSLSocket;
import javax.swing.*;
import java.awt.*;
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
            String room = (String) JOptionPane.showInputDialog(frame,
                    "Room to delete:", "Room Deletion", JOptionPane.PLAIN_MESSAGE);
            if (room != null) {
                sendMessage(ClientMessages.getDeleteRoomRequest(room));
            }
        });

        joinButton.addActionListener(e -> {
            String room = (String) JOptionPane.showInputDialog(frame,
                    "Room to join:", "Room Change", JOptionPane.PLAIN_MESSAGE);
            if (room != null) {
                sendMessage(ClientMessages.getJoinRoomRequest(room));
            }
        });

        createButton.addActionListener(e -> {
            String room = (String) JOptionPane.showInputDialog(frame,
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
                } catch (InterruptedException | ExecutionException e1) {
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
     *
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
        synchronized (out) {
            out.close();
            out = temp_out;
        }
        socket = temp_socket;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        chatPanel = new JPanel();
        chatPanel.setLayout(new GridLayoutManager(10, 4, new Insets(0, 0, 0, 0), -1, -1));
        final Spacer spacer1 = new Spacer();
        chatPanel.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 25), null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        chatPanel.add(spacer2, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 25), null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        chatPanel.add(spacer3, new GridConstraints(0, 0, 10, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, new Dimension(20, -1), null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        chatPanel.add(spacer4, new GridConstraints(1, 3, 9, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, new Dimension(20, -1), null, null, 0, false));
        whoButton = new JButton();
        whoButton.setText("Who");
        chatPanel.add(whoButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        quitButton = new JButton();
        quitButton.setText("Quit");
        chatPanel.add(quitButton, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        listButton = new JButton();
        listButton.setText("List");
        chatPanel.add(listButton, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deleteButton = new JButton();
        deleteButton.setText("Delete Room");
        chatPanel.add(deleteButton, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        joinButton = new JButton();
        joinButton.setText("Join Room");
        chatPanel.add(joinButton, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createButton = new JButton();
        createButton.setText("Create Room");
        chatPanel.add(createButton, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        inputField = new JTextField();
        chatPanel.add(inputField, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(500, -1), null, null, 0, false));
        sendButton = new JButton();
        sendButton.setText("Send");
        chatPanel.add(sendButton, new GridConstraints(8, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        chatPanel.add(spacer5, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        chatPanel.add(scrollPane1, new GridConstraints(1, 1, 7, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(500, 500), null, 0, false));
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        scrollPane1.setViewportView(chatArea);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return chatPanel;
    }
}
