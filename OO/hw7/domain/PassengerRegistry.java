package domain;

import java.util.concurrent.ConcurrentHashMap;

public class PassengerRegistry {
    private static final ConcurrentHashMap<Integer, String> FINAL_TO_FLOOR =
            new ConcurrentHashMap<>();

    private PassengerRegistry() {

    }

    public static void registerFinalToFloor(int personId, String finalToFloor) {
        FINAL_TO_FLOOR.putIfAbsent(personId, finalToFloor);
    }

    public static String getFinalToFloor(int persongId) {
        return FINAL_TO_FLOOR.get(persongId);
    }
}
