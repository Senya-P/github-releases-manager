package cz.cuni.mff.releasemanager;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Enum representing the available commands for the release manager.
 * Each command has a name and an argument count.
 */
public enum Command {
    SEARCH("search", 1),
    INSTALL("install", 1),
    UNINSTALL("uninstall", 1),
    UPDATE("update", 1),
    LIST("list", 0),
    HELP("help", 0);

    /**
     * Constructor for the Command enum.
     * @param command the name of the command
     * @param argNumber the number of arguments required by the command
     */
    Command(String command, int argNumber) {
        this.commandName = command;
        this.argNumber = argNumber;
    }
    public final String commandName;
    public String argument;
    public int argNumber;

    /**
     * A map of command names to their corresponding Command enum values.
     * This is used for quick lookup of commands by name.
     */
    private static final Map<String, Command> COMMAND_MAP = Arrays.stream(values())
        .collect(Collectors.toMap(c -> c.commandName, Function.identity()));

    /**
     * Returns the Command enum value corresponding to the given name.
     * The name is converted to lowercase to ensure case-insensitive matching.
     * @param name the name of the command
     * @return an Optional containing the Command enum value if found
     */
    public static Optional<Command> fromName(String name) {
        return Optional.ofNullable(COMMAND_MAP.get(name.toLowerCase()));
    }

    /**
     * Checks if the command requires a single argument.
     * @return true if the command requires a single argument, false otherwise.
     */
    public boolean requiresArgument() {
        return argNumber == 1;
    }
}
