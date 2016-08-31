package xyz.AlastairPaterson.ChatServer;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import xyz.AlastairPaterson.ChatServer.Servers.CoordinationServer;

import javax.naming.ConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        ArgumentParser parser = configureArgumentParser();

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch(ArgumentParserException ex) {
            parser.handleError(ex);
            System.exit(1);
        }

        if(ns.getBoolean("verbose")) {
            Configurator.defaultConfig().level(Level.TRACE).activate();
            Logger.debug("Verbose logging enabled");
        }

        StateManager.getInstance().setThisServerId(ns.getString("name"));
        try {
            processServers(readConfiguration(ns.get("l")));
        } catch(Exception ex) {
            Logger.error(ex.getMessage());
            System.exit(1);
        }
    }

    private static ArgumentParser configureArgumentParser() {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("ChatServer")
                .defaultHelp(true)
                .description("Runs a distributed Chat server");

        parser.addArgument("-v", "--verbose")
                .required(false)
                .action(Arguments.storeTrue())
                .help("enables verbose (debugging) output");

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
     * @param path The path to the configuration file
     * @return A list of configuration entries
     */
    private static List<String> readConfiguration(File path) throws IOException, ConfigurationException {
        FileReader configurationFile = new FileReader(path);
        BufferedReader reader = new BufferedReader(configurationFile);
        String currentLine;
        List<String> returnArray = new ArrayList<>();

        while(true) {
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
     * @param configurationLines A list of tab delimited configuration options
     */
    private static void processServers(List<String> configurationLines) throws Exception {
        Logger.debug("Beginning config processing");

        for (String currentLine: configurationLines) {
            Logger.debug("Adding config: {}", currentLine);
            String[] configuration = currentLine.split("\t");

            boolean isLocalServer = configuration[0].equalsIgnoreCase(StateManager.getInstance().getThisServerId());
            int coordinationPort = Integer.parseInt(configuration[3]);
            int clientPort = Integer.parseInt(configuration[2]);
            String serverName = configuration[0];
            String serverAddress = configuration[1];

            CoordinationServer currentServer = new CoordinationServer(serverName,
                    serverAddress,
                    coordinationPort,
                    clientPort, isLocalServer);

            // Add our found coordination server
            StateManager.getInstance().addServer(currentServer);
        }
        Logger.debug("Config loaded, validating connectivity");

        validateConnectivity();

        Logger.info("All servers reached. Chat service now available");

        Logger.debug("Finished config processing");
    }

    private static void validateConnectivity() throws InterruptedException {
        // Wait one second for servers to start up
        Thread.sleep(1000);
        while(!StateManager.getInstance().getServers().stream().allMatch(CoordinationServer::isConnected)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Can't reach all servers, waiting three seconds before trying again");
            for (CoordinationServer s: StateManager.getInstance().getServers()) {
                sb.append("\nServer ").append(s.getId()).append(" connected: ").append(s.isConnected());
            }
            Logger.warn(sb.toString());
            Thread.sleep(3000);
        }
    }
}
