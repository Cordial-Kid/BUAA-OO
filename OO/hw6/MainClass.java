import com.oocourse.elevator2.TimableOutput;

import java.util.HashMap;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        RequestQueue requestQueue = new RequestQueue();
        HashMap<Integer, RequestQueue> queueMap = new HashMap<>();
        HashMap<Integer, Boolean> isMaintainMap = new HashMap<>();
        HashMap<Integer, Elevator> elevatorMap = new HashMap<>();
        for (int i = 1; i <= 6; i++) {
            queueMap.put(i, new RequestQueue());
            isMaintainMap.put(i, false);
            // TODO init elevator
            Elevator elevator = new Elevator(i, queueMap.get(i), requestQueue, isMaintainMap);
            elevator.start();
            elevatorMap.put(i, elevator);
        }
        InputThread inputThread = new InputThread(requestQueue);
        DispatchThread dispatchThread =
                new DispatchThread(requestQueue, queueMap, isMaintainMap, elevatorMap);
        inputThread.start();
        dispatchThread.start();
    }
}
