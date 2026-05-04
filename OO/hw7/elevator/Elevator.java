package elevator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.oocourse.elevator3.MaintRequest;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.TimableOutput;
import com.oocourse.elevator3.UpdateRequest;
import com.oocourse.elevator3.RecycleRequest;

import domain.Floor;
import domain.Person;
import queue.RequestQueue;

import com.oocourse.elevator3.PersonRequest;

public class Elevator extends Thread {
    private static final int MAIN_MAX_FLOOR = 7;
    private static final int MAIN_MIN_FLOOR = -4;
    private static final int DOUBLE_MAIN_MAX_FLOOR = 7;
    private static final int DOUBLE_MAIN_MIN_FLOOR = 2;
    private static final int DOUBLE_BACKUP_MAX_FLOOR = 2;
    private static final int DOUBLE_BACKUP_MIN_FLOOR = -4;

    private final int id;
    private volatile int direction;
    private volatile String curFloor;
    private volatile int peoWeight;
    private final Strategy strategy;
    private final ArrayList<Person> persons = new ArrayList<>();    // 电梯内的乘客
    private MaintRequest maintRequest = null;    // 维修队列
    private UpdateRequest updateRequest = null;
    private RecycleRequest recycleRequest = null;
    private int moveTime = 400;
    private final HashMap<Integer, Boolean> isBusyMap;
    private final RequestQueue requestqueue;    // 电梯自己的乘客等候队列
    private final RequestQueue globalQueue;
    private final ShaftContext shaftContext;          // 维护当前电梯所处的轿厢变量
    private final boolean mainEle;
    private volatile ElevState state;
    private volatile boolean handlingPassenger = false;

    public Elevator(int id,
                    RequestQueue requestQueue,
                    RequestQueue globalQueue,
                    HashMap<Integer, Boolean> isMainTainMap,
                    ShaftContext shaftContext,
                    boolean mainEle) {
        this.id = id;
        this.requestqueue = requestQueue;
        this.curFloor = "F1";
        this.direction = 1;   // 1上-1下
        this.peoWeight = 0;
        this.strategy = new Strategy(requestqueue, persons);
        this.globalQueue = globalQueue;
        this.isBusyMap = isMainTainMap;
        this.shaftContext = shaftContext;
        this.mainEle = mainEle;
        this.state = mainEle ? ElevState.NORMAL : ElevState.DORMANT;
    }

