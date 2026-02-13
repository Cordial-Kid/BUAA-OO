import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;

//接口实现 “统一管理不同子类对象”
//Bottle（及子类 HpBottle）和 Equipment 都实现了 Item 接口，那么它们的对象都可以被当作 Item 类型
//类和接口几乎一样
//用backpack，bottleBackpack，weaponBackpack,ArmourBackpack来更方便的维护程序，
// 其中第一个用HashMap，后三个用队列

class Adventurer implements Employer, Employee {
    private String advID;
    private int hitPoint;
    private int atk;
    private int def;
    private int mana;
    private int money;
    private boolean aidTrigger;
    private HashMap<String, Item> items;
    private HashMap<String, Item> backpack;
    private LinkedList<Bottle> bottleBackpack;
    private LinkedList<Armour> armourBackPack;
    private LinkedList<Weapon> weaponBackPack;
    private ArrayList<Spell> spells;
    private ArrayList<Adventurer> hired;

    //构造方法的核心是初始化对象
    public Adventurer(String advID) {
        this.advID = advID;
        this.hitPoint = 500;
        this.atk = 1;
        this.def = 0;
        this.mana = 10;
        this.money = 50;
        this.aidTrigger = false;
        //下面两个初始化不参与传参的
        this.items = new HashMap<>();
        this.backpack = new HashMap<>();
        this.bottleBackpack = new LinkedList<>();
        this.armourBackPack = new LinkedList<>();
        this.weaponBackPack = new LinkedList<>();
        this.spells = new ArrayList<>();
        this.hired = new ArrayList<>();
    }

    public boolean getAidTrigger() {
        return aidTrigger;
    }

    public String getAdvID() {
        return advID;
    }

    public int getMoney() {
        return money;
    }

    public int getMana() {
        return mana;
    }

    public int getHitPoint() {
        return hitPoint;
    }

    public int getAtk() {
        return atk;
    }

    public int getFinalAtk() {
        Weapon tmp = weaponBackPack.peek();
        int ce = 0;
        if (tmp != null) {
            ce = tmp.getEquipCE();
        }
        int finalAtk = atk + ce;
        return finalAtk;
    }

    public int getDef() {
        return def;
    }

    public int getFinalDef() {
        Armour tmp = armourBackPack.peek();
        int ce = 0;
        if (tmp != null) {
            ce = tmp.getEquipCE();
        }
        int finalDef = def + ce;
        return finalDef;
    }

    public void setMana(int n) {
        this.mana = n;
    }

    public void setHitPoint(int n) {
        this.hitPoint = n;
    }

    public void setAtk(int n) {
        this.atk = n;
    }

    public void setDef(int n) {
        this.def = n;
    }

    public void setMoney(int n) {
        this.money = n;
    }

    //数量一般是为了验证才开发出来的
    public int getItemSize() {
        return items.size();
    }

    public int getSpellSize() {
        return spells.size();
    }

    public int getBackPackSize() {
        return backpack.size();
    }

    public int getHiredSize() {
        return hired.size();
    }

    public void setAidTrigger(boolean n) {
        aidTrigger = n;
    }

    //Employer
    @Override
    public String getName() {
        return getAdvID();
    }

    @Override
    public void attach(Employee employee) {
        Adventurer tmp = null;
        if (employee instanceof Adventurer) {
            tmp = (Adventurer) employee;
        }
        hired.add(tmp);
    }

    @Override
    public void detach(Employee employee) {
        hired.remove(employee);
    }

    @Override
    public int getHp() {
        return getHitPoint();
    }

    //不同视角的切换，记得把target存下来
    @Override
    //通知的时候一般要体现出来调用employee的方法
    public void notifyEmployees(int[] cnt) {
        //这里要操控employee援助
        // 使用 visited 防止同一个下级通过多条路径被多次尝试（图而非树情况）
        HashSet<Adventurer> visited = new HashSet<>();
        for (Adventurer e : hired) {
            if (e != null && e.isAlive() && !visited.contains(e)) {
                notifyAllEmployees(e, this, cnt, visited);
            }
        }
    }

    //    被通知的下级要做出某些回应
    // 增加 visited 参数以防止重复访问（图结构中同一节点可能被多次遍历）
    private void notifyAllEmployees(Employee employee,
                                    Employer target,
                                    int[] cnt,
                                    HashSet<Adventurer> visited) {
        if (employee instanceof Adventurer) {
            Adventurer employee1 = (Adventurer) employee;
            if (employee1.isAlive() && !visited.contains(employee1)) {
                // 标记已访问
                visited.add(employee1);
                employee.aidEmployer(target, cnt);
            }
        }
        for (Adventurer subordinate : employee.getSubordinates()) {
            if (subordinate != null && !visited.contains(subordinate) && subordinate.isAlive()) {
                notifyAllEmployees(subordinate, target, cnt, visited);
            }
        }
    }

    //employee

