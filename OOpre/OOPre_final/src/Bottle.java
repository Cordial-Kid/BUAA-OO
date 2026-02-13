class Bottle implements Item, Usage {
    private String bottleID;
    private int effect;

    public Bottle(String bottleID, int effect) {
        this.bottleID = bottleID;
        this.effect = effect;
    }

    public int getEffect() {
        return effect;
    }

    public String getBottleID() {
        return bottleID;
    }

    @Override
    public String getID() {
        return bottleID;
    }

    @Override
    public String getClassName() {
        return "Bottle";
    }

    @Override
    public void use(Adventurer source, Adventurer target, String useID, int flag) {

    }
}
