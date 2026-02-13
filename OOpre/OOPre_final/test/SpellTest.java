import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SpellTest {
    private Spell example;

    @Before
    public void setUp() {
        example = new Spell("a", 6, 10);
    }

    @Test
    public void getSpellID() {
    }

    @Test
    public void getManaCost() {
    }

    @Test
    public void getPower() {
    }

    @Test
    public void getID() {
        assertEquals("a", example.getID());
    }

    @Test
    public void use() {
    }

    @Test
    public void getClassName() {
        example.getClassName();
    }
}