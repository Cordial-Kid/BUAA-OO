import java.util.ArrayList;

public interface Employer {
    public String getName();

    public void attach(Employee employee);

    public void detach(Employee employee);

    public void notifyEmployees(int[] cnt);

    public int getHp();

    public ArrayList<Adventurer> getSubordinates();
}
