class AttackSpell extends Spell implements Usage {
    public AttackSpell(String spellID, int manaCost, int power) {
        super(spellID, manaCost, power);
    }

    @Override
    public String getID() {
        return getSpellID();
    }

    @Override
    public String getClassName() {
        return "AttackSpell";
    }

    @Override
    public void use(Adventurer source, Adventurer target, String useID, int flag) {
        if (!source.isAlive()) {
            System.out.println(source.getAdvID() + " is dead!");
            return;
        } else if (!target.isAlive()) {
            System.out.println(target.getAdvID() + " is dead!");
            return;
        }
        //法术一定携带
        int pre = source.getMana();
        if (pre >= getManaCost()) {
            int now = pre - getManaCost();
            source.setMana(now);
            int n = target.getHitPoint();
            n = n - getPower();
            if (n <= 0) {
                n = 0;
            }
            target.setHitPoint(n);
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
