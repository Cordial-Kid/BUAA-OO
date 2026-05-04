import com.oocourse.elevator2.ElevatorInput;   // 相当于order input
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.MaintRequest;
import com.oocourse.elevator2.Request;   // 相当于order

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
            if (request instanceof PersonRequest || request instanceof MaintRequest) {
                requestQueue.offer(request);
            }
        }
    }
}
