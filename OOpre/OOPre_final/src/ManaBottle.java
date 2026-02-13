//魔力
class ManaBottle extends Bottle implements Item, Usage {
    public ManaBottle(String bottleID, int effect) {
        super(bottleID, effect);
    }

    @Override
    public String getID() {
        //所有的子类都有ID，没有特殊
        //因为接口所以必须重写，否则直接调用父类即可
        return getBottleID();
    }

    @Override
    public String getClassName() {
        return "ManaBottle";
    }

    @Override
    public void use(Adventurer source, Adventurer target, String useID, int flag) {
        Usage tmp = source.itemTaken(useID);
        if (tmp == null) {
            System.out.println(source.getAdvID() + " fail to use " + getBottleID());
        } else {
            int effect = getEffect();
            int mana = target.getMana();
            mana = mana + effect;
            target.setMana(mana);
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
