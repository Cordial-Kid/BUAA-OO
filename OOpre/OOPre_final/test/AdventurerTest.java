import org.junit.Test;
import org.junit.Before;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class AdventurerTest {

    private Adventurer adventurer;

    @Before
    public void setUp() {
        adventurer = new Adventurer("Bob");
    }

    @Test
    public void addBottle() {
        int pre = adventurer.getItemSize();
        adventurer.addBottle("forgetLove", "AtkBottle", 999);
        int now = adventurer.getItemSize();
        assertEquals(pre + 1, now);
//        如果能正确移除则加入方法正常
    }

    @Test
    public void addEquip() {
        int pre = adventurer.getItemSize();
        adventurer.addBottle("forgetLove", "AtkBottle", 999);
        int now = adventurer.getItemSize();
        assertEquals(pre + 1, now);
//        如果能正确移除则加入方法正常
    }

    @Test
    public void removeitem() {
        adventurer.addBottle("happy", "AtkBottle", 888);
        adventurer.removeItem("happy");
        assertEquals(0, adventurer.getItemSize());
    }


    @Test
    public void learnSpell() {
        int pre = adventurer.getSpellSize();
        adventurer.learnSpell("立定", "AttackSpell", 5, 5);
        int now = adventurer.getSpellSize();
        assertEquals(1, now - pre);
    }

    @Test
    public void takeItem() {
        adventurer.addBottle("a", "AtkBottle", 17);
        int pre = adventurer.getBackPackSize();
        adventurer.takeItem("a");
        int now = adventurer.getBackPackSize();
        assertEquals(1, now - pre);
    }

    @Test
    public void findItem() {
        adventurer.addBottle("a", "AtkBottle", 12);
        adventurer.addBottle("b", "AktBottle", 15);
        Item tmp = adventurer.findItem("a");
        assertEquals("a", tmp.getID());
    }

    @Test
    public void itemTaken() {
        adventurer.takeItem("1");
        adventurer.takeItem("2");
        adventurer.takeItem("3");
        Usage tmp = adventurer.itemTaken("4");
        assertNull(tmp);
    }

    @Test
    public void isAlive() {
        assertTrue(adventurer.isAlive());
        adventurer.setHitPoint(0);
        assertFalse(adventurer.isAlive());
    }

    @Test
    public void isUp() {
        Adventurer c = new Adventurer("Curry");
        Adventurer j = new Adventurer("james");
        Adventurer m = new Adventurer("Messi");
        adventurer.attach(c);
        c.attach(j);
        j.attach(m);
        boolean a = adventurer.isUp(c);
        boolean b = adventurer.isUp(m);
        assertTrue(a);
        assertTrue(b);
    }

    @Test
    public void isAlly() {
        Adventurer c = new Adventurer("Curry");
        Adventurer j = new Adventurer("james");
        Adventurer m = new Adventurer("Messi");
        adventurer.attach(c);
        c.attach(j);
        j.attach(m);
        boolean a = m.isAlly(adventurer);
        boolean b = m.isAlly(c);
    }

    @Test
    public void itemhave() {
        adventurer.addBottle("a", "AtkBottle", 17);
        Usage tmp = adventurer.itemhave("a");
        assertNotNull(tmp);
    }

    @Test
    public void aidEmployer() {
        int[] cnt = {0};
        Adventurer c = new Adventurer("curry");
        c.learnSpell("立定", "HealSpell", 5, 5);
        c.aidEmployer(adventurer, cnt);
        assertEquals(1, cnt[0]);
        int tmp = adventurer.getHitPoint();
        assertEquals(505, tmp);
    }

    @Test
    public void getSubordinates() {
        ArrayList<Adventurer> tmp = adventurer.getSubordinates();
    }


}