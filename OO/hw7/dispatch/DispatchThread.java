package dispatch;

import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.MaintRequest;
import com.oocourse.elevator3.RecycleRequest;
import com.oocourse.elevator3.UpdateRequest;
import com.oocourse.elevator3.TimableOutput;

import java.util.HashMap;

import domain.Floor;
import domain.PassengerRegistry;
import elevator.ElevState;
import elevator.Elevator;
import queue.RequestQueue;

public class DispatchThread extends Thread {
    private static final int MAIN_MIN_FLOOR = -4;
    private static final int MAIN_MAX_FLOOR = 7;
    private static final int DOUBLE_MAIN_MIN_FLOOR = 2;
    private static final int DOUBLE_MAIN_MAX_FLOOR = 7;
    private static final int DOUBLE_BACKUP_MIN_FLOOR = -4;
    private static final int DOUBLE_BACKUP_MAX_FLOOR = 2;
    private static final int EXCHANGE_FLOOR = 2;

    private static final int MOVE_WEIGHT = 560;
    private static final int QUEUE_WEIGHT = 620;
    private static final int QUEUE_SQUARE_WEIGHT = 130;
    private static final int WEIGHT_WEIGHT = 5;
    private static final int SAME_DIR_BONUS = 620;
    private static final int OPPOSITE_DIR_PENALTY = 900;
    private static final int IDLE_BONUS = 300;
    private static final int SAME_FLOOR_BONUS = 1000;
    private static final int TRANSFER_SEGMENT_BONUS = 480;
    private static final int DOUBLE_REGION_BONUS = 180;
    private static final int SPECIAL_STATE_DELAY_PENALTY = 1600;

    private final RequestQueue requests;
    private final HashMap<Integer, RequestQueue> queueMap;
    private final HashMap<Integer, Boolean> isBusyMap;
    private final HashMap<Integer, Elevator> elevatorMap;

    public DispatchThread(RequestQueue requests,
                          HashMap<Integer, RequestQueue> queueMap,
                          HashMap<Integer, Boolean> isMainTainMap,
                          HashMap<Integer, Elevator> elevatorMap) {
        this.requests = requests;
        this.queueMap = queueMap;
        this.isBusyMap = isMainTainMap;
        this.elevatorMap = elevatorMap;
    }

    @Override
    public void run() {
        while (true) {
            if (requests.isEmpty() && requests.isEnd() && !isAnyEleMainTain()) {
                for (RequestQueue queue : queueMap.values()) {
                    queue.setEnd();
                }
                break;
            }
            Request request = requests.poll();
            if (request == null) {
                sleepBriefly();
                continue;
            }
            dispatch(request);
        }
    }

    // 分配算法是大头
    private void dispatch(Request request) {
        if (request instanceof MaintRequest) {
            int elevatorId = ((MaintRequest) request).getElevatorId();
            markBusy(elevatorId);
            queueMap.get(elevatorId).offer(request);
        } else if (request instanceof UpdateRequest) {
            int elevatorId = ((UpdateRequest) request).getElevatorId();
            markBusy(elevatorId);
            queueMap.get(elevatorId).offer(request);
        } else if (request instanceof RecycleRequest) {
            int elevatorId = ((RecycleRequest) request).getElevatorId();
            markBusy(elevatorId);
            queueMap.get(elevatorId).offer(request);
        } else if (request instanceof PersonRequest) {
            dispatchPerson(request);
        }
    }

    private void dispatchPerson(Request request) {
        PersonRequest preq = (PersonRequest) request;
        PassengerRegistry.registerFinalToFloor(preq.getPersonId(), preq.getToFloor());
        while (true) {
            PersonRequest tmp = null;
            int elevatorId = chooseDirect(preq);
            if (elevatorId == -1 && needTransfer(preq)) {
                tmp = chooseTransfer(preq);
                elevatorId = chooseDirect(tmp);
            } else {
                sleepBriefly();   // 如果单纯是体重原因就睡一会儿
            }
            if (elevatorId != -1) {
                ElevState state = elevatorMap.get(elevatorId).getEleState();
                if (state == ElevState.NORMAL || state == ElevState.DOUBLE) {
                     TimableOutput.println("RECEIVE-" + preq.getPersonId() + "-" + elevatorId);
                    if (tmp != null) {
                        queueMap.get(elevatorId).offer(tmp);
                    } else {
                        queueMap.get(elevatorId).offer(request);
                    }
                    return;
                } else if (state == ElevState.REPAIR
                        || state == ElevState.TEST
                        || state == ElevState.UPDATE) {
                    if (tmp != null) {
                        queueMap.get(elevatorId).offer(tmp);
                    } else {
                        queueMap.get(elevatorId).offer(request);
                    }
                    return;
                } else {
                    sleepBriefly();
                }
            }
        }
    }

