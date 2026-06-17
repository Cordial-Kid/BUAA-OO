import com.oocourse.spec3.main.UserInterface;
import com.oocourse.spec3.main.VideoInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class User implements UserInterface {
    private int id;
    private String name;
    private int age;
    private int coins = 0;
    private final HashSet<UserInterface> following;
    private final HashSet<UserInterface> followers;
    private final LinkedList<Integer> receivedVideos;
    private final HashSet<Integer> medals;
    private final HashSet<UserInterface> contributors;
    private final HashMap<UserInterface, Integer> contributions;
    private final HashSet<VideoInterface> watchedVideos;
    private final HashSet<VideoInterface> likedVideos;
    private final HashSet<String> types;
    private final HashMap<String, Integer> typeCounts;
    private final HashSet<VideoInterface> videos;
    private static final String[] VALID_TYPES = {
        "tech", "music", "sport", "game", "food", "travel", "comedy"
    };

    public User(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.following = new HashSet<>();
        this.followers = new HashSet<>();
        this.receivedVideos = new LinkedList<>();
        this.medals = new HashSet<>();
        this.contributors = new HashSet<>();
        this.contributions = new HashMap<>();
        this.watchedVideos = new HashSet<>();
        this.likedVideos = new HashSet<>();
        this.types = new HashSet<>();
        this.typeCounts = new HashMap<>();
        this.videos = new HashSet<>();

        for (String type : VALID_TYPES) {
            types.add(type);
            typeCounts.put(type, 0);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserInterface)) {
            return false;
        }
        return ((UserInterface) obj).getId() == id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
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
        List<Integer> result = new LinkedList<>();
        for (Integer videoId : receivedVideos) {
            if (result.size() == 5) {
                break;
            }
            result.add(videoId);
        }
        return result;
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
        for (Integer videoId : receivedVideos) {
            if (videoId == video.getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasWatchedVideo(VideoInterface video) {
        return watchedVideos.contains(video);
    }

    @Override
    public boolean hasLikedVideo(VideoInterface video) {
        return likedVideos.contains(video);
    }

    @Override
    public boolean hasMedal(int uploaderId) {
        if (medals.contains(uploaderId)) {
            return true;
        }
        return false;
    }

    @Override
    public int getInterest(String type, int totalVideos) {
        int typeCount = typeCounts.get(type);
        return typeCount * (totalVideos - watchedVideos.size() + 1);
    }

    @Override
    public int getInfluence(String type) {
        int ans = 0;
        for (VideoInterface v : videos) {
            if (v.getType().equals(type)) {
                ans += v.getHeat();
            }
        }
        return ans;
    }

    @Override
    public List<Integer> getProfile(int totalVideos) {
        List<Integer> result = new LinkedList<>();
        for (String type : VALID_TYPES) {
            result.add(getInterest(type, totalVideos));
        }
        return result;
    }

    @Override
    public long computeUpScore(UserInterface up, int totalVideos) {
        long ans = 0;
        for (String s : types) {
            ans += (long) getInterest(s, totalVideos) * up.getInfluence(s);
        }
        return ans;
    }

    public int getMostContributorId() {
        int max = -1;
        int bestId = -1;
        for (UserInterface contributor : contributions.keySet()) {
            int tmp = contributions.get(contributor);
            if (tmp > max || (tmp == max && contributor.getId() < bestId)) {
                max = tmp;
                bestId = contributor.getId();
            }
        }
        return bestId;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public int getCoins() {
        return this.coins;
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

    public LinkedList<Integer> getReceivedVideos() {
        return receivedVideos;
    }

    public HashSet<VideoInterface> getLikedVideos() {
        return this.likedVideos;
    }

    public HashSet<UserInterface> getContributors() {
        return this.contributors;
    }

    public HashMap<UserInterface, Integer> getContributions() {
        return this.contributions;
    }

    public HashSet<Integer> getMedals() {
        return this.medals;
    }

    public HashSet<VideoInterface> getWatchedVideos() {
        return this.watchedVideos;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public HashSet<VideoInterface> getVideos() {
        return this.videos;
    }

    public HashMap<String, Integer> getTypeCounts() {
        return this.typeCounts;
    }

    public boolean strictEquals(UserInterface user) {
        return true;
    }
}
