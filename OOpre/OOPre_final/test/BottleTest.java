import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class BottleTest {
    private Bottle example;

    @Before
    public void setUp() {
        example = new Bottle("love", 10);
    }

    @Test
    public void getBottleID() {
        assertEquals("love", example.getBottleID());
    }

    @Test
    public void getID() {
        assertEquals("love", example.getID());
    }

    @Test
    public void getEffect() {
        assertEquals(10, example.getEffect());
    }

    @Test
    public void getClassName() {
        assertEquals("Bottle", example.getClassName());
    }
}