    private int chooseDirect(PersonRequest preq) {
        int elevatorId = -1;
        int minCost = Integer.MAX_VALUE;
        for (int i = 1; i <= 12; i++) {
            Elevator elevator = elevatorMap.get(i);
            ElevState s = elevator.getEleState();
            if (s == ElevState.DORMANT || s == ElevState.REC_ACCEPT || s == ElevState.RECYCLE) {
                continue;
            }
            boolean isMainTain;
            if (s == ElevState.NORMAL || s == ElevState.DOUBLE) {
                isMainTain = false;
            } else {
                isMainTain = true;
            }
            int stayFloor = calcStayFloor(elevator.getEleState());
            int cost = calculateCost(elevator, preq, isMainTain, stayFloor);
            if (cost < minCost) {
                minCost = cost;
                elevatorId = i;
            }
        }
        return elevatorId;
    }

    private PersonRequest chooseTransfer(PersonRequest preq) {
        PersonRequest tmp = new PersonRequest(preq.getFromFloor(),
                "F2",
                preq.getPersonId(),
                preq.getWeight());
        return tmp;
    }

    private boolean needTransfer(PersonRequest preq) {
        int fromFloor = Floor.toInt(preq.getFromFloor());
        int toFloor = Floor.toInt(preq.getToFloor());
        return ((fromFloor < 2) && (toFloor > 2)) || ((fromFloor > 2) && (toFloor < 2));
    }

    private void sleepBriefly() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private int calcStayFloor(ElevState elevatorState) {
        int tmp;
        switch (elevatorState) {
            case REP_ACCEPT:
            case REPAIR:
            case TEST:
            case REC_ACCEPT:
            case RECYCLE:
                tmp = 1;
                break;
            case UP_ACCEPT:
            case UPDATE:
                tmp = 3;
                break;
            default:
                tmp = 0;
        }
        return tmp;
    }

    /*
    private int calculateCost(Elevator elevator, PersonRequest preq,
                              boolean isMainTain, int stayFloor) {
        if (!elevator.canReceivePassenger(preq)) {
            return Integer.MAX_VALUE;
        }
        // 容量一票否决：装不下直接 Pass
        if (elevator.getPeoWeight() + preq.getWeight() > 400) {
            return Integer.MAX_VALUE;
        }

        int eleFloor = isMainTain ? stayFloor : Floor.toInt(elevator.getCurFloor());
        int reqFromFloor = Floor.toInt(preq.getFromFloor());
        int reqToFloor = Floor.toInt(preq.getToFloor());

        final int reqDirection = reqToFloor > reqFromFloor ? 1 : -1;
        int elevatorDirection = elevator.getDirection();

        // 真实折返距离与运行代价
        int distanceCost = 0;
        int positionGap = reqFromFloor - eleFloor;

        if (positionGap == 0) {
            distanceCost = 0; // 同层
        } else if (positionGap * elevatorDirection > 0) {
            // 顺路驶来,直接算层数差
            distanceCost = Math.abs(positionGap);
        } else {
            // 背道而驰,算跑到尽头再折返回来的距离
            int extremeFloor = (elevatorDirection == 1) ? 7 : -4;
            distanceCost = Math.abs(extremeFloor - eleFloor)
                    + Math.abs(extremeFloor - reqFromFloor);
        }

        // 距离成本
        int cost = distanceCost * MOVE_WEIGHT;

        // 电梯越重，接新客的意愿越低。
        int currentWeight = elevator.getPeoWeight();
        cost += currentWeight * WEIGHT_WEIGHT;

        if (positionGap == 0) {
            cost -= SAME_FLOOR_BONUS;
        }

        // 防止某个电梯分了太多人导致
        int queueSize = queueMap.get(elevator.getElevatorId()).size();
        cost += queueSize * QUEUE_WEIGHT + queueSize * queueSize * QUEUE_SQUARE_WEIGHT;

        // 方向微调
        if (reqDirection == elevatorDirection) {
            cost -= SAME_DIR_BONUS; // 同向奖励
        } else {
            cost += OPPOSITE_DIR_PENALTY; // 反向惩罚
        }

        // 绝对空闲奖励最高优先级
        if (currentWeight == 0 && queueSize == 0) {
            cost -= IDLE_BONUS; // 空白电梯出动奖励
        }

        return Math.max(cost, 0);
    }

    */

