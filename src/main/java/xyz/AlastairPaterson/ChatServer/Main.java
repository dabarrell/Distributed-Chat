package xyz.AlastairPaterson.ChatServer;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.apache.commons.csv.*;
import java.nio.charset.Charset;

import xyz.AlastairPaterson.ChatServer.Messages.NewServer.NewServerRequestMessage;
import xyz.AlastairPaterson.ChatServer.Servers.CoordinationServer;

import javax.naming.ConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    /**
     * Where all the magic happens
     *
     * @param args Arguments
     */
    public static void main(String[] args) {
        ArgumentParser parser = configureArgumentParser();
        parseRegisteredUsers();

        Namespace arguments = null;
        try {
            arguments = parser.parseArgs(args);
        } catch (ArgumentParserException ex) {
            parser.handleError(ex);
            System.exit(1);
        }

        if (arguments.getBoolean("verbose")) {
            Configurator.defaultConfig().level(Level.TRACE).activate();
            Logger.debug("Verbose logging enabled");
        }

        StateManager.getInstance().setThisServerId(arguments.getString("name"));

        configureSSL();

        try {
            processServers(readConfiguration(arguments.get("l")), arguments.getBoolean("add"));
        } catch (Exception ex) {
            Logger.error(ex.getMessage());
            System.exit(1);
        }


        Logger.info("Chat server now available");
    }

    private static void configureSSL() {
        //Security.addProvider(new Provider());

        //Specifying the Keystore details
        Logger.debug("Reading keystore at {}", System.getProperty("user.dir") + "/keystore.ks");
        if (!new File(System.getProperty("user.dir") + "/keystore.ks").exists()) {
            Logger.error("Could not find keystore.ks at {}. Check file exists and create if required", System.getProperty("user.dir"));
            System.exit(1);
        }

        System.setProperty("javax.net.ssl.keyStore", System.getProperty("user.dir") + "/keystore.ks");
        System.setProperty("javax.net.ssl.keyStorePassword","comp90015");
        System.setProperty("javax.net.ssl.trustStore", System.getProperty("user.dir") + "/cacerts.jks");

        // Enable debugging to view the handshake and communication which happens between the SSLClient and the SSLServer
        // System.setProperty("javax.net.debug","all");

    }

    /**
     * Configures an argument parser
     *
     * @return The configured argument parser
     */
    private static ArgumentParser configureArgumentParser() {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("ChatServer")
                .defaultHelp(true)
                .description("Runs a distributed Chat server");

        parser.addArgument("-v", "--verbose")
                .required(false)
                .action(Arguments.storeTrue())
                .help("enables verbose (debugging) output");

        parser.addArgument("-a", "--add")
                .required(false)
                .action(Arguments.storeTrue())
                .help("starts server as an addition to system");

        parser.addArgument("-n", "--name")
                .required(true)
                .type(String.class)
                .help("name of the server");

        parser.addArgument("-l")
                .required(true)
                .type(File.class)
                .help("path to the configuration file");
        return parser;
    }

    /**
     * Reads the configuration for the environment
     *
     * @param path The path to the configuration file
     * @return A list of configuration entries
     */
    private static List<String> readConfiguration(File path) throws IOException, ConfigurationException {
        FileReader configurationFile = new FileReader(path);
        BufferedReader reader = new BufferedReader(configurationFile);
        String currentLine;
        List<String> returnArray = new ArrayList<>();

        while (true) {
            currentLine = reader.readLine();
            if (currentLine == null) break;
            returnArray.add(currentLine);
        }

        if (returnArray.size() == 0) {
            throw new ConfigurationException("Configuration file empty");
        }

        return returnArray;
    }



    /**
     * Processes configuration to learn about other servers
     *
     * @param configurationLines A list of tab delimited configuration options
     */
    private static void processServers(List<String> configurationLines, Boolean additional) throws Exception {
        Logger.debug("Beginning config processing");

        for (String currentLine : configurationLines) {
            Logger.debug("Adding config: {}", currentLine);
            String[] configuration = currentLine.split("\t");

            boolean isLocalServer = configuration[0].equalsIgnoreCase(StateManager.getInstance().getThisServerId());
            int coordinationPort = Integer.parseInt(configuration[3]);
            int clientPort = Integer.parseInt(configuration[2]);
            int heartbeatPort = Integer.parseInt(configuration[4]);
            int userAdditionPort = Integer.parseInt(configuration[5]);
            String serverName = configuration[0];
            String serverAddress = configuration[1];

            CoordinationServer currentServer = new CoordinationServer(serverName,
                    serverAddress,
                    coordinationPort,
                    clientPort,
                    heartbeatPort,
                    userAdditionPort,
                    isLocalServer);

            // Add our found coordination server
            if (StateManager.getInstance().addServer(currentServer)) {
                currentServer.begin();
            }
        }

        Logger.debug("Config loaded, validating connectivity");

        validateConnectivity();

        if (additional) {
            processAdditional();
        } else {
            StateManager.getInstance().getThisCoordinationServer().finishLoad();
        }

    }

    private static void processAdditional() throws Exception {
        String serverId = StateManager.getInstance().getThisServerId();
        String hostname = StateManager.getInstance().getThisCoordinationServer().getHostname();
        int coordPort = StateManager.getInstance().getThisCoordinationServer().getCoordinationPort();
        int clientPort = StateManager.getInstance().getThisCoordinationServer().getClientPort();
        int heartbeatPort = StateManager.getInstance().getThisCoordinationServer().getHeartbeatPort();
        int userAdditionPort = StateManager.getInstance().getThisCoordinationServer().getUserAdditionPort();

        NewServerRequestMessage newServerRequestMessage = new NewServerRequestMessage(serverId, hostname, coordPort, clientPort, heartbeatPort, userAdditionPort);

        for (CoordinationServer server : StateManager.getInstance().getServers()) {
            if (server.equals(StateManager.getInstance().getThisCoordinationServer())) {
                continue;
            }

            server.sendMessage(newServerRequestMessage);

            break;
        }
    }

    /**
     * Validates connectivity to other configured servers
     *
     * @throws InterruptedException If the thread checking connectivity is interrupted
     */
    private static void validateConnectivity() throws InterruptedException {
        // Wait one second for servers to start up
        Thread.sleep(1000);
        while (!StateManager.getInstance().getServers().stream().allMatch(CoordinationServer::isConnected)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Can't reach all servers, waiting three seconds before trying again");
            for (CoordinationServer s : StateManager.getInstance().getServers()) {
                sb.append("\nServer ").append(s.getId()).append(" connected: ").append(s.isConnected());
            }
            Logger.warn(sb.toString());
            Thread.sleep(3000);
        }

        StateManager.getInstance().getThisCoordinationServer().startHeartbeatServer();
    }

    /**
     * Parses the registered users
     */
    private static void parseRegisteredUsers() {

      try{
         File registered_user_file = new File("registered_users.txt");
         CSVParser parser = CSVParser.parse(registered_user_file, Charset.defaultCharset(), CSVFormat.DEFAULT);
         for (CSVRecord csvRecord : parser) {
           StateManager.getInstance().addRegisteredUser(csvRecord.get(0), csvRecord.get(1));
        }
      }
      catch(IOException e) {
        System.out.println("Error while reading CSV : "+e.getMessage());
      }

    }

}
