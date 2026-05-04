import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.Request;

import java.util.ArrayList;
import java.util.Iterator;

public class RequestQueue {
    private final ArrayList<PersonRequest> requests = new ArrayList<>();
    private boolean isEnd = false;

    public synchronized void offer(Request request) {
        if (request instanceof PersonRequest) {
            requests.add((PersonRequest) request);
            notifyAll();
        }
    }

    public synchronized PersonRequest poll() {
        // while 防止虚假唤醒
        while (requests.isEmpty() && !isEnd) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (requests.isEmpty()) {
            return null;
        }
        return requests.remove(0);
    }

    public synchronized void setEnd() {
        isEnd = true;
        notifyAll();
    }

    public synchronized boolean isEmpty() {
        return requests.isEmpty();
    }

    public synchronized boolean isEnd() {
        return isEnd;
    }

    //深浅
    public synchronized ArrayList<PersonRequest> getRequests() {
        return requests;
    }

    /*
    public synchronized Person getRequestAndRemove(String curFloor, int direction, int peoWeight) {
        Iterator<PersonRequest> iterator = requests.iterator();
        while (iterator.hasNext()) {
            PersonRequest request = iterator.next();
            if (request.getFromFloor().equals(curFloor)
                    && RequestQueue.getDirection(request) == direction
                    && peoWeight + request.getWeight() <= 400) {
                PersonRequest tmp = request;
                iterator.remove();
                return RequestQueue.toPerson(tmp);
            }
        }
        return null;
    }
    */

    public synchronized ArrayList<Person> getWaiting(String curFloor, int direction) {
        ArrayList<Person> waitingPersons = new ArrayList<>();
        for (PersonRequest request : requests) {
            if (request.getFromFloor().equals(curFloor)
                    && RequestQueue.getDirection(request) == direction) {
                waitingPersons.add(RequestQueue.toPerson(request));
            }
        }
        return waitingPersons;
    }

    public synchronized void remove(Person person) {
        Iterator<PersonRequest> iterator = requests.iterator();
        while (iterator.hasNext()) {
            PersonRequest request = iterator.next();
            if (request.getPersonId() == person.getId()) {
                iterator.remove();
                return;
            }
        }
    }

    private static Person toPerson(PersonRequest personRequest) {
        return new Person(personRequest.getPersonId(),
                personRequest.getFromFloor(),
                personRequest.getToFloor(),
                personRequest.getWeight(),
                personRequest.getElevatorId());
    }

    /*
    private static PersonRequest toPersonRequest(Person person) {
        return new PersonRequest(
                Floor.toString(person.getFromFloor()),
                Floor.toString(person.getToFloor()),
                person.getId(),
                person.getWeight(),
                person.getElevatorId());
    }
     */

    private static int getDirection(PersonRequest request) {
        String from = request.getFromFloor();
        String to = request.getToFloor();
        return Floor.toInt(to) > Floor.toInt(from) ? 1 : -1;
    }
}