    private int calculateCost(Elevator elevator, PersonRequest preq,
                              boolean isMainTain, int stayFloor) {
        if (!elevator.canReceivePassenger(preq)) {
            return Integer.MAX_VALUE;
        }
        if (elevator.getPeoWeight() + preq.getWeight() > 400) {
            return Integer.MAX_VALUE;
        }

        int eleFloor = isMainTain ? stayFloor : Floor.toInt(elevator.getCurFloor());
        int reqFromFloor = Floor.toInt(preq.getFromFloor());
        int reqToFloor = Floor.toInt(preq.getToFloor());
        int minFloor = getMinReachableFloor(elevator);
        int maxFloor = getMaxReachableFloor(elevator);
        int elevatorDirection = getEffectiveDirection(elevator,
                isMainTain, eleFloor, reqFromFloor, reqToFloor);

        int pickupDistance = estimatePickupDistance(eleFloor,
                reqFromFloor, elevatorDirection, minFloor, maxFloor);
        int positionGap = reqFromFloor - eleFloor;
        final int requestDirection = reqToFloor > reqFromFloor ? 1 : -1;
        int cost = pickupDistance * MOVE_WEIGHT;

        int currentWeight = elevator.getPeoWeight();
        int queueSize = queueMap.get(elevator.getElevatorId()).size();
        cost += currentWeight * WEIGHT_WEIGHT;
        cost += queueSize * QUEUE_WEIGHT + queueSize * queueSize * QUEUE_SQUARE_WEIGHT;

        if (positionGap == 0) {
            cost -= SAME_FLOOR_BONUS;
        }
        if (isOnCurrentRoute(eleFloor, reqFromFloor, elevatorDirection)
                && requestDirection == elevatorDirection) {
            cost -= SAME_DIR_BONUS;
        } else if (requestDirection != elevatorDirection) {
            cost += OPPOSITE_DIR_PENALTY;
        }
        if (currentWeight == 0 && queueSize == 0) {
            cost -= IDLE_BONUS;
        }
        if (isDoubleRegionElevator(elevator)) {
            cost -= DOUBLE_REGION_BONUS;
            if (reqFromFloor == EXCHANGE_FLOOR || reqToFloor == EXCHANGE_FLOOR) {
                cost -= TRANSFER_SEGMENT_BONUS;
            }
        }
        if (isMainTain) {
            cost += SPECIAL_STATE_DELAY_PENALTY;
        }

        return Math.max(cost, 0);
    }

    private int estimatePickupDistance(int eleFloor, int reqFromFloor,
                                       int direction, int minFloor, int maxFloor) {
        if (eleFloor == reqFromFloor) {
            return 0;
        }
        if (isOnCurrentRoute(eleFloor, reqFromFloor, direction)) {
            return floorDistance(eleFloor, reqFromFloor);
        }

        int extremeFloor = direction == 1 ? maxFloor : minFloor;
        return floorDistance(eleFloor, extremeFloor)
                + floorDistance(extremeFloor, reqFromFloor);
    }

    private boolean isOnCurrentRoute(int eleFloor, int reqFromFloor, int direction) {
        return eleFloor == reqFromFloor || (reqFromFloor - eleFloor) * direction > 0;
    }

    private int floorDistance(int fromFloor, int toFloor) {
        if (fromFloor == toFloor) {
            return 0;
        }
        if (fromFloor < 0 && toFloor > 0) {
            return Math.abs(fromFloor) + toFloor - 1;
        }
        if (fromFloor > 0 && toFloor < 0) {
            return fromFloor + Math.abs(toFloor) - 1;
        }
        return Math.abs(fromFloor - toFloor);
    }

    private int getEffectiveDirection(Elevator elevator, boolean isMainTain,
                                      int eleFloor, int reqFromFloor,
                                      int reqToFloor) {
        if (!isMainTain) {
            return elevator.getDirection();
        }
        if (reqFromFloor > eleFloor) {
            return 1;
        }
        if (reqFromFloor < eleFloor) {
            return -1;
        }
        return reqToFloor > reqFromFloor ? 1 : -1;
    }

    private int getMinReachableFloor(Elevator elevator) {
        if (isBackupElevator(elevator)) {
            return DOUBLE_BACKUP_MIN_FLOOR;
        }
        if (isDoubleRegionElevator(elevator)) {
            return DOUBLE_MAIN_MIN_FLOOR;
        }
        return MAIN_MIN_FLOOR;
    }

    private int getMaxReachableFloor(Elevator elevator) {
        if (isBackupElevator(elevator)) {
            return DOUBLE_BACKUP_MAX_FLOOR;
        }
        if (isDoubleRegionElevator(elevator)) {
            return DOUBLE_MAIN_MAX_FLOOR;
        }
        return MAIN_MAX_FLOOR;
    }

    private boolean isDoubleRegionElevator(Elevator elevator) {
        ElevState state = elevator.getEleState();
        return isBackupElevator(elevator)
                || state == ElevState.DOUBLE
                || state == ElevState.UP_ACCEPT
                || state == ElevState.UPDATE;
    }

    private boolean isBackupElevator(Elevator elevator) {
        return elevator.getElevatorId() > 6;
    }

    private boolean isAnyEleMainTain() {
        synchronized (isBusyMap) {
            for (int i = 1; i <= 12; i++) {
                if (Boolean.TRUE.equals(isBusyMap.get(i))) {
                    return true;
                }
            }
        }
        for (int i = 1; i <= 12; i++) {
            if (elevatorMap.get(i).getPeoWeight() > 0) {
                return true;
            }
            if (!queueMap.get(i).isEmpty()) {
                return true;
            }
            if (elevatorMap.get(i).isHandlingPassenger()) {
                return true;
            }
        }
        return false;
    }

    private void markBusy(int elevatorId) {
        synchronized (isBusyMap) {
            isBusyMap.put(elevatorId, true);
        }
    }
}
