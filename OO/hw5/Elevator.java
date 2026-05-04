import java.util.ArrayList;
import java.util.Iterator;

import com.oocourse.elevator1.TimableOutput;

public class Elevator extends Thread {
    private final int id;
    private final RequestQueue requestqueue;
    private int direction;
    private String curFloor;
    private int peoWeight;
    private final Strategy strategy;
    private final ArrayList<Person> persons = new ArrayList<>();

    public Elevator(int id, RequestQueue requestQueue) {
        this.id = id;
        this.requestqueue = requestQueue;
        this.curFloor = "F1";
        this.direction = 1;   // 1上-1下
        this.peoWeight = 0;
        this.strategy = new Strategy(requestqueue, persons);
    }

    @Override
    public void run() {
        while (true) {
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
                    return;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    private void reverse() {
        direction *= -1;
    }

    private void move() {
        try {
            sleep(400);
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
        candidates.sort((p1,p2) -> {
            int tmp1 = p1.getWeight();
            int tmp2 = p2.getWeight();
            return Integer.compare(tmp1,tmp2);
        });
        for (Person person : candidates) {
            if (person.getWeight() + peoWeight <= 400) {
                requestqueue.remove(person);
                TimableOutput.println("IN-"
                        + person.getId()
                        + "-" + curFloor
                        + "-" + person.getElevatorId());
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
}