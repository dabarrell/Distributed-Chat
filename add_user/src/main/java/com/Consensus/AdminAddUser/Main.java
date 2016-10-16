package com.Consensus.AdminAddUser;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.apache.commons.csv.*;
import java.nio.charset.Charset;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;

import javax.naming.ConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static final List<RootServer> rootServers = new ArrayList<>();

    /**
     * Where all the magic happens
     *
     * @param args Arguments
     */
    public static void main(String[] args) {
        ArgumentParser parser = configureArgumentParser();


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

        configureSSL();

        try {
            if (!new File("../servers_remote.conf").exists()) {
                Logger.error("Could not find conf at {}. Check file exists and create if required", System.getProperty("user.dir"));
                System.exit(1);
            }
            Logger.info("Parsing config");
            parseServerConfig(arguments.get("l"));
        } catch (Exception ex) {
            Logger.error(ex.getMessage());
            System.exit(1);
        }

        try{
          SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
          for( RootServer server : rootServers){
            SSLSocket remoteServer = (SSLSocket)factory.createSocket(server.address, server.port);
            writeToSocket(remoteServer,  addMessage(arguments.get("i"), arguments.get("p")));
          }
        }catch(Exception e){
          Logger.error(e);
        }

    }


    /**
     * Writes an object to a socket
     *
     * @param socket  The destination socket
     * @param content The item to be written
     * @throws IOException Thrown if writing fails
     */
    static void writeToSocket(SSLSocket socket, String content) throws IOException {
        BufferedWriter socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        Logger.debug("Writing {} to {}", content , socket.getInetAddress().toString());

        socketWriter.write(content);
        socketWriter.flush();
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

    }

    /**
     * Configures an argument parser
     *
     * @return The configured argument parser
     */
    private static ArgumentParser configureArgumentParser() {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("AdminAddUser")
                .defaultHelp(true)
                .description("Used by admins to add new registered users.");

        parser.addArgument("-v", "--verbose")
                .required(false)
                .action(Arguments.storeTrue())
                .help("enables verbose (debugging) output");

        parser.addArgument("-l")
                .required(true)
                .type(File.class)
                .help("path to the configuration file");

        parser.addArgument("-i")
                .required(true)
                .help("name of identity to register");

        parser.addArgument("-p")
                .required(true)
                .help("users password");

        return parser;
    }

    /**
     * Reads the configuration for the environment
     *
     * @param serverConf The configuration file
     * @return A list of configuration entries
     */
    private static void parseServerConfig(File serverConf) throws IOException, ConfigurationException {
        try{
           CSVParser parser = CSVParser.parse(serverConf, Charset.defaultCharset(), CSVFormat.TDF);
           Logger.info("Parsing config ..");
           for (CSVRecord csvRecord : parser) {
             Logger.info("Adding server {}", csvRecord.get(1));
             rootServers.add(new RootServer(csvRecord.get(1), Integer.parseInt(csvRecord.get(3))));
          }
        }
        catch(IOException e) {
          Logger.error("Error while reading CSV : "+e.getMessage());
        }
    }

    /**
     * create message for new user
     */
    private static String addMessage(String id, String password){
      JSONObject object = new JSONObject();
      object.put("type", "addRegisteredUser");
      object.put("identity", id);
      object.put("password", password);
      return (object.toString() + "\n");
    }


}

class RootServer {

  public final String address;
  public final int port;

  public RootServer(String address, int port){
    this.address = address;
    this.port = port;
  }

}
