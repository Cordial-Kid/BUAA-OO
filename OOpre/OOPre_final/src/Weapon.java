class Weapon extends Equipment implements Item {
    public Weapon(String equipID, int ce) {
        super(equipID, ce);
    }

    @Override
    public String getID() {
        return getEquipID();
    }

    @Override
    public String getClassName() {
        return "Weapon";
    }
}

