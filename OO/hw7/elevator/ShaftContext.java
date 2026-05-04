package elevator;

import com.oocourse.elevator3.TimableOutput;
import domain.Floor;

public class ShaftContext {
    private final int mainId;
    private final int backupId;
    private ShaftState shaftState = ShaftState.NORMAL;
    private String mainFloor = "F1";
    private String backupFloor = "F1";

    public ShaftContext(int mainId) {
        this.mainId = mainId;
        this.backupId = mainId + 6;
    }

    public int getMainId() {
        return mainId;
    }

    public int getBackupId() {
        return backupId;
    }

    public synchronized ShaftState getShaftState() {
        return shaftState;
    }

    public synchronized void setShaftState(ShaftState shaftState) {
        this.shaftState = shaftState;
        notifyAll();               // 这里的notifyall考虑一下为什么
    }

    public synchronized void updateFloor(int elevatorId, String floor) {
        if (elevatorId == mainId) {
            this.mainFloor = floor;
        } else {
            this.backupFloor = floor;
        }
        notifyAll();
    }

    public synchronized String arrive(int elevatorId, int targetFloor) {
        while (!canArrive(elevatorId, targetFloor)) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        String floor = Floor.toString(targetFloor);
        if (elevatorId == mainId) {
            this.mainFloor = floor;
        } else {
            this.backupFloor = floor;
        }
        notifyAll();
        TimableOutput.println("ARRIVE-" + floor + "-" + elevatorId);
        return floor;
    }

    private boolean canArrive(int elevatorId, int targetFloor) {
        if (!((shaftState == ShaftState.DOUBLE) || (shaftState == ShaftState.REC_ACCEPT))) {
            return true;
        }
        if (!(targetFloor == 2)) {
            return true;
        } else {
            int tmp = Floor.toInt(getAnotherFloor(elevatorId));
            if (tmp != 2) {
                return true;
            }
        }
        return false;
    }

    private String getAnotherFloor(int elevatorId) {
        return elevatorId == mainId ? backupFloor : mainFloor;
    }

}
