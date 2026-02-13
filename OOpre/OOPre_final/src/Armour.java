class Armour extends Equipment implements Item {
    public Armour(String equipID, int ce) {
        super(equipID, ce);
    }

    @Override
    public String getID() {
        return getEquipID();
    }

    @Override
    public String getClassName() {
        return "Armour";
    }

}
