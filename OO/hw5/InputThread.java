import com.oocourse.elevator1.ElevatorInput;   // 相当于order input
import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.Request;   // 相当于order

public class InputThread extends Thread {
    private final RequestQueue requestQueue;

    public InputThread(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                requestQueue.setEnd();
                break;
            }
            if (request instanceof PersonRequest) {
                requestQueue.offer(request);
            }
        }
    }
}
