# GitHub Releases Manager

A multi-platform command-line tool for GitHub releases.

## Features

- **Search** for public GitHub repositories by name
- **Install** the latest release of a repository
- **Update** installed releases if newer versions are available
- **Uninstall** installed release
- **List** all installed releases
- **Help** for usage guidance

Supported formats: exe, msi, dmg, appimage

---

## Prerequisites

- Java 21+
- Maven 3.6+

---

## Building the application

To build the project:

```bash
mvn package
```

This will produce `target/github-releases-manager-1.0-jar-with-dependencies.jar`


To remove the generated files:

```bash
mvn clean
```

## Running the application

```bash
java -jar target/github-releases-manager-1.0-jar-with-dependencies.jar [command] [options]
```

Example:

```bash
java -jar target/github-releases-manager-1.0-jar-with-dependencies.jar list
```

## Supported commands

| Command                  | Description                                                 |
| ------------------------ | ----------------------------------------------------------- |
| `search [name]`          | Search for repositories by name                             |
| `install [owner/repo]`   | Install the latest release of a repository                  |
| `uninstall [owner/repo]` | Uninstall a previously installed release                    |
| `update [owner/repo]`    | Update an installed release if a newer version is available |
| `list`                   | List all installed releases                                 |
| `help`                   | Show usage instructions                                     |

Example repositories:

- keepassxreboot/keepassxc
- ip7z/7zip
- obsproject/obs-studio

## Running the tests

```bash
mvn test
```

## Documentation

To generate the full API documentation:

```bash
mvn javadoc:javadoc
```

The javadoc will be available at `target/reports/apidocs/index.html`
