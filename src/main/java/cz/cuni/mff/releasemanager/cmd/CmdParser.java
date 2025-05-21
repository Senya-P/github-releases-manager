package cz.cuni.mff.releasemanager.cmd;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Command line parser for the application.
 * This class is responsible for parsing command line arguments and user input.
 */
public class CmdParser {

    private final Scanner scanner = new Scanner(System.in);

    /**
     * Parses the command line arguments and returns the corresponding Command.
     *
     * @param args command line arguments
     * @return the parsed Command
     */
    public Command parse(String[] args) {
        Command defaultCommand = Command.HELP;
        defaultCommand.argument = null;
        if (args.length > 1) {
            defaultCommand.argument =  String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        }
        if (args.length == 0) {
            return defaultCommand;
        }
        Command command = Command.fromName(args[0]).orElseGet(() -> defaultCommand);
        if (command.requiresArgument()) {
            command.argument = args[1];
        }
        return command;
    }

    /**
     * Prompts the user for input and returns the input as an integer.
     * This method handles invalid input by recursively calling itself until an integer is entered.
     * @return the user input as an integer
     */
    public int getUserInput() {
        try {
            String input = scanner.nextLine();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return getUserInput();
        }
    }

}