    @Override
    public void run() {
        while (true) {
            syncStateWithShaft();
            checkSpecialRequest();

            switch (state) {
                case NORMAL:
                case DOUBLE:
                    if (!runFluent()) {
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
                case UP_ACCEPT:
                    runUpAccept();
                    break;
                case UPDATE:
                    runUpdate();
                    break;
                case REC_ACCEPT:
                    runRecAccept();
                    break;
                case RECYCLE:
                    runRecycle();
                    break;
                case DORMANT:
                    // normal 态和 dormant都可以结束循环
                    if (!runDormant()) {
                        return;
                    }
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    private void syncStateWithShaft() {
        if (mainEle
                && state == ElevState.DOUBLE
                && shaftContext.getShaftState() == ShaftState.NORMAL) {
            state = ElevState.NORMAL;
        }
        if (!mainEle
                && state == ElevState.DORMANT
                && shaftContext.getShaftState() == ShaftState.DOUBLE) {
            state = ElevState.DOUBLE;
        }
    }

    private void checkSpecialRequest() {
        if (mainEle && state == ElevState.NORMAL) {
            MaintRequest maintRequest = requestqueue.extractMaintainRequest();
            if (maintRequest != null) {
                this.maintRequest = maintRequest;
                this.state = ElevState.REP_ACCEPT;
                return;
            }
            UpdateRequest updateRequest = requestqueue.extractUpdateRequest();
            if (updateRequest != null) {
                this.updateRequest = updateRequest;
                this.state = ElevState.UP_ACCEPT;
                this.shaftContext.setShaftState(ShaftState.UP_ACCEPT);
                return;
            }
        } else if (!mainEle && state == ElevState.DOUBLE) {
            RecycleRequest recycleRequest = requestqueue.extractRecycleRequest();
            if (recycleRequest != null) {
                this.recycleRequest = recycleRequest;
                this.state = ElevState.REC_ACCEPT;
                this.shaftContext.setShaftState(ShaftState.REC_ACCEPT);
            }
        }
    }

    private boolean runFluent() {
        Com com = strategy.getCommand(curFloor,
                direction,
                peoWeight,
                getMinReachableFloor(),
                getMaxReachableFloor());
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
                if (tryLeaveExchangeFloor()) {
                    break;
                }
                synchronized (requestqueue) {
                    try {
                        requestqueue.wait(20);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            case END:
                if (tryLeaveExchangeFloor()) {
                    break;
                }
                return false;
            default:
                throw new IllegalArgumentException();
        }
        return true;
    }

    //维修的时候保证只有一个电梯在工作
    private void runRepAccept() {
        int curLevel = Floor.toInt(curFloor);
        if (curLevel != 1) {
            direction = curLevel > 1 ? -1 : 1;
            move();
        } else {
            TimableOutput.println("OPEN-F1-" + id);
            sleepBriefly(400);
            PassengerFlowService.ejectAllPassengers(persons, curFloor, id);
            TimableOutput.println("IN-" + maintRequest.getWorkerId() + "-" + curFloor + "-" + id);
            TimableOutput.println("CLOSE-" + curFloor + "-" + id);
            TimableOutput.println("MAINT1-BEGIN-" + id);
            state = ElevState.REPAIR;
            letReceive("F1");
        }
    }

    private void runRepair() {
        sleepBriefly(1000);
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
        sleepBriefly(400);
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
        markNotBusy();
    }

    private void runUpAccept() {
        int curLevel = Floor.toInt(curFloor);
        if (curLevel != 3) {
            direction = curLevel > 3 ? -1 : 1;
            move();
            if (PassengerFlowService.canPassengerOut(peoWeight, persons, curFloor)) {
                openForOut();
            }
        } else {
            if (!persons.isEmpty()) {
                TimableOutput.println("OPEN-F3-" + id);
                sleepBriefly(400);
                PassengerFlowService.ejectAllPassengers(persons, curFloor, id);
                TimableOutput.println("CLOSE-" + curFloor + "-" + id);
            }
            TimableOutput.println("UPDATE-BEGIN-" + id);
            state = ElevState.UPDATE;
            shaftContext.setShaftState(ShaftState.UPDATE);
            letReceive("F3");
        }
    }

    // 激活的时候电梯可以接任务，但要等到UP_ACCEPT结束才行
    private void runUpdate() {
        sleepBriefly(1000);
        TimableOutput.println("UPDATE-END-" + id);
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
        updateRequest = null;
        state = ElevState.DOUBLE;
        shaftContext.setShaftState(ShaftState.DOUBLE);
        shaftContext.updateFloor(id, curFloor);
        markNotBusy();
    }

    private void runRecAccept() {
        int curLevel = Floor.toInt(curFloor);
        if (curLevel != 1) {
            direction = curLevel > 1 ? -1 : 1;
            move();
            if (PassengerFlowService.canPassengerOut(peoWeight, persons, curFloor)) {
                openForOut();
            }
        } else {
            if (!persons.isEmpty()) {
                TimableOutput.println("OPEN-F1-" + id);
                sleepBriefly(400);
                PassengerFlowService.ejectAllPassengers(persons, curFloor, id);
                TimableOutput.println("CLOSE-" + curFloor + "-" + id);
            }
            TimableOutput.println("RECYCLE-BEGIN-" + id);
            state = ElevState.RECYCLE;
            shaftContext.setShaftState(ShaftState.RECYCLE);
            letReceive("F1");
        }
    }

    // 回收的时候电梯不能接任务
    private void runRecycle() {
        sleepBriefly(1000);
        TimableOutput.println("RECYCLE-END-" + id);
        recycleRequest = null;
        state = ElevState.DORMANT;
        shaftContext.setShaftState(ShaftState.NORMAL);
        markNotBusy();
    }

    // 只有备用电梯会来runDormant
    private boolean runDormant() {
        while ((shaftContext.getShaftState() != ShaftState.DOUBLE) && !requestqueue.isEnd()) {
            sleepBriefly(10);
        }
        if (shaftContext.getShaftState() == ShaftState.DOUBLE) {
            curFloor = "F1";
            direction = 1;
            state = ElevState.DOUBLE;
            shaftContext.updateFloor(id, curFloor);
            return true;
        }
        return false;
    }

    private void reverse() {
        direction *= -1;
    }

    private void move() {
        int tmp = Floor.nextLevel(curFloor, direction);
        if (!Floor.inRange(tmp, getMinReachableFloor(), getMaxReachableFloor())) {
            reverse();
            tmp = Floor.nextLevel(curFloor, direction);
        }
        sleepBriefly(moveTime);
        curFloor = shaftContext.arrive(id, tmp);
    }

    private void open() {
        handlingPassenger = true;
        TimableOutput.println("OPEN-" + curFloor + "-" + id);
        peoWeight = PassengerFlowService.passengerOut(persons,
                curFloor,
                peoWeight,
                id,
                globalQueue);
        sleepBriefly(400);

        Com com = strategy.getCommand(curFloor,
                direction,
                peoWeight,
                getMinReachableFloor(),
                getMaxReachableFloor());
        if (com == Com.REVERSE) {
            direction *= -1;
        }
        peoWeight = PassengerFlowService.passengerIn(requestqueue,
                curFloor,
                direction,
                peoWeight,
                id,
                persons);
        TimableOutput.println("CLOSE-" + curFloor + "-" + id);
        handlingPassenger = false;
    }

    private void openForOut() {
        handlingPassenger = true;
        TimableOutput.println("OPEN-" + curFloor + "-" + id);
        peoWeight = PassengerFlowService.passengerOut(persons,
                curFloor,
                peoWeight,
                id,
                globalQueue);
        sleepBriefly(400);
        TimableOutput.println("CLOSE-" + curFloor + "-" + id);
        handlingPassenger = false;
    }

    // 只有double和normal的时候才需要连续运行
    private int getMinReachableFloor() {
        if (!mainEle) {
            return DOUBLE_BACKUP_MIN_FLOOR;
        } else {
            ShaftState shaftState = shaftContext.getShaftState();
            if (shaftState == ShaftState.DOUBLE
                    || shaftState == ShaftState.RECYCLE
                    || shaftState == ShaftState.REC_ACCEPT) {
                return DOUBLE_MAIN_MIN_FLOOR;
            }
        }
        return MAIN_MIN_FLOOR;
    }

    private int getMaxReachableFloor() {
        if (!mainEle) {
            return DOUBLE_BACKUP_MAX_FLOOR;
        }
        return shaftContext.getShaftState() == ShaftState.DOUBLE ?
                DOUBLE_MAIN_MAX_FLOOR : MAIN_MAX_FLOOR;
    }

    // 不仅返回结果还要执行动作
    private boolean tryLeaveExchangeFloor() {
        if (shaftContext.getShaftState() == ShaftState.DOUBLE && Floor.toInt(curFloor) == 2) {
            direction = mainEle ? 1 : -1;
            move();
            return true;
        }
        return false;
    }

    private void letReceive(String floor) {
        Iterator<Person> iterator = persons.iterator();
        while (iterator.hasNext()) {
            Person p = iterator.next();
            peoWeight = peoWeight - p.getWeight();
            PassengerFlowService.removeAndOffer(p, floor, globalQueue);
            iterator.remove();
        }
        // 清理该电梯的等候队列人数
        ArrayList<PersonRequest> waitingQueue = requestqueue.clearWaitingQueue();
        for (PersonRequest req : waitingQueue) {
            globalQueue.offer(req);
        }
    }

    private void markNotBusy() {
        synchronized (isBusyMap) {
            isBusyMap.put(id, false);
            isBusyMap.notifyAll();
        }
    }

    private void sleepBriefly(int time) {
        try {
            sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean canReceivePassenger(PersonRequest preq) {
        int fromFloor = Floor.toInt(preq.getFromFloor());
        int toFloor = Floor.toInt(preq.getToFloor());
        if (mainEle) {
            if (state == ElevState.NORMAL
                    || state == ElevState.REPAIR
                    || state == ElevState.TEST
                    || state == ElevState.REP_ACCEPT) {
                return Floor.inRange(fromFloor, MAIN_MIN_FLOOR, MAIN_MAX_FLOOR)
                        && Floor.inRange(toFloor, MAIN_MIN_FLOOR, MAIN_MAX_FLOOR);
            } else if (state == ElevState.DOUBLE
                    || state == ElevState.UPDATE
                    || state == ElevState.UP_ACCEPT) {
                return Floor.inRange(fromFloor, DOUBLE_MAIN_MIN_FLOOR, DOUBLE_MAIN_MAX_FLOOR)
                        && Floor.inRange(toFloor, DOUBLE_MAIN_MIN_FLOOR, DOUBLE_MAIN_MAX_FLOOR);
            }
        } else if (!mainEle && state == ElevState.DOUBLE) {
            return Floor.inRange(fromFloor, DOUBLE_BACKUP_MIN_FLOOR, DOUBLE_BACKUP_MAX_FLOOR)
                    && Floor.inRange(toFloor, DOUBLE_BACKUP_MIN_FLOOR, DOUBLE_BACKUP_MAX_FLOOR);
        }
        return false;
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

    public boolean isHandlingPassenger() {
        return handlingPassenger;
    }
}