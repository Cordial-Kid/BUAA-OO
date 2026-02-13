import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AttackSpellTest {
    private AttackSpell example1;
    private AttackSpell example2;
    private Adventurer source;
    private Adventurer target;

    @Before
    public void setUp() {
        source = new Adventurer("a");
        target = new Adventurer("b");
        example1 = new AttackSpell("love", 5, 100);
        example2 = new AttackSpell("JJ", 5, 600);
        source.learnSpell("love", "AttackSpell", 5, 100);
        source.learnSpell("JJ", "AttackSpell", 5, 600);
    }

    @Test
    public void getID() {
        assertEquals("love", example1.getID());
    }

    @Test
    public void use() {
        example1.use(source, target, "love", 0);
        assertEquals(400, target.getHitPoint());
        example2.use(source, target, "JJ", 0);
        assertEquals(0, target.getHitPoint());
    }

    @Test
    public void getClassName() {
        example1.getClassName();
    }
}