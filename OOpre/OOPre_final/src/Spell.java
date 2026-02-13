class Spell implements Usage {
    private String spellID;
    private int manaCost;
    private int power;

    public Spell(String spellID, int manaCost, int power) {
        this.manaCost = manaCost;
        this.spellID = spellID;
        this.power = power;
    }

    public String getSpellID() {
        return spellID;
    }

    public int getManaCost() {
        return manaCost;
    }

    public int getPower() {
        return power;
    }

    @Override
    public String getID() {
        return spellID;
    }

    //抽象方法可以强制子类执行这个方法，并且可以穿插到函数里，减少代码冗余
    //但是抽象方法会让父类无法实例化
    //两个法术的判断逻辑都一样，只是作用效果不一样，不需要一直重写，只要重写一部分就好
    @Override
    public void use(Adventurer source, Adventurer target, String useID, int flag) {
    }

    @Override
    public String getClassName() {
        return "Spell";
    }
}
