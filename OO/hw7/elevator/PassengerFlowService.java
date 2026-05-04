package elevator;

import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.TimableOutput;
import domain.Floor;
import domain.Person;
import queue.RequestQueue;

import java.util.ArrayList;
import java.util.Iterator;

public class PassengerFlowService {

    public static boolean canPassengerOut(int peoWeight,
                                          ArrayList<Person> persons,
                                          String curFloor) {
        if (peoWeight == 0) {
            return false;
        }
        for (Person person : persons) {
            if (person.getToFloor() == Floor.toInt(curFloor)) {
                return true;
            }
        }
        return false;
    }

    public static void ejectAllPassengers(ArrayList<Person> persons, String curFloor, int id) {
        Iterator<Person> iterator = persons.iterator();
        while (iterator.hasNext()) {
            Person p = iterator.next();
            outputPassengerOut(p, curFloor, id);
        }
    }

    public static int passengerIn(RequestQueue requestqueue,
                                   String curFloor,
                                   int direction,
                                   int peoWeight,
                                   int id,
                                   ArrayList<Person> persons) {
        int tmp = peoWeight;
        ArrayList<Person> candidates = requestqueue.getWaiting(curFloor, direction);
        if (candidates.isEmpty()) {
            return peoWeight;
        }
        tmp = optimizeIn(candidates, peoWeight, requestqueue, curFloor, id, persons);
        return tmp;
    }

    // 这是正儿八经的中途放下到站的客人
    public static int passengerOut(ArrayList<Person> persons,
                                    String curFloor,
                                    int peoWeight,
                                    int id,
                                    RequestQueue globalQueue) {
        int tmp = peoWeight;
        Iterator<Person> iterator = persons.iterator();
        while (iterator.hasNext()) {
            Person person = iterator.next();
            if (person.getToFloor() == Floor.toInt(curFloor)) {
                outputPassengerOut(person, curFloor, id);
                tmp -= person.getWeight();
                removeAndOffer(person, curFloor, globalQueue);
                iterator.remove();
            }
        }
        return tmp;
    }

    public static void removeAndOffer(Person person, String floor, RequestQueue globalQueue) {
        if (!person.getFinalToFloor().equals(floor)) {
            globalQueue.offer(new PersonRequest(floor,
                    person.getFinalToFloor(),
                    person.getId(),
                    person.getWeight()));
        }
    }

    // 这是留给ejectall用的
    private static void outputPassengerOut(Person person, String curFloor, int id) {
        String result = person.getFinalToFloor().equals(curFloor) ? "OUT-S-" : "OUT-F-";
        TimableOutput.println(result + person.getId() + "-" + curFloor + "-" + id);
    }

    private static int optimizeIn(ArrayList<Person> candidates,
                                   int peoWeight,
                                   RequestQueue requestqueue,
                                   String curFloor,
                                   int id,
                                   ArrayList<Person> persons) {
        int tmp = peoWeight;
        candidates.sort((p1, p2) -> {
            int tmp1 = p1.getWeight();
            int tmp2 = p2.getWeight();
            return Integer.compare(tmp1, tmp2);
        });
        for (Person person : candidates) {
            if (person.getWeight() + tmp <= 400) {
                requestqueue.remove(person.getId());
                TimableOutput.println("IN-"
                        + person.getId()
                        + "-" + curFloor
                        + "-" + id);
                tmp += person.getWeight();
                persons.add(person);
            }
        }
        return tmp;
    }
}
