package input;

import com.oocourse.elevator3.ElevatorInput;   // 相当于order input
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.MaintRequest;
import com.oocourse.elevator3.RecycleRequest;
import com.oocourse.elevator3.UpdateRequest;
import com.oocourse.elevator3.Request;   // 相当于order

import queue.RequestQueue;

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
            if (request instanceof PersonRequest
                    || request instanceof MaintRequest
                    || request instanceof RecycleRequest
                    || request instanceof UpdateRequest) {
                requestQueue.offer(request);
            }
        }
    }
}
