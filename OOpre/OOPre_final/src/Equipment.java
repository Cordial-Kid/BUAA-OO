class Equipment implements Item {
    private String equipID;
    private int ce;

    public Equipment(String equipID, int ce) {
        this.equipID = equipID;
        this.ce = ce;
    }

    public int getEquipCE() {
        return ce;
    }

    public String getEquipID() {
        return equipID;
    }

    @Override
    public String getID() {
        return equipID;
    }

    @Override
    public String getClassName() {
        return "Equipment";
    }

}
