import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class HpBottleTest {
    private Adventurer source;
    private Adventurer target;
    private HpBottle example;

    @Before
    public void setUp() {
        source = new Adventurer("a");
        target = new Adventurer("b");
        example = new HpBottle("love", 12);
        source.addBottle("love", "HpBottle", 12);
    }

    @Test
    public void getID() {
        String tmp = example.getID();
        assertEquals("love", tmp);
    }

    @Test
    public void getClassName() {
        String name = example.getClassName();
        assertEquals("HpBottle", name);
    }

    @Test
    public void use() {
        source.takeItem("love");
        example.use(source, target, "love", 0);
        assertEquals(512, target.getHitPoint());
        example.use(source, target, "hhh", 0);
        assertEquals(512, target.getHitPoint());
    }
}