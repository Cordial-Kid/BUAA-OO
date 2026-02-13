import java.util.HashMap;
import java.util.List;

class AdventureContainer {
    private HashMap<String, Adventurer> adventurers;

    public AdventureContainer() {
        this.adventurers = new HashMap<>();
    }

    public Adventurer finder(String advID) {
        Adventurer tmp = adventurers.get(advID);
        return tmp;
    }

    private Stone stone = Stone.getInstance();

    //    添加冒险者
    public void addAdventurer(String adventurerID) {
        Adventurer newone = new Adventurer(adventurerID);
        adventurers.put(adventurerID, newone);
    }

    //    添加药水(因为有effect，所以要分开添加）
    public void addBottleTop(String adventurerID, String bottleID, String type, int effect) {
        Adventurer tmp = adventurers.get(adventurerID);
        if (tmp != null && tmp.isAlive()) {
            tmp.addBottle(bottleID, type, effect);
        } else {
            System.out.println(adventurerID + " is dead!");
        }
    }

    //    添加装备
    public void addEquipTop(String adventurerID, String equipmentID, String type, int ce) {
        Adventurer tmp = adventurers.get(adventurerID);
        if (tmp != null && tmp.isAlive()) {
            tmp.addEquip(equipmentID, type, ce);
        } else {
            System.out.println(adventurerID + " is dead!");
        }
    }

    //    删除item
    public void removeItemTop(String adventurerID, String bottleID) {
        Adventurer tmp = adventurers.get(adventurerID);
        if (tmp != null && tmp.isAlive()) {
            Item tmp1 = tmp.findItem(bottleID);
            tmp.removeItem(bottleID);
            System.out.println(tmp1.getClassName());
        } else {
            System.out.println(adventurerID + " is dead!");
        }
    }

    //学习法术
    public void learnSpellTop(String advID, String spellID, String type, int manaCost, int power) {
        Adventurer tmp = adventurers.get(advID);
        if (tmp != null && tmp.isAlive()) {
            tmp.learnSpell(spellID, type, manaCost, power);
        } else {
            System.out.println(advID + " is dead!");
        }
    }

    //携带东西
    public void takeItemTop(String advID, String itemID) {
        Adventurer tmp = adventurers.get(advID);
        if (tmp != null && tmp.isAlive()) {
            tmp.takeItem(itemID);
        } else {
            System.out.println(advID + " is dead!");
        }
    }

    //使用
    public void use(String advID, String useID, String targetID) {
        Adventurer source = adventurers.get(advID);
        Adventurer target = adventurers.get(targetID);
        //use方法在Bottle和Spell中有写到
        if (!source.isAlive()) {
            System.out.println(source.getAdvID() + " is dead!");
            return;
        } else if (!target.isAlive()) {
            System.out.println(target.getAdvID() + " is dead!");
            return;
        }
        stone.seal(target);
        Usage have = source.itemhave(useID);
        if (have != null && (have.getClassName().equals("HealSpell") ||
                have.getClassName().equals("AtkBottle") ||
                have.getClassName().equals("DefBottle") ||
                have.getClassName().equals("ManaBottle") ||
                have.getClassName().equals("HpBottle")) && !source.isAlly(target)) {
            System.out.println("That's not my ally");
            return;
        }
        if (have != null && have.getClassName().equals("AttackSpell") && target.isUp(source)) {
            System.out.println("That's my boss!");
            return;
        }
        Usage tmp = source.itemTaken(useID);
        if (tmp == null) {
            System.out.println(source.getAdvID() + " fail to use " + useID);
        } else {
            tmp.use(source, target, useID, 0);
            use_help(target);
            //use也可以导致死亡从而收获金币
            if (!target.isAlive()) {
                int preMoney = source.getMoney();
                int increase = target.cntValue();
                int nowMoney = preMoney + increase;
                source.setMoney(nowMoney);
            }
        }
    }

    //help of use
    public void use_help(Adventurer suffer) {
        suffer.setAidTrigger(stone.shouldIntrigger(suffer));
        if (suffer.getAidTrigger()) {
            int[] cnt = {0};
            suffer.notifyEmployees(cnt);
            if (cnt[0] > 0) {
                System.out.println(suffer.getAdvID() + " "
                        + "is helped by" + " "
                        + cnt[0] + " " + "adventurer(s), now Hp is"
                        + " " + suffer.getHitPoint());
            }
        }
    }

    //购买
    public void buyItemTop(String advID, String itemID, String type) {
        Adventurer tmp = adventurers.get(advID);
        if (tmp != null && tmp.isAlive()) {
            tmp.buyItem(itemID, type);
        } else {
            System.out.println(advID + " is dead!");
        }
    }

