import com.oocourse.elevator2.MaintRequest;
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;

import java.util.HashMap;

import com.oocourse.elevator2.TimableOutput;

public class DispatchThread extends Thread {
    private final RequestQueue requests;
    private final HashMap<Integer, RequestQueue> queueMap;
    private final HashMap<Integer, Boolean> isMainTainMap;
    private final HashMap<Integer, Elevator> elevatorMap;

    public DispatchThread(RequestQueue requests,
                          HashMap<Integer, RequestQueue> queueMap,
                          HashMap<Integer, Boolean> isMainTainMap,
                          HashMap<Integer, Elevator> elevatorMap) {
        this.requests = requests;
        this.queueMap = queueMap;
        this.isMainTainMap = isMainTainMap;
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
                continue;
            }
            dispatch(request);
        }
    }

    // 分配算法是大头
    private void dispatch(Request request) {
        if (request instanceof MaintRequest) {
            int elevatorId = ((MaintRequest) request).getElevatorId();
            synchronized (isMainTainMap) {
                isMainTainMap.put(elevatorId, true);
            }
            queueMap.get(elevatorId).offer(request);
        } else if (request instanceof PersonRequest) {
            PersonRequest preq = (PersonRequest) request;
            while (true) {
                int elevatorId = -1;
                int minCost = Integer.MAX_VALUE;
                for (int i = 1; i <= 6; i++) {
                    Elevator elevator = elevatorMap.get(i);
                    boolean isMainTain = elevator.getEleState() != ElevState.NORMAL;
                    int cost = calculateCost(elevator, preq, isMainTain);
                    if (cost < minCost) {
                        minCost = cost;
                        elevatorId = i;
                    }
                }
                if (elevatorMap.get(elevatorId).getEleState() == ElevState.NORMAL) {
                    TimableOutput.println("RECEIVE-" + preq.getPersonId() + "-" + elevatorId);
                    queueMap.get(elevatorId).offer(request);
                    return;
                } else if (elevatorMap.get(elevatorId).getEleState() != ElevState.REP_ACCEPT) {
                    queueMap.get(elevatorId).offer(request);
                    return;
                } else {
                    // 在REP_ACCEPT状态下不能放入队列，否则会被分走。
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private int calculateCost(Elevator elevator, PersonRequest preq, boolean isMainTain) {
        // 容量一票否决：装不下直接 Pass
        if (elevator.getPeoWeight() + preq.getWeight() > 400) {
            return Integer.MAX_VALUE;
        }

        int eleFloor = isMainTain ? 1 : Floor.toInt(elevator.getCurFloor());
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
        int cost = distanceCost * 400;

        // 电梯越重，接新客的意愿越低。
        int currentWeight = elevator.getPeoWeight();
        cost += currentWeight * 16;

        // 防止某个电梯分了太多人导致
        int queueSize = queueMap.get(elevator.getElevatorId()).size();
        cost += queueSize * 1000;

        // 方向微调
        if (reqDirection == elevatorDirection) {
            cost -= 400; // 同向奖励
        } else {
            cost += 400; // 反向惩罚
        }

        // 绝对空闲奖励最高优先级
        if (currentWeight == 0 && queueSize == 0) {
            cost -= 800; // 空白电梯出动奖励
        }

        return Math.max(cost, 0);
    }

    private boolean isAnyEleMainTain() {
        synchronized (isMainTainMap) {
            for (int i = 1; i <= 6; i++) {
                if (Boolean.TRUE.equals(isMainTainMap.get(i))) {
                    return true;
                }
            }
            return false;
        }
    }
}
