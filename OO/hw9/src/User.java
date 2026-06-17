import com.oocourse.spec1.main.UserInterface;
import com.oocourse.spec1.main.VideoInterface;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class User implements UserInterface {
    private int id;
    private String name;
    private int age;
    private final HashSet<UserInterface> following;
    private final HashSet<UserInterface> followers;
    private final ArrayList<Integer> receivedVideos;

    public User(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.following = new HashSet<>();
        this.followers = new HashSet<>();
        this.receivedVideos = new ArrayList<>();
    }

    @Override
    public double[] queryAgeRatio() {
        double[] result = new double[4];

        if (followers.isEmpty()) {
            return result;
        }

        for (UserInterface user : followers) {
            int age = user.getAge();
            if (age <= 16) {
                result[0]++;
            } else if (age <= 30) {
                result[1]++;
            } else if (age <= 45) {
                result[2]++;
            } else {
                result[3]++;
            }
        }

        for (int i = 0; i < 4; i++) {
            result[i] /= followers.size();
        }

        return result;
    }

    @Override
    public List<Integer> queryReceivedUnwatchedVideos() {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < receivedVideos.size() && i <= 4; i++) {
            result.add(receivedVideos.get(i));
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserInterface)) {
            return false;
        }
        return ((UserInterface) obj).getId() == id;
    }

    @Override
    public boolean isFollowing(UserInterface user) {
        if (following.contains(user)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean containsFollower(UserInterface user) {
        if (followers.contains(user)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean hasReceivedVideo(VideoInterface video) {
        for (int i = 0; i < receivedVideos.size(); i++) {
            if (receivedVideos.get(i) == video.getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getAge() {
        return this.age;
    }

    public HashSet<UserInterface> getFollowers() {
        return followers;
    }

    public HashSet<UserInterface> getFollowing() {
        return following;
    }

    public ArrayList<Integer> getReceivedVideos() {
        return receivedVideos;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    public boolean strictEquals(UserInterface user) {
        return true;
    }

}