    public void aidEmployer(Employer target, int[] cnt) {
        //选择要使用的治疗法术
        Spell ans = null;
        for (Spell s : spells) {
            if (s.getClassName().equals("HealSpell") && mana >= s.getManaCost()) {
                if (ans == null) {
                    ans = s;
                } else {
                    if (s.getPower() > ans.getPower()) {
                        ans = s;
                    } else if (s.getPower() == ans.getPower()) {
                        ans = (ans.getManaCost() > s.getManaCost()) ? s : ans;
                    }
                }
            }
        }
        if (ans != null) {
            ans.use(this, (Adventurer) target, ans.getSpellID(), 1);
            cnt[0]++;
        }
    }

    public ArrayList<Adventurer> getSubordinates() {
        return hired;
    }

    //添加bottle
    public void addBottle(String bottleID, String type, int bottleEffect) {
        Bottle newone = Factory.createBottle(bottleID, type, bottleEffect);
        items.put(bottleID, newone);
    }

    //添加equipment
    public void addEquip(String equipmentID, String type, int ce) {
        Equipment newone = Factory.createEquipment(equipmentID, type, ce);
        items.put(equipmentID, newone);
    }

    //    在所有中查找item
    public Item findItem(String itemID) {
        if (items.containsKey(itemID)) {
            return items.get(itemID);
        }
        return null;
    }
    //在背包中查找Item

    //从所有区删除item
    //删除一个还好，当要删除很多的时候就需要用迭代器，并且将需要删除的存下来，避免一边删除一边更改集合的结构
    public void removeItem(String itemID) {
        Item tmp = findItem(itemID);
        if (tmp != null) {
            items.remove(itemID);
            backpack.remove(itemID);
            bottleBackpack.remove(tmp);
            armourBackPack.remove(tmp);
            weaponBackPack.remove(tmp);
        }
    }

    //学习法术，独立管理
    public void learnSpell(String spellID, String type, int manaCost, int power) {
        Spell newone = Factory.createSpell(spellID, type, manaCost, power);
        spells.add(newone);
    }

    //  物品进包
    public void takeItem(String itemID) {
        Item tmp = findItem(itemID);
        if (tmp != null) {
            backpack.put(itemID, tmp);
            if (tmp instanceof Bottle) {
                Bottle bottle = (Bottle) tmp;   //强制转换自己没用
                if (bottleBackpack.size() == 10) {
                    bottleBackpack.poll();
                }
                bottleBackpack.add(bottle);
            } else if (tmp instanceof Armour) {
                Armour armour = (Armour) tmp;
                if (armourBackPack.size() == 1) {
                    armourBackPack.poll();
                }
                armourBackPack.add(armour);
            } else if (tmp instanceof Weapon) {
                Weapon weapon = (Weapon) tmp;
                if (weaponBackPack.size() == 1) {
                    weaponBackPack.poll();
                }
                weaponBackPack.add(weapon);
            }
            System.out.println(tmp.getClassName());
        }
    }

    //    判断死活
    public boolean isAlive() {
        return hitPoint > 0;
    }

    //是否携带了需要的东西
    public Usage itemTaken(String useID) {
        for (Spell i : spells) {
            if (i.getID().equals(useID)) {
                return i;
            }
        }
        //j指向的是Item并不都是bottle，不一定都有用
        for (Bottle bottle : bottleBackpack) {
            if (bottle.getBottleID().equals(useID)) {
                return bottle;
            }
        }
        return null;
    }

    //从拥有的物品里面找
    public Usage itemhave(String useID) {
        for (Spell i : spells) {
            if (i.getID().equals(useID)) {
                return i;
            }
        }
        //j指向的是Item并不都是bottle，不一定都有用
        for (Item bottle : items.values()) {
            if (bottle.getID().equals(useID)) {
                return (Usage) bottle;
            }
        }
        return null;
    }
    //使用应该是congtainer的事了，因为涉及到多人

    public void buyItem(String itemID, String type) {
        int cost = Math.min(money, 100);
        Item newone = Factory.createItem(type, itemID, cost);
        items.put(itemID, newone);
        int rest = money - cost;
        money = rest;
        System.out.println(rest);
    }

    public int checkWeapon() {
        int ans = 0;
        Weapon tmp = weaponBackPack.peek();
        if (tmp != null && tmp.getClassName().equals("Magicbook")) {
            ans = tmp.getEquipCE();
        }
        return ans;
    }

    public int cntValue() {
        int ans = 0;
        for (Item i : items.values()) {
            if (i instanceof Bottle) {
                Bottle tmp = (Bottle) i;
                ans += tmp.getEffect();
            } else {
                Equipment tmpp = (Equipment) i;
                ans += tmpp.getEquipCE();
            }
        }
        ans += money;
        return ans;
    }

    public boolean isUp(Adventurer tar) {
        return isUpHelper(tar, new HashSet<>());
    }

    //图的遍历,判断adventurer是不是一个employee的上级
    //一次调用用同一个visited
    public boolean isUpHelper(Adventurer tar, HashSet<String> visited) {
        if (this == tar) {
            return true;
        }
        if (visited.contains(this.advID)) {
            return false;
        }
        visited.add(advID);
        //this 的使用在递归调用的时候极其有用
        for (Adventurer adv : this.hired) {
            if (adv.isAlive()) {
                if (adv.isUpHelper(tar, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    //判断是否是盟友关系
    public boolean isAlly(Adventurer ques) {
        if (this == ques || this.isUp(ques) || ques.isUp(this)) {
            return true;
        }
        return false;
    }

}
