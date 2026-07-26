package ua.alu.npj6;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URISyntaxException;
import java.io.File;

public class DecklistTest {

    @Test
    public void readFileTest() throws URISyntaxException {
        File deckfile;

        URL resource = assertDoesNotThrow(() -> getClass().getClassLoader().getResource("test.dck"));
        assertNotNull(resource);

        deckfile = new File(resource.toURI());
        Decklist SUT = new Decklist(deckfile);
        
        String names[] = new String[]{"A", "BBB", "C C C", "DD DD DD"};
        int list[] = new int[]{0, 1, 1, 2, 2, 2, 3, 3, 3, 3};

        assertArrayEquals(names, SUT.names);
        assertArrayEquals(list, SUT.list);
    }
}
