import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AtkBottleTest {
    private Adventurer source;
    private Adventurer target;
    private AtkBottle example;

    @Before
    public void setUp() {
        source = new Adventurer("a");
        target = new Adventurer("b");
        example = new AtkBottle("love", 12);
        source.addBottle("love", "AtkBottle", 12);
    }

    @Test
    public void getID() {
        String tmp = example.getID();
        assertEquals("love", tmp);
    }

    @Test
    public void getClassName() {
        String name = example.getClassName();
        assertEquals("AtkBottle", name);
    }

    @Test
    public void use() {
        source.takeItem("love");
        example.use(source, target, "love",0);
        assertEquals(13, target.getAtk());
        example.use(source,target,"hhh",0);
        assertEquals(13,target.getAtk());
    }
}