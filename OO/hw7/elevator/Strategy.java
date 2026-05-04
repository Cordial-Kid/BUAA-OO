package elevator;

import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;

import domain.Floor;
import domain.Person;
import queue.RequestQueue;

import java.util.ArrayList;

public class Strategy {
    private final RequestQueue requestqueue;
    private final ArrayList<Person> persons;

    public Strategy(RequestQueue requests, ArrayList<Person> persons) {
        this.requestqueue = requests;
        this.persons = persons;
    }

    public Com getCommand(String curFloor, int direction,
                          int curWeight, int minFloor, int maxFloor) {
        if (canOutElevator(curWeight, curFloor) || canInElevator(curWeight, curFloor, direction)) {
            return Com.OPEN;
        }
        if (curWeight != 0) {
            if (canMove(curFloor, direction, minFloor, maxFloor)) {
                return Com.MOVE;
            } else {
                return Com.REVERSE;
            }
        }
        if (!requestqueue.isEmpty()) {    // 同时有两把不一样的锁在一个函数里
            if (hasSameDir(curFloor, direction)
                    && canMove(curFloor, direction, minFloor, maxFloor)) {
                return Com.MOVE;
            } else {
                return Com.REVERSE;
            }
        } else {
            if (requestqueue.isEnd()) {
                return Com.END;
            } else {
                return Com.WAIT;
            }
        }
    }

    private boolean canMove(String curFloor, int direction, int minFloor, int maxFloor) {
        int nextLevel = Floor.nextLevel(curFloor, direction);
        return Floor.inRange(nextLevel, minFloor, maxFloor);
    }

    private boolean canOutElevator(int curWeight, String curFloor) {
        if (curWeight == 0) {
            return false;
        }
        for (Person person : persons) {
            if (person.getToFloor() == Floor.toInt(curFloor)) {
                return true;
            }
        }
        return false;
    }

    private boolean canInElevator(int curWeight, String fromFloor, int direction) {
        if (curWeight >= 400) {
            return false;
        }
        synchronized (requestqueue) {
            for (Request request : requestqueue.getRequests()) {
                if (!(request instanceof PersonRequest)) {
                    continue;
                }
                PersonRequest personRequest = (PersonRequest) request;
                if (personRequest.getFromFloor().equals(fromFloor)
                        && Strategy.getDirection(personRequest) == direction
                        && personRequest.getWeight() + curWeight <= 400) {
                    return true;
                }
            }
            return false;
        }
    }

    // 当前前进方向还有没有人等
    //怎么解决跨区域传递问题，在move时手动解决
    private boolean hasSameDir(String curFloor, int direction) {
        synchronized (requestqueue) {
            for (Request request : requestqueue.getRequests()) {
                if (!(request instanceof PersonRequest)) {
                    continue;
                }
                String from = ((PersonRequest) request).getFromFloor();
                int tmp1 = Floor.toInt(from) - Floor.toInt(curFloor);
                int tmp = tmp1 * direction;
                if (tmp > 0) {
                    return true;
                }
            }
            return false;
        }
    }

    private static int getDirection(PersonRequest request) {
        String from = request.getFromFloor();
        String to = request.getToFloor();
        return Floor.toInt(to) > Floor.toInt(from) ? 1 : -1;
    }
}
