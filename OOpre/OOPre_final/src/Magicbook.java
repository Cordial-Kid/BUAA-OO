class Magicbook extends Weapon implements Item {
    public Magicbook(String equipID, int ce) {
        super(equipID, ce);
    }

    @Override
    public String getID() {
        return getEquipID();
    }

    @Override
    public String getClassName() {
        return "Magicbook";
    }
}
