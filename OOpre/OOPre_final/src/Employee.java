import java.util.ArrayList;

public interface Employee {
    public String getName();

    public void aidEmployer(Employer employer, int[] cnt);

    public ArrayList<Adventurer> getSubordinates();
}
