package domain;

public enum Floor {
    // 枚举常量(level),枚举常量相当于是对象
    B4(-4), B3(-3), B2(-2), B1(-1),
    F1(1), F2(2), F3(3), F4(4), F5(5), F6(6), F7(7);

    private final int level;

    private Floor(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public static int toInt(String floorStr) {
        try {
            return Floor.valueOf(floorStr).getLevel();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid floor: " + floorStr);
        }
    }

    public static String toString(int level) {
        for (Floor f : Floor.values()) {
            if (f.getLevel() == level) {
                return f.name();
            }
        }
        throw new IllegalArgumentException("Invalid floor level: " + level);
    }

    public static int nextLevel(String curFloor, int direction) {
        int tmp = Floor.toInt(curFloor);
        if (tmp == 1 && direction == -1) {
            tmp = -1;
        } else if (tmp == -1 && direction == 1) {
            tmp = 1;
        } else {
            tmp = tmp + direction;
        }
        return tmp;
    }

    public static boolean inRange(int curFloor, int a, int b) {
        return (curFloor >= a && curFloor <= b && curFloor != 0);
    }
}
