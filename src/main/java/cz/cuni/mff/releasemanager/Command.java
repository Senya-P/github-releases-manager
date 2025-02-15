package cz.cuni.mff.releasemanager;

public enum Command { //iface?
    SEARCH("search"),
    INSTALL("install"),
    UPDATE("update"),
    LIST("list");

    Command(String command) {
        this.commandName = command;
    }
    private final String commandName;
}
