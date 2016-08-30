package xyz.AlastairPaterson.ChatServer;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;

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
    }

    private static ArgumentParser configureArgumentParser() {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("ChatServer")
                .defaultHelp(true)
                .description("Runs a distributed Chat server");

        parser.addArgument("-v", "--verbose")
                .required(false)
                .action(Arguments.storeTrue())
                .help("Enables verbose (debugging) output");

        parser.addArgument("-n", "--name")
                .required(true)
                .type(String.class)
                .help("The name of the server");

        parser.addArgument("-l", "--configuration")
                .required(true)
                .type(File.class)
                .help("The path to the configuration file");
        return parser;
    }
}
