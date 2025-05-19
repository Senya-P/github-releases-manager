package cz.cuni.mff.releasemanager;

import java.util.Arrays;
import java.util.Scanner;


public class CmdParser {

    private final Scanner scanner = new Scanner(System.in);

    public CmdParser() {
    }

    public Command parse(String[] args) {
        Command defaultCommand = Command.HELP;
        defaultCommand.argument = Arrays.toString(args);

        Command command = Command.fromName(args[0]).orElseGet(() -> defaultCommand);
        if (command.requiresArgument()) {
            command.argument = args[1];
        }
        return command;
    }

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
