import com.oocourse.elevator1.TimableOutput;

import java.util.HashMap;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        RequestQueue requestQueue = new RequestQueue();
        HashMap<Integer, RequestQueue> queueMap = new HashMap<>();
        for (int i = 1; i <= 6; i++) {
            queueMap.put(i, new RequestQueue());
            Elevator elevator = new Elevator(i, queueMap.get(i));
            elevator.start();
        }
        InputThread inputThread = new InputThread(requestQueue);
        DispatchThread dispatchThread = new DispatchThread(requestQueue, queueMap);
        inputThread.start();
        dispatchThread.start();
    }
}
