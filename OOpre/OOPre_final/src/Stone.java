import java.util.HashMap;

public class Stone {
    //饿汉式私有实例化
    private static final Stone stone = new Stone();
    private final HashMap<Employer, Integer> sealedHp = new HashMap<>();

    private Stone() {
    }

    ;

    public static Stone getInstance() {
        return stone;
    }

    //战前封存
    public void seal(Employer employer) {
        sealedHp.put(employer, employer.getHp());
    }

    //判断是否触发救援
    public boolean shouldIntrigger(Employer employer) {
        int sealed = sealedHp.get(employer);
        if (employer.getHp() <= sealed / 2 && employer.getHp() > 0) {
            return true;
        } else {
            return false;
        }
    }
}
