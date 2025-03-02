package cz.cuni.mff.releasemanager;

public enum Command { //iface?
    SEARCH("search"),
    INSTALL("install"),
    UPDATE("update"),
    LIST("list"),
    HELP("help");

    Command(String command) {
        this.commandName = command;
    }
    public final String commandName;
    public String argument;
}
