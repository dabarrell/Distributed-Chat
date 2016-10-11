package au.edu.unimelb.tcp.client.forms;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class LoginSelection
{
    private JButton googleLoginButton;
    private JButton twitterLoginButton;
    private JButton facebookLoginButton;
    private JButton loginButton;
    private JPanel loginSelectionPanel;

    public LoginSelection() {
        googleLoginButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        twitterLoginButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        facebookLoginButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        loginButton.addActionListener(e -> {
            FormUtilities.changePanel(loginSelectionPanel, new Login().getLoginPanel());
        });
    }

    public JPanel getLoginSelectionPanel() {
        return loginSelectionPanel;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chat Client");
        frame.setContentPane(new LoginSelection().loginSelectionPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
