package cz.cuni.mff.releasemanager;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Command {
    SEARCH("search", 0),
    INSTALL("install", 1),
    UPDATE("update", 1),
    LIST("list", 0),
    HELP("help", 0);

    Command(String command, int argNumber) {
        this.commandName = command;
        this.argNumber = argNumber;
    }
    public final String commandName;
    public String argument;
    public int argNumber;

    private static final Map<String, Command> COMMAND_MAP = Arrays.stream(values())
        .collect(Collectors.toMap(c -> c.commandName, Function.identity()));

    public static Optional<Command> fromName(String name) {
        return Optional.ofNullable(COMMAND_MAP.get(name.toLowerCase()));
    }

    public boolean requiresArgument() {
        return argNumber == 1;
    }
}
