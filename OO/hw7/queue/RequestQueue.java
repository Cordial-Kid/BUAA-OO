package queue;

import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.MaintRequest;
import com.oocourse.elevator3.RecycleRequest;
import com.oocourse.elevator3.UpdateRequest;

import domain.Floor;
import domain.Person;

import java.util.ArrayList;
import java.util.Iterator;

public class RequestQueue {
    private final ArrayList<Request> requests = new ArrayList<>();
    private boolean isEnd = false;

    public synchronized void offer(Request request) {
        requests.add(request);
        notifyAll();
    }

    public synchronized Request poll() {
        // while 防止虚假唤醒,防止被其他线程截胡，wait醒后直接从wait的下一条指令开始执行
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

    // 优先处理维修请求
    public synchronized MaintRequest extractMaintainRequest() {
        Iterator<Request> iterator = requests.iterator();
        while (iterator.hasNext()) {
            Request request = iterator.next();
            if (request instanceof MaintRequest) {
                iterator.remove();
                return (MaintRequest) request;
            }
        }
        return null;
    }

    public synchronized RecycleRequest extractRecycleRequest() {
        Iterator<Request> iterator = requests.iterator();
        while (iterator.hasNext()) {
            Request request = iterator.next();
            if (request instanceof RecycleRequest) {
                iterator.remove();
                return (RecycleRequest) request;
            }
        }
        return null;
    }

    public synchronized UpdateRequest extractUpdateRequest() {
        Iterator<Request> iterator = requests.iterator();
        while (iterator.hasNext()) {
            Request request = iterator.next();
            if (request instanceof UpdateRequest) {
                iterator.remove();
                return (UpdateRequest) request;
            }
        }
        return null;
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

    public synchronized int size() {
        return requests.size();
    }

    //深浅
    public synchronized ArrayList<Request> getRequests() {
        return requests;
    }

    public synchronized ArrayList<Person> getWaiting(String curFloor, int direction) {
        ArrayList<Person> waitingPersons = new ArrayList<>();
        for (Request request : requests) {
            if (!(request instanceof PersonRequest)) {
                continue;
            }
            PersonRequest personRequest = (PersonRequest) request;
            if (personRequest.getFromFloor().equals(curFloor)
                    && RequestQueue.getDirection(personRequest) == direction) {
                waitingPersons.add(RequestQueue.toPerson(personRequest));
            }
        }
        return waitingPersons;
    }

    public synchronized void remove(int id) {
        Iterator<Request> iterator = requests.iterator();
        while (iterator.hasNext()) {
            Request request = iterator.next();
            if (request instanceof PersonRequest
                    && ((PersonRequest) request).getPersonId() == id) {
                iterator.remove();
                return;
            }
        }
    }

    public synchronized ArrayList<PersonRequest> clearWaitingQueue() {
        Iterator<Request> iterator = requests.iterator();
        ArrayList<PersonRequest> waitingPersons = new ArrayList<>();
        while (iterator.hasNext()) {
            Request req = iterator.next();
            if (req instanceof PersonRequest) {
                waitingPersons.add((PersonRequest) req);
                iterator.remove();
            }
        }
        return waitingPersons;
    }

    private static Person toPerson(PersonRequest personRequest) {
        return new Person(personRequest.getPersonId(),
                personRequest.getFromFloor(),
                personRequest.getToFloor(),
                personRequest.getWeight());
    }

    private static int getDirection(PersonRequest request) {
        String from = request.getFromFloor();
        String to = request.getToFloor();
        return Floor.toInt(to) > Floor.toInt(from) ? 1 : -1;
    }
}
