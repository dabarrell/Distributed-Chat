package au.edu.unimelb.tcp.client.forms;

import javax.swing.*;

public class FormUtilities
{
    public static void changePanel(JPanel oldPanel, JPanel newPanel) {
        JFrame frame = (JFrame)SwingUtilities.getWindowAncestor(oldPanel);

        // Set the new panel and resize
        frame.setContentPane(newPanel);
        frame.pack();
    }
}
