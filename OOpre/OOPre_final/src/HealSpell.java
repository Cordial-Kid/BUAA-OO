class HealSpell extends Spell implements Usage {
    public HealSpell(String spellID, int manaCost, int power) {
        super(spellID, manaCost, power);
    }

    @Override
    public String getID() {
        return getSpellID();
    }

    @Override
    public String getClassName() {
        return "HealSpell";
    }

    @Override
    public void use(Adventurer source, Adventurer target, String useID, int flag) {
        //法术一定携带
        int pre = source.getMana();
        if (pre >= getManaCost()) {
            int now = pre - getManaCost();
            int n = target.getHitPoint();
            n = n + getPower();
            target.setHitPoint(n);
            source.setMana(now);
            if (flag == 0) {
                System.out.println(target.getAdvID()
                        + " " + target.getHitPoint()
                        + " " + target.getAtk()
                        + " " + target.getDef()
                        + " " + target.getMana());
            }
        } else {
            System.out.println(source.getAdvID() + " fail to use " + getSpellID());
        }
    }
}
