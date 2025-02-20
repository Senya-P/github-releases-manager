package cz.cuni.mff.releasemanager;

public class Platform {
    public enum OS {
        WINDOWS, LINUX, MAC
    }

    public enum Architecture {
        X86_32, X86_64, ARM_32, ARM_64
    }

    private static Platform instance;

    private final OS os;
    private final Architecture arch;

    private Platform() {
        this.os = detectOS();
        this.arch = detectArch();
    }

    private static OS detectOS() {
        final String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return OS.WINDOWS;
        } else if (os.contains("linux") || os.contains("nix")) {
            return OS.LINUX;
        } else if (os.contains("mac")) {
            return OS.MAC;
        } else {
            throw new IllegalStateException("Unsupported OS: " + os);
        }
    }

    private static Architecture detectArch() {
        final String architecture = System.getProperty("os.arch").toLowerCase();
        if (architecture.contains("x86_64") || architecture.contains("amd64")) {
            return Architecture.X86_64; 
        }
        else if (architecture.contains("x86")) {
            return Architecture.X86_32;
        } else if (architecture.contains("arm")) {
            return Architecture.ARM_32;
        } else if (architecture.contains("aarch64")) {
            return Architecture.ARM_64;
        } else {
            throw new IllegalStateException("Unsupported architecture: " + architecture);
        }
    }

    public OS getOS() {
        return this.os;
    }

    public Architecture getArchitecture() {
        return this.arch;
    }

    public static Platform detectPlatform() {
        if (instance == null) {
            instance = new Platform();
        }
        return instance;
    }

    public static PlatformHandler getPlatformHandler() {
        if (instance == null) {
            instance = new Platform();
        }
        switch (instance.getOS()) {
            case WINDOWS -> {
                return new WindowsHandler();
            }
            case LINUX -> {
                return new LinuxHandler();
            }
            case MAC -> {
                return new MacHandler();
            }
            default -> throw new IllegalStateException("Unsupported OS: " + instance.getOS());
        }
    }
}
