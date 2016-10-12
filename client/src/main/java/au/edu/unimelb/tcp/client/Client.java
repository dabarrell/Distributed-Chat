package au.edu.unimelb.tcp.client;

import java.io.IOException;
import java.net.UnknownHostException;

import org.json.simple.parser.ParseException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Client {

	public static void main(String[] args) throws IOException, ParseException {
		System.setProperty("javax.net.ssl.trustStore", System.getProperty("user.dir") + "/cacerts.jks");

		SSLSocket socket = null;
		String identity = null;
		boolean debug = false;
		try {
			//load command line args
			ComLineValues values = new ComLineValues();
			CmdLineParser parser = new CmdLineParser(values);
			try {
				parser.parseArgument(args);
				String hostname = values.getHost();
				identity = values.getIdeneity();
				int port = values.getPort();
				debug = values.isDebug();
				SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
				socket = (SSLSocket)factory.createSocket(hostname, port);
			} catch (CmdLineException e) {
				e.printStackTrace();
			}
			
			State state = new State(identity, "");
			
			// start sending thread
			MessageSendThread messageSendThread = new MessageSendThread(socket, state, debug);
			Thread sendThread = new Thread(messageSendThread);
			sendThread.start();
			
			// start receiving thread
			Thread receiveThread = new Thread(new MessageReceiveThread(socket, state, messageSendThread, debug));
			receiveThread.start();
			
		} catch (UnknownHostException e) {
			System.out.println("Unknown host");
		} catch (IOException e) {
			System.out.println("Communication Error: " + e.getMessage());
		}
	}
}
