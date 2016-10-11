package au.edu.unimelb.tcp.client.forms;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

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

    public Chat() {
        whoButton.addActionListener(e -> {

        });

        quitButton.addActionListener(e -> {
            JFrame frame = (JFrame)SwingUtilities.getWindowAncestor(chatPanel);
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        });

        listButton.addActionListener(e -> {

        });

        deleteButton.addActionListener(e -> {

        });

        joinButton.addActionListener(e -> {

        });

        createButton.addActionListener(e -> {

        });

        sendButton.addActionListener(e -> {
            String input = inputField.getText();
        });
    }

    public JPanel getChatPanel() {
        return chatPanel;
    }
}
