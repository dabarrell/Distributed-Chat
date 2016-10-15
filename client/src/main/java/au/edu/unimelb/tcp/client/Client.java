package au.edu.unimelb.tcp.client;

import java.io.IOException;
import java.net.UnknownHostException;

import au.edu.unimelb.tcp.client.forms.LoginSelection;
import org.json.simple.parser.ParseException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.*;

public class Client {

    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.trustStore", System.getProperty("user.dir") + "/cacerts.jks");

        // Start the GUI
        JFrame frame = new JFrame("Chat Client");
        frame.setContentPane(new LoginSelection().getLoginSelectionPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
