import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class HealSpellTest {
    private HealSpell example;
    private Adventurer source;
    private Adventurer target;

    @Before
    public void setUp() {
        source = new Adventurer("a");
        target = new Adventurer("b");
        example = new HealSpell("love", 5, 100);
        source.learnSpell("love", "HealSpell", 5, 100);
    }

    @Test
    public void getID() {
        assertEquals("love", example.getID());
    }

    @Test
    public void use() {
        example.use(source, target, "love", 0);
        assertEquals(600, target.getHitPoint());
        example.use(source, target, "love", 0);
        example.use(source, target, "love", 0);
        assertEquals(700, target.getHitPoint());
    }
}