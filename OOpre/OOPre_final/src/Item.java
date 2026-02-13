interface Item {
    String getID();

    String getClassName();
}

//抽象类实际上就是一个包含普通方法和成员变量的类接口，
//用abstract可以表似乎抽象方法，像这个一样
//非抽象子类必须重写所有抽象方法
