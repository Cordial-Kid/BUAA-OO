import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ManaBottleTest {
    private Adventurer source;
    private Adventurer target;
    private ManaBottle example;

    @Before
    public void setUp() {
        source = new Adventurer("a");
        target = new Adventurer("b");
        example = new ManaBottle("love", 12);
        source.addBottle("love", "ManaBottle", 12);
    }

    @Test
    public void getID() {
        String tmp = example.getID();
        assertEquals("love", tmp);
    }

    @Test
    public void getClassName() {
        String name = example.getClassName();
        assertEquals("ManaBottle", name);
    }

    @Test
    public void use() {
        source.takeItem("love");
        example.use(source, target, "love", 0);
        assertEquals(22, target.getMana());
        example.use(source, target, "hhh", 0);
        assertEquals(22, target.getMana());
    }
}