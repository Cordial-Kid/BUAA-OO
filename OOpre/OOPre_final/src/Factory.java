//工厂模式的核心是封装对象创建的逻辑

public class Factory {
    public static Bottle createBottle(String bottleID, String type, int bottleEffect) {
        switch (type) {
            case "HpBottle":
                return new HpBottle(bottleID, bottleEffect);
            case "ManaBottle":
                return new ManaBottle(bottleID, bottleEffect);
            case "DefBottle":
                return new DefBottle(bottleID, bottleEffect);
            case "AtkBottle":
                return new AtkBottle(bottleID, bottleEffect);
            default:
                return null;
        }
    }

    public static Spell createSpell(String spellID, String type, int manaCost, int power) {
        switch (type) {
            case "HealSpell":
                return new HealSpell(spellID, manaCost, power);
            case "AttackSpell":
                return new AttackSpell(spellID, manaCost, power);
            default:
                return null;
        }
    }

    public static Equipment createEquipment(String equipmentID, String className, int ce) {
        switch (className) {
            case "Armour":
                return new Armour(equipmentID, ce);
            case "Sword":
                return new Sword(equipmentID, ce);
            case "Magicbook":
                return new Magicbook(equipmentID, ce);
            default:
                return null;
        }
    }

    public static Item createItem(String id, String type, int money) {
        Item item = createBottle(type, id, money);
        if (item == null) {
            item = createEquipment(type, id, money);
        }
        return item;
    }

}
