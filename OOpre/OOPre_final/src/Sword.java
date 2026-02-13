class Sword extends Weapon implements Item {
    public Sword(String equipID, int ce) {
        super(equipID, ce);
    }

    @Override
    public String getID() {
        return getEquipID();
    }

    @Override
    public String getClassName() {
        return "Sword";
    }
}
