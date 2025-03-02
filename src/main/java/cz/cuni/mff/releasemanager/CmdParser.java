package cz.cuni.mff.releasemanager;


public class CmdParser {

    public CmdParser() {
    }

    public Command parse(String[] args) {
        if (args.length == 1) {
            if (args[0].equals("list")) {
                return Command.LIST;
            }
            else if (args[0].equals("help")) {
                return Command.HELP;
            }
            else return Command.HELP;
        }
        else if (args.length == 2) {
            if (args[0].equals("search")) {
                Command command = Command.SEARCH;
                command.argument = args[1];
                return command;
            }
            else if (args[0].equals("install")) {
                Command command = Command.INSTALL;
                command.argument = args[1];
                return command;
            }
            else if (args[0].equals("update")) {
                Command command = Command.UPDATE;
                command.argument = args[1];
                return command;
            }
            else return Command.HELP;
        }
        return Command.HELP;
    }

}
