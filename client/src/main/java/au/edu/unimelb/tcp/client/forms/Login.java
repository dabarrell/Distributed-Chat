package au.edu.unimelb.tcp.client.forms;

import au.edu.unimelb.tcp.client.State;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.*;
import java.io.IOException;

public class Login
{
    private JPanel loginPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField hostnameField;
    private JTextField portField;
    private JButton cancelButton;
    private JButton loginButton;
    private JCheckBox debugCheckBox;

    public Login() {
        cancelButton.addActionListener(e -> {
            FormUtilities.changePanel(loginPanel, new LoginSelection().getLoginSelectionPanel());
        });

        loginButton.addActionListener(e -> {
            try {
                // Grab the input
                String username = usernameField.getText();
                char[] password = passwordField.getPassword();
                String hostname = hostnameField.getText();
                int port = Integer.parseUnsignedInt(portField.getText());
                boolean debug = debugCheckBox.isSelected();

                // Connect to the server
                SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
                SSLSocket socket = (SSLSocket)factory.createSocket(hostname, port);

                // Initialise the state
                State state = new State(username, String.valueOf(password), "");

                // Show the chat room
                JFrame frame = FormUtilities.getFrame(loginPanel);
                FormUtilities.changePanel(loginPanel, new Chat(frame, socket, state, debug).getChatPanel());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(FormUtilities.getFrame(loginPanel), "Invalid port.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(FormUtilities.getFrame(loginPanel), "Communication problem.");
            }
        });
    }

    public JPanel getLoginPanel() {
        return loginPanel;
    }
}