    //计算防御力
    public int getAllDef(int number, List<String> list) {
        int max = 0;
        for (String i : list) {
            Adventurer mid = adventurers.get(i);
            int cmp = mid.getFinalDef();
            max = Math.max(cmp, max);
        }
        return max;
    }

    //物理攻击
    public void physicsAttack(int atk, int def, Adventurer tmp, List<String> list, String source) {
        if (atk > def) {
            int gap = atk - def;
            for (String i : list) {
                Adventurer mid = adventurers.get(i);
                int preH = mid.getHitPoint();
                int nowH = Math.max(preH - gap, 0);
                mid.setHitPoint(nowH);
                System.out.print(nowH + " ");
                if (nowH == 0) {
                    int apreMoney = tmp.getMoney();
                    int increase = mid.cntValue();
                    int anowMoney = apreMoney + increase;
                    tmp.setMoney(anowMoney);
                }
            }
            System.out.println();
        } else {
            System.out.println("Adventurer" + " " + source + " " + "defeated");
        }
    }

    //法术攻击
    public void magicAttack(Adventurer tmp, List<String> list, int atk, String source) {
        if (tmp.getMana() >= Math.ceil(Math.sqrt(tmp.checkWeapon()))) {
            for (String i : list) {
                Adventurer mid = adventurers.get(i);
                int preH = mid.getHitPoint();
                int nowH = Math.max(preH - atk, 0);
                mid.setHitPoint(nowH);
                System.out.print(nowH + " ");
                if (nowH == 0) {
                    int apreMoney = tmp.getMoney();
                    int increase = mid.cntValue();
                    int anowMoney = apreMoney + increase;
                    tmp.setMoney(anowMoney);
                }
            }
            System.out.println();
            int apreMana = tmp.getMana();
            int anowMana = apreMana - (int) Math.ceil(Math.sqrt(tmp.checkWeapon()));
            tmp.setMana(anowMana);
        } else {
            System.out.println("Adventurer" + " " + source + " " + "defeated");
        }
    }

    //攻击
    public void fightTop(String source, int number, List<String> list) {
        Adventurer tmp = adventurers.get(source);
        if (tmp != null && tmp.isAlive()) {
            for (String i : list) {
                Adventurer t = adventurers.get(i);
                if (t.isUp(tmp)) {
                    System.out.println("That's my boss!");
                    return;
                }
            }
        }
        for (String j : list) {
            Adventurer tmpE = adventurers.get(j);
            stone.seal(tmpE);
        }

        if (tmp != null && tmp.isAlive()) {
            int def = getAllDef(number, list);
            int atk = tmp.getFinalAtk();
            if (tmp.checkWeapon() == 0) {
                //物理攻击成功
                physicsAttack(atk, def, tmp, list, source);
            } else {
                magicAttack(tmp, list, atk, source);
            }
            fight_help(list);
        } else {
            System.out.println(source + " is dead!");
        }
    }

    //help of fight
    public void fight_help(List<String> list) {
        for (String index : list) {
            Adventurer suffer = adventurers.get(index);
            suffer.setAidTrigger(stone.shouldIntrigger(suffer));
            if (suffer.getAidTrigger()) {
                int[] cnt = {0};
                suffer.notifyEmployees(cnt);
                if (cnt[0] > 0) {
                    System.out.println(suffer.getAdvID() + " "
                            + "is helped by" + " "
                            + cnt[0] + " " + "adventurer(s), now Hp is"
                            + " " + suffer.getHitPoint());
                }
            }
        }
    }

    //添加雇佣关系
    public void addRelationTop(String adv1, String adv2) {
        Adventurer tmp1 = adventurers.get(adv1);
        Adventurer tmp2 = adventurers.get(adv2);
        if (tmp1.getHitPoint() == 0) {
            System.out.println(adv1 + " is dead!");
        } else if (tmp2.getHitPoint() == 0) {
            System.out.println(adv2 + " is dead!");
        } else {
            tmp1.attach(tmp2);
        }

    }

    //删除雇佣关系
    public void removeRelation(String adv1, String adv2) {
        Adventurer tmp1 = adventurers.get(adv1);
        Adventurer tmp2 = adventurers.get(adv2);
        if (tmp1.getHitPoint() == 0) {
            System.out.println(adv1 + " is dead!");
        } else if (tmp2.getHitPoint() == 0) {
            System.out.println(adv2 + " is dead!");
        } else {
            tmp1.detach(tmp2);
        }
    }

    public int getsize() {
        return adventurers.size();
    }

}
