import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.Request;

import java.util.HashMap;

import com.oocourse.elevator1.TimableOutput;

public class DispatchThread extends Thread {
    private final RequestQueue requests;
    private final HashMap<Integer, RequestQueue> queueMap;

    public DispatchThread(RequestQueue requests,
                          HashMap<Integer, RequestQueue> queueMap) {
        this.requests = requests;
        this.queueMap = queueMap;
    }

    @Override
    public void run() {
        while (true) {
            if (requests.isEmpty() && requests.isEnd()) {
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
        int elevatorId = -1;
        if (request instanceof PersonRequest) {
            elevatorId = ((PersonRequest) request).getElevatorId();
        }
        try {
            TimableOutput.println("RECEIVE-"
                    + ((PersonRequest) request).getPersonId()
                    + "-"
                    + elevatorId);
            queueMap.get(elevatorId).offer(request);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException();
        }
    }
}
