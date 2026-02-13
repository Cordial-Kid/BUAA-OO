//Usage接口接到bottle和spell，equipment没有该接口，就不具备该特征
interface Usage {
    void use(Adventurer source, Adventurer target, String useID, int flag);

    String getID();

    String getClassName();
}
