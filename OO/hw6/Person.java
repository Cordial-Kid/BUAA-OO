public class Person {
    private final int id;
    private final String fromFloor;
    private final String toFloor;
    private final int direction; // 1代表向上，-1代表向下
    private final int weight;
    private final int elevatorId = -1;

    public Person(int id, String fromFloor, String toFloor, int weight) {
        this.id = id;
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;
        this.direction = Floor.toInt(toFloor) > Floor.toInt(fromFloor) ? 1 : -1;
        this.weight = weight;
    }

    public int getId() {
        return id;
    }

    public int getFromFloor() {
        return Floor.toInt(fromFloor);
    }

    public int getToFloor() {
        return Floor.toInt(toFloor);
    }

    public int getDirection() {
        return direction;
    }

    public int getWeight() {
        return weight;
    }

    public int getElevatorId() {
        return elevatorId;
    }
}