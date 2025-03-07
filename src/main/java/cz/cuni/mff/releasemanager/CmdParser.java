package cz.cuni.mff.releasemanager;

import java.util.Arrays;


public class CmdParser {

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

}
