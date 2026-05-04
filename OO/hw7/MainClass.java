import com.oocourse.elevator3.TimableOutput;

import dispatch.DispatchThread;
import elevator.Elevator;
import elevator.ShaftContext;
import input.InputThread;
import queue.RequestQueue;

import java.util.HashMap;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        RequestQueue requestQueue = new RequestQueue();
        HashMap<Integer, RequestQueue> queueMap = new HashMap<>();
        HashMap<Integer, Boolean> isBusyMap = new HashMap<>();
        HashMap<Integer, Elevator> elevatorMap = new HashMap<>();
        HashMap<Integer, ShaftContext> shaftContextMap = new HashMap<>();

        for (int i = 1; i <= 6; i++) {
            ShaftContext shaftContext = new ShaftContext(i);
            shaftContextMap.put(i, shaftContext);
            shaftContextMap.put(i + 6, shaftContext);
        }

        for (int i = 1; i <= 12; i++) {
            queueMap.put(i, new RequestQueue());
            isBusyMap.put(i, false);
            boolean isMainEle = i <= 6;
            Elevator elevator = new Elevator(i,
                    queueMap.get(i),
                    requestQueue,
                    isBusyMap,
                    shaftContextMap.get(i),
                    isMainEle);
            elevator.start();
            elevatorMap.put(i, elevator);
        }
        InputThread inputThread = new InputThread(requestQueue);
        DispatchThread dispatchThread =
                new DispatchThread(requestQueue, queueMap, isBusyMap, elevatorMap);
        inputThread.start();
        dispatchThread.start();
    }
}
