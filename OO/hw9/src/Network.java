
import com.oocourse.spec1.exceptions.EqualUserIdException;
import com.oocourse.spec1.exceptions.InvalidAgeException;
import com.oocourse.spec1.exceptions.EqualVideoIdException;
import com.oocourse.spec1.exceptions.UserIdNotFoundException;
import com.oocourse.spec1.exceptions.SelfSubscriptionException;
import com.oocourse.spec1.exceptions.DuplicateSubscriptionException;
import com.oocourse.spec1.exceptions.FollowLinkNotFoundException;
import com.oocourse.spec1.exceptions.VideoIdNotFoundException;
import com.oocourse.spec1.exceptions.UncessException;
import com.oocourse.spec1.main.NetworkInterface;
import com.oocourse.spec1.main.UserInterface;
import com.oocourse.spec1.main.VideoInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;

public class Network implements NetworkInterface {
    private HashMap<Integer, UserInterface> users;
    private HashMap<Integer, VideoInterface> videos;

    public Network() {
        this.users = new HashMap<>();
        this.videos = new HashMap<>();
    }

    @Override
    public boolean containsUser(int id) {
        return users.containsKey(id);
    }

    @Override
    public UserInterface getUser(int id) {
        if (users.containsKey(id)) {
            return users.get(id);
        }
        return null;
    }

    @Override
    public boolean containsVideo(int id) {
        return videos.containsKey(id);
    }

    @Override
    public VideoInterface getVideo(int id) {
        if (videos.containsKey(id)) {
            return videos.get(id);
        }
        return null;
    }

    @Override
    public void addUser(int id, String name, int age)
            throws EqualUserIdException, InvalidAgeException {
        if (users.containsKey(id)) {
            throw new EqualUserIdException(id);
        } else if (!users.containsKey(id) && ((age < 0) || (age > 110))) {
            throw new InvalidAgeException(age);
        }
        UserInterface user = new User(id, name, age);
        users.put(id, user);

        System.out.println("add_user succeeded");
    }

    @Override
    public void uploadVideo(int uploaderId, int videoId)
            throws UserIdNotFoundException, EqualVideoIdException {
        if (!users.containsKey(uploaderId)) {
            throw new UserIdNotFoundException(uploaderId);
        }
        if (videos.containsKey(videoId)) {
            throw new EqualVideoIdException(videoId);
        }

        VideoInterface video = new Video(videoId, uploaderId);
        videos.put(videoId, video);

        UserInterface user = users.get(uploaderId);
        if (user instanceof User) {
            HashSet<UserInterface> followers = ((User) user).getFollowers();
            for (UserInterface follower : followers) {
                if (follower instanceof User) {
                    ((User) follower).getReceivedVideos().add(0, videoId);
                }
            }
        }
        System.out.println("upload_video succeeded");
    }

    @Override
    public void followUser(int id1, int id2)
            throws UserIdNotFoundException,
            SelfSubscriptionException,
            DuplicateSubscriptionException {
        if (!users.containsKey(id1)) {
            throw new UserIdNotFoundException(id1);
        } else if (!users.containsKey(id2)) {
            throw new UserIdNotFoundException(id2);
        } else if (id1 == id2) {
            throw new SelfSubscriptionException(id1);
        } else if (users.get(id1).isFollowing(users.get(id2))) {
            throw new DuplicateSubscriptionException(id1, id2);
        }
        ((User) users.get(id1)).getFollowing().add(users.get(id2));
        ((User) users.get(id2)).getFollowers().add(users.get(id1));
        System.out.println("follow_user succeeded");
    }

    @Override
    public void unfollowUser(int id1, int id2)
            throws UserIdNotFoundException, FollowLinkNotFoundException {
        if (!users.containsKey(id1)) {
            throw new UserIdNotFoundException(id1);
        } else if (!users.containsKey(id2)) {
            throw new UserIdNotFoundException(id2);
        } else if (!users.get(id1).isFollowing(users.get(id2))) {
            throw new FollowLinkNotFoundException(id1, id2);
        }

        ((User) users.get(id1)).getFollowing().remove(users.get(id2));
        ((User) users.get(id2)).getFollowers().remove(users.get(id1));
        System.out.println("unfollow_user succeeded");
    }

    @Override
    public void watchVideo(int userId, int videoId)
            throws UserIdNotFoundException, VideoIdNotFoundException {
        if (!users.containsKey(userId)) {
            throw new UserIdNotFoundException(userId);
        } else if (!videos.containsKey(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        // 如果hasReceived失效怎么办

        User user = (User) users.get(userId);
        ArrayList<Integer> receivedVideos = user.getReceivedVideos();
        receivedVideos.remove(Integer.valueOf(videoId));
        // 不管找到找不到删一下
        System.out.println("watch_video succeeded");
    }

    @Override
    public List<Integer> queryReceivedUnwatchedVideos(int userId)
            throws UserIdNotFoundException {
        if (!users.containsKey(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        User user = (User) users.get(userId);
        return user.queryReceivedUnwatchedVideos();
    }

    @Override
    public double[] queryUpFollowersAgeRatio(int upId)
            throws UserIdNotFoundException {
        if (!users.containsKey(upId)) {
            throw new UserIdNotFoundException(upId);
        }
        User user = (User) users.get(upId);
        return user.queryAgeRatio();
    }

    @Override
    public int queryMutualFollowingSum() {
        int sum = 0;
        for (UserInterface user : users.values()) {
            if (user instanceof User) {
                HashSet<UserInterface> following = ((User) user).getFollowing();
                HashSet<UserInterface> followers = ((User) user).getFollowers();
                for (UserInterface followee : following) {
                    if (followers.contains(followee)) {
                        sum++;
                    }
                }
            }
        }
        return sum / 2;
    }

    @Override
    public int queryShortestPath(int id1, int id2) throws UserIdNotFoundException, UncessException {
        if (!users.containsKey(id1)) {
            throw new UserIdNotFoundException(id1);
        } else if (!users.containsKey(id2)) {
            throw new UserIdNotFoundException(id2);
        }
        if (id1 == id2) {
            return 0;
        }

        Queue<UserInterface> queue = new LinkedList<>();
        HashMap<Integer, Integer> distance = new HashMap<>();
        UserInterface start = users.get(id1);

        queue.add(start);
        distance.put(id1, 0);

        while (!queue.isEmpty()) {
            UserInterface user = queue.poll();
            int curDistance = distance.get(user.getId());
            for (UserInterface next : ((User) user).getFollowing()) {
                if (!distance.containsKey(next.getId())) {
                    queue.add(next);
                    distance.put(next.getId(), curDistance + 1);
                    if (next.getId() == id2) {
                        return curDistance + 1;
                    }
                }
            }
        }
        throw new UncessException(id1, id2);
    }

    public UserInterface[] getUsers() {
        return null;
    }
}
