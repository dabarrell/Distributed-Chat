package au.edu.unimelb.tcp.client.forms;

import javax.swing.*;

public class FormUtilities
{
    /**
     * Change the panel of the current frame.
     * @param oldPanel The old panel.
     * @param newPanel The new panel.
     */
    public static void changePanel(JPanel oldPanel, JPanel newPanel) {
        JFrame frame = getFrame(oldPanel);

        // Set the new panel and resize
        frame.setContentPane(newPanel);
        frame.pack();
    }

    /**
     * Get the frame of a JPanel.
     * @param panel The panel whose frame we want.
     * @return
     */
    public static JFrame getFrame(JPanel panel) {
        return (JFrame)SwingUtilities.getWindowAncestor(panel);
    }
}
