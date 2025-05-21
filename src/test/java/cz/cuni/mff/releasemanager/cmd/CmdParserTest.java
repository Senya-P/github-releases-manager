package cz.cuni.mff.releasemanager.cmd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class CmdParserTest {

    private CmdParser cmdParser;

    @BeforeEach
    void setUp() {
        cmdParser = new CmdParser();
    }

    @Test
    void testParseArgument() {
        String[] args = {"COMMAND_NAME", "argument"};
        Command result = cmdParser.parse(args);

        assertNotNull(result);
        assertEquals("argument", result.argument);
    }

    @Test
    void testParseWithValidCommand() {
        String[] args = {"list"};
        Command result = cmdParser.parse(args);
        assertEquals(Command.LIST, result);
    }

    @Test
    void testParseCaseInsensitivityWithValidCommand() {
        String[] args = {"SEaRch", "argument"};
        Command result = cmdParser.parse(args);
        assertEquals(Command.SEARCH, result);
    }

    @Test
    void testParseWithInvalidCommand() {
        String[] args = {"INVALID_COMMAND"};
        Command result = cmdParser.parse(args);
        assertEquals(Command.HELP, result);
    }

    @Test
    void testParseWithNoArguments() {
        String[] args = {};
        Command result = cmdParser.parse(args);
        assertEquals(Command.HELP, result);
    }
}