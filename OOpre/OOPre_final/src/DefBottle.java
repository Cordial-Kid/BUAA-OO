//防御
class DefBottle extends Bottle implements Item, Usage {
    public DefBottle(String bottleID, int effect) {
        super(bottleID, effect);
    }

    @Override
    public String getID() {
        return getBottleID();
    }

    @Override
    public String getClassName() {
        return "DefBottle";
    }

    @Override
    public void use(Adventurer source, Adventurer target, String useID, int flag) {
        Usage tmp = source.itemTaken(useID);
        if (tmp == null) {
            System.out.println(source.getAdvID() + " fail to use " + getBottleID());
        } else {
            int effect = getEffect();
            int def = target.getDef();
            def = def + effect;
            target.setDef(def);
            if (flag == 0) {
                System.out.println(target.getAdvID()
                        + " " + target.getHitPoint()
                        + " " + target.getAtk()
                        + " " + target.getDef()
                        + " " + target.getMana());
            }

            source.removeItem(useID);
        }
    }
}