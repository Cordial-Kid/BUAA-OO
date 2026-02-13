import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DefBottleTest {
    private Adventurer source;
    private Adventurer target;
    private DefBottle example;

    @Before
    public void setUp() {
        source = new Adventurer("a");
        target = new Adventurer("b");
        example = new DefBottle("love", 12);
        source.addBottle("love", "DefBottle", 12);
    }

    @Test
    public void getID() {
        String tmp = example.getID();
        assertEquals("love", tmp);
    }

    @Test
    public void getClassName() {
        String name = example.getClassName();
        assertEquals("DefBottle", name);
    }

    @Test
    public void use() {
        source.takeItem("love");
        example.use(source, target, "love", 0);
        assertEquals(12, target.getDef());
        example.use(source, target, "hhh", 0);
        assertEquals(12, target.getDef());
    }
}