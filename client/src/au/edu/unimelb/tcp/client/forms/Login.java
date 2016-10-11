package au.edu.unimelb.tcp.client.forms;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Login
{
    private JPanel loginPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField hostnameField;
    private JTextField portField;
    private JButton cancelButton;
    private JButton loginButton;

    public Login() {
        cancelButton.addActionListener(e -> {
            FormUtilities.changePanel(loginPanel, new LoginSelection().getLoginSelectionPanel());
        });

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            char[] password = passwordField.getPassword();
            String hostname = hostnameField.getText();
            String port = portField.getText();

            // TODO: Login and check result
            // login(username, password, hostname, port);

            FormUtilities.changePanel(loginPanel, new Chat().getChatPanel());
        });
    }

    public JPanel getLoginPanel() {
        return loginPanel;
    }
}
