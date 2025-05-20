package cz.cuni.mff.releasemanager.platform;

/**
 * This class is used to detect the current operating system and architecture.
 */
public final class Platform {
    /**
     * Enum representing the supported operating systems.
     */
    public enum OS {
        WINDOWS, LINUX, MAC
    }
    /**
     * ARCHITECTURE CHECK IS NOT YET SUPPORTED
     * 
     * Enum representing the supported architectures.
     */
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

    /**
     * Detects the current operating system.
     * @return the detected OS
     */
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

    /**
     * ARCHITECTURE CHECK IS NOT YET SUPPORTED
     * 
     * Detects the current architecture.
     * @return the detected architecture
     */
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

    /**
     * Returns the current operating system.
     * @return the OS
     */
    public OS getOS() {
        return this.os;
    }

    /**
     * ARCHITECTURE CHECK IS NOT YET SUPPORTED
     * 
     * Returns the current architecture.
     * @return the architecture
     */
    public Architecture getArchitecture() {
        return this.arch;
    }

    /**
     * Returns the singleton instance of the Platform class.
     * @return the singleton instance
     */
    public static Platform detectPlatform() {
        if (instance == null) {
            instance = new Platform();
        }
        return instance;
    }

    /**
     * Returns the appropriate PlatformHandler based on the current OS.
     * @return the PlatformHandler for the current OS
     */
    public static PlatformHandler getPlatformHandler() {
        if (instance == null) {
            instance = new Platform();
        }
        switch (instance.getOS()) {
            case WINDOWS -> {
                return WindowsHandler.getInstance();
            }
            case LINUX -> {
                return LinuxHandler.getInstance();
            }
            case MAC -> {
                return MacHandler.getInstance();
            }
            default -> throw new IllegalStateException("Unsupported OS: " + instance.getOS());
        }
    }
}
