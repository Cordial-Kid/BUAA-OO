import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.oocourse.elevator2.MaintRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.TimableOutput;
import com.oocourse.elevator2.PersonRequest;

public class Elevator extends Thread {
    private final int id;
    private volatile int direction;
    private volatile String curFloor;
    private volatile int peoWeight;
    private final Strategy strategy;
    private final ArrayList<Person> persons = new ArrayList<>();    // 电梯内的乘客
    private volatile ElevState state = ElevState.NORMAL;
    private MaintRequest maintRequest = null;                      // 维修队列
    private int moveTime = 400;
    private final HashMap<Integer, Boolean> isMainTainMap;
    private final RequestQueue requestqueue;    // 电梯自己的乘客等候队列
    private final RequestQueue globalQueue;

    public Elevator(int id,
                    RequestQueue requestQueue,
                    RequestQueue globalQueue,
                    HashMap<Integer, Boolean> isMainTainMap) {
        this.id = id;
        this.requestqueue = requestQueue;
        this.curFloor = "F1";
        this.direction = 1;   // 1上-1下
        this.peoWeight = 0;
        this.strategy = new Strategy(requestqueue, persons);
        this.globalQueue = globalQueue;
        this.isMainTainMap = isMainTainMap;
    }

    @Override
    public void run() {
        while (true) {
            checkMainTain();

            switch (state) {
                case NORMAL:
                    if (!runNormal()) {
                        return;
                    }
                    break;
                case REP_ACCEPT:
                    runRepAccept();
                    break;
                case REPAIR:
                    runRepair();
                    break;
                case TEST:
                    runTest();
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    private void checkMainTain() {
        if (state == ElevState.NORMAL) {
            MaintRequest maintRequest = requestqueue.extractMaintainRequest();
            if (maintRequest != null) {
                this.maintRequest = maintRequest;
                this.state = ElevState.REP_ACCEPT;
            }
        }
    }

    private boolean runNormal() {
        Com com = strategy.getCommand(curFloor, direction, peoWeight);
        switch (com) {
            case OPEN:
                open();
                break;
            case MOVE:
                move();
                break;
            case REVERSE:
                reverse();
                break;
            case WAIT:
                synchronized (requestqueue) {
                    try {
                        requestqueue.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            case END:
                return false;
            default:
                throw new IllegalArgumentException();
        }
        return true;
    }

    private void runRepAccept() {
        int curLevel = Floor.toInt(curFloor);
        if (curLevel != 1) {
            direction = curLevel > 1 ? -1 : 1;
            move();
            if (canPassengerOut()) {
                TimableOutput.println("OPEN-" + curFloor + "-" + id);
                passengerOut();
                try {
                    sleep(400);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                TimableOutput.println("CLOSE-" + curFloor + "-" + id);
            }
        } else {
            TimableOutput.println("OPEN-F1-" + id);
            try {
                sleep(400);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            ejectAllPassengers();

            TimableOutput.println("IN-" + maintRequest.getWorkerId() + "-" + curFloor + "-" + id);
            TimableOutput.println("CLOSE-" + curFloor + "-" + id);
            TimableOutput.println("MAINT1-BEGIN-" + id);
            state = ElevState.REPAIR;
            letReceive();
        }
    }

    private void runRepair() {
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }

        TimableOutput.println("MAINT2-BEGIN-" + id);
        state = ElevState.TEST;
    }

    private void runTest() {
        this.moveTime = 200;
        int targetLevel = Floor.toInt(maintRequest.getToFloor());
        while (Floor.toInt(curFloor) != targetLevel) {
            direction = targetLevel > 1 ? 1 : -1;
            move();
        }
        while (Floor.toInt(curFloor) != 1) {
            direction = targetLevel > 1 ? -1 : 1;
            move();
        }
        moveTime = 400;
        TimableOutput.println("OPEN-" + curFloor + "-" + id);
        try {
            sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 这里的顺序怎么看
        TimableOutput.println("OUT-S-" + maintRequest.getWorkerId() + "-" + "F1-" + id);
        TimableOutput.println("CLOSE-" + curFloor + "-" + id);
        TimableOutput.println("MAINT-END-" + id);
        if (!requestqueue.isEmpty()) {
            ArrayList<Request> requests = requestqueue.getRequests();
            for (Request request : requests) {
                PersonRequest preq = null;
                if (request instanceof PersonRequest) {
                    preq = (PersonRequest) request;
                }
                TimableOutput.println("RECEIVE-" + preq.getPersonId() + "-" + id);
            }
        }
        state = ElevState.NORMAL;

        this.maintRequest = null;
        synchronized (isMainTainMap) {
            isMainTainMap.put(id, false);
            isMainTainMap.notifyAll();
        }
    }

    private boolean canPassengerOut() {
        if (peoWeight == 0) {
            return false;
        }
        for (Person person : persons) {
            if (person.getToFloor() == Floor.toInt(curFloor)) {
                return true;
            }
        }
        return false;
    }

    private void ejectAllPassengers() {
        Iterator<Person> iterator = persons.iterator();
        while (iterator.hasNext()) {
            Person p = iterator.next();
            if (p.getToFloor() == 1) {
                TimableOutput.println("OUT-S-" + p.getId() + "-" + curFloor + "-" + id);
            } else {
                TimableOutput.println("OUT-F-" + p.getId() + "-" + curFloor + "-" + id);
            }
        }
    }

    private void letReceive() {
        Iterator<Person> iterator = persons.iterator();
        while (iterator.hasNext()) {
            Person p = iterator.next();
            if (p.getToFloor() == 1) {
                peoWeight = peoWeight - p.getWeight();
                iterator.remove();
            } else {
                peoWeight = peoWeight - p.getWeight();
                globalQueue.offer(new PersonRequest("F1",
                        Floor.toString(p.getToFloor()),
                        p.getId(),
                        p.getWeight()));
                iterator.remove();
            }
        }
        // 清理该电梯的等候队列人数
        ArrayList<PersonRequest> waitingQueue = requestqueue.clearWaitingQueue();
        for (PersonRequest req : waitingQueue) {
            globalQueue.offer(req);
        }
    }

    private void reverse() {
        direction *= -1;
    }

    private void move() {
        try {
            // sleep时被强制唤醒会报错，需要try
            sleep(moveTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        int tmp = Floor.toInt(curFloor);
        if (tmp == 1 && direction == -1) {
            tmp = -1;
        } else if (tmp == -1 && direction == 1) {
            tmp = 1;
        } else {
            tmp = tmp + direction;
        }
        curFloor = Floor.toString(tmp);
        TimableOutput.println("ARRIVE-" + curFloor + "-" + id);
    }

    private void open() {
        TimableOutput.println("OPEN-" + curFloor + "-" + id);
        passengerOut();
        try {
            sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Com com = strategy.getCommand(curFloor, direction, peoWeight);
        if (com == Com.REVERSE) {
            direction *= -1;
        }
        passengerIn();
        TimableOutput.println("CLOSE-" + curFloor + "-" + id);
    }

    private void passengerIn() {
        ArrayList<Person> candidates = requestqueue.getWaiting(curFloor, direction);
        if (candidates.isEmpty()) {
            return;
        }
        optimizeIn(candidates);
    }

    private void optimizeIn(ArrayList<Person> candidates) {
        candidates.sort((p1, p2) -> {
            int tmp1 = p1.getWeight();
            int tmp2 = p2.getWeight();
            return Integer.compare(tmp1, tmp2);
        });
        for (Person person : candidates) {
            if (person.getWeight() + peoWeight <= 400) {
                requestqueue.remove(person.getId());
                TimableOutput.println("IN-"
                        + person.getId()
                        + "-" + curFloor
                        + "-" + id);
                peoWeight = peoWeight + person.getWeight();
                persons.add(person);
            }
        }
    }

    private void passengerOut() {
        Iterator<Person> iterator = persons.iterator();
        while (iterator.hasNext()) {
            Person person = iterator.next();
            if (person.getToFloor() == Floor.toInt(curFloor)) {
                TimableOutput.println("OUT-S-" + person.getId() + "-" + curFloor + "-" + id);
                peoWeight = peoWeight - person.getWeight();
                iterator.remove();
            }
        }
    }

    public int getElevatorId() {
        return id;
    }

    public String getCurFloor() {
        return curFloor;
    }

    public int getDirection() {
        return direction;
    }

    public int getPeoWeight() {
        return peoWeight;
    }

    public ElevState getEleState() {
        return this.state;
    }
}