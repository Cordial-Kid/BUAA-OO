import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

public class AdventureContainerTest {
    private AdventureContainer roster;

    @Before
    public void setUp() {
        roster = new AdventureContainer();
    }

    @Test
    public void addAdventurer() {
        roster.addAdventurer("哈基米");
        assertEquals(1, roster.getsize());
    }


    @Test
    public void addBottleTop() {
        roster.addAdventurer("Messi");
        roster.addAdventurer("Cristiano");
        Adventurer tmp = roster.finder("Messi");
        int pre = tmp.getItemSize();
        roster.addBottleTop("Messi", "AtkBottle", "HpBottle", 10086);
        int now = tmp.getItemSize();
        assertEquals(pre + 1, now);
        tmp.setHitPoint(0);
        roster.addBottleTop("Messi", "666", "HpBottle", 10086);
        int _now = tmp.getItemSize();
        assertEquals(1, _now);
    }

    @Test
    public void addEquipTop() {
        roster.addAdventurer("James");
        Adventurer tmp = roster.finder("James");
        int pre = tmp.getItemSize();
        roster.addEquipTop("James", "大宝剑", "Sword", 19);
        int now = tmp.getItemSize();
        assertEquals(pre + 1, now);
        tmp.setHitPoint(0);
        roster.addBottleTop("James", "666", "HpBottle", 10086);
        int _now = tmp.getItemSize();
        assertEquals(1, _now);
    }

    @Test
    public void removeItemTop() {
        roster.addAdventurer("Curry");
        roster.addBottleTop("Curry", "AtkBottle", "AtkBottle", 10010);
        roster.removeItemTop("Curry", "AtkBottle");
        Adventurer tmp = roster.finder("Curry");
        assertEquals(0, tmp.getItemSize());

    }

    @Test
    public void learnSpellTop() {
        roster.addAdventurer("Curry");
        roster.learnSpellTop("Curry", "love", "AttackSpell", 5, 10);
        Adventurer tmp = roster.finder("Curry");
        int num = tmp.getSpellSize();
        assertEquals(1, num);
        tmp.setHitPoint(0);
        roster.learnSpellTop("Messi", "666", "HpBottle", 5, 10);
        int _now = tmp.getSpellSize();
        assertEquals(1, _now);
    }

    @Test
    public void takeItemTop() {
        roster.addAdventurer("Curry");
        roster.addBottleTop("Curry", "AtkBottle", "AtkBottle", 10010);
        roster.takeItemTop("Curry", "AtkBottle");
        Adventurer tmp = roster.finder("Curry");
        int num = tmp.getBackPackSize();
        assertEquals(1, num);
        tmp.setHitPoint(0);
        roster.takeItemTop("Curry", "AtkBottle");
        int _now = tmp.getBackPackSize();
        assertEquals(1, _now);
    }

    @Test
    public void buyItemTop() {
        roster.addAdventurer("Curry");
        roster.buyItemTop("Curry", "小宝剑", "Sword");
        Adventurer tmp = roster.finder("Curry");
        int num = tmp.getItemSize();
        assertEquals(1, num);
        tmp.setHitPoint(0);
        roster.buyItemTop("Curry", "小宝剑", "Sword");
        int _now = tmp.getItemSize();
        assertEquals(1, _now);
    }

    @Test
    public void getAllDef() {
        roster.addAdventurer("Curry");
        Adventurer tmp = roster.finder("Curry");
        tmp.addEquip("复活甲", "Armour", 100);
        tmp.takeItem("复活甲");
        List<String> list = new ArrayList<>();
        list.add("Curry");
        int def = roster.getAllDef(1, list);
        assertEquals(100, def);
    }

    @Test
    public void fightTop() {
        roster.addAdventurer("Curry");
        roster.addAdventurer("James");
        roster.addAdventurer("Messi");
        Adventurer C = roster.finder("Curry");
        Adventurer J = roster.finder("James");
        Adventurer M = roster.finder("Messi");
        roster.addEquipTop("Curry", "大宝剑", "Sword", 999);
        roster.addEquipTop("James", "反甲", "Armour", 999);
        C.takeItem("大宝剑");
        J.takeItem("反甲");
        List<String> list = new ArrayList<>();
        list.add("James");
        roster.fightTop("Curry", 1, list);
        int hit = J.getHitPoint();
        assertEquals(499, hit);
        roster.addEquipTop("Curry", "回响之杖", "Magicbook", 16);
        List<String> _list = new ArrayList<>();
        _list.add("Messi");
        C.takeItem("回响之杖");
        roster.fightTop("Curry", 1, _list);
        int restMana = C.getMana();
        assertEquals(6, restMana);
        int restHit = M.getHitPoint();
        assertEquals(483, restHit);
    }

    @Test
    public void addRelationTop() {
        roster.addAdventurer("Curry");
        roster.addAdventurer("James");
        roster.addAdventurer("Messi");
        Adventurer C = roster.finder("Curry");
        Adventurer J = roster.finder("James");
        Adventurer M = roster.finder("Messi");
        int a = C.getHiredSize();
        roster.addRelationTop("Curry", "James");
        int b = C.getHiredSize();
        assertEquals(1, b - a);
    }

    @Test
    public void removeRelation() {
        roster.addAdventurer("Curry");
        roster.addAdventurer("James");
        roster.addAdventurer("Messi");
        Adventurer C = roster.finder("Curry");
        Adventurer J = roster.finder("James");
        Adventurer M = roster.finder("Messi");
        int a = C.getHiredSize();
        roster.addRelationTop("Curry", "James");
        int b = C.getHiredSize();
        assertEquals(1, b - a);
        roster.removeRelation("Curry", "James");
        int c = C.getHiredSize();
        assertEquals(0, c);
    }


}