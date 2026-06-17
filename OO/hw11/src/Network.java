import com.oocourse.spec3.exceptions.ColdStartUserException;
import com.oocourse.spec3.exceptions.ColdStartVideoException;
import com.oocourse.spec3.exceptions.EqualCommentIdException;
import com.oocourse.spec3.exceptions.EqualUserIdException;
import com.oocourse.spec3.exceptions.EqualVideoIdException;
import com.oocourse.spec3.exceptions.FollowLinkNotFoundException;
import com.oocourse.spec3.exceptions.InsufficientCoinsException;
import com.oocourse.spec3.exceptions.InvalidAgeException;
import com.oocourse.spec3.exceptions.InvalidCoinsException;
import com.oocourse.spec3.exceptions.InvalidRankException;
import com.oocourse.spec3.exceptions.InvalidTypeException;
import com.oocourse.spec3.exceptions.NoContributorsException;
import com.oocourse.spec3.exceptions.NoUserException;
import com.oocourse.spec3.exceptions.NoVideoUploadedException;
import com.oocourse.spec3.exceptions.SelfSubscriptionException;
import com.oocourse.spec3.exceptions.UncessException;
import com.oocourse.spec3.exceptions.UserIdNotFoundException;
import com.oocourse.spec3.exceptions.VideoIdNotFoundException;
import com.oocourse.spec3.exceptions.VideoUnwatchedException;
import com.oocourse.spec3.exceptions.DuplicateSubscriptionException;
import com.oocourse.spec3.exceptions.DuplicateMedalException;
import com.oocourse.spec3.exceptions.InvalidCommentException;
import com.oocourse.spec3.main.NetworkInterface;
import com.oocourse.spec3.main.UserInterface;
import com.oocourse.spec3.main.VideoInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
        if (users.containsKey(id)) { return users.get(id); }
        return null;
    }

    @Override
    public boolean containsVideo(int id) {
        return videos.containsKey(id);
    }

    @Override
    public VideoInterface getVideo(int id) {
        if (videos.containsKey(id)) { return videos.get(id); }
        return null;
    }

    @Override
    public void addUser(int id, String name, int age)
            throws EqualUserIdException, InvalidAgeException {
        if (users.containsKey(id)) { throw new EqualUserIdException(id); }
        if (!users.containsKey(id) && ((age < 0) || (age > 110))) {
            throw new InvalidAgeException(age);
        }
        UserInterface user = new User(id, name, age);
        users.put(id, user);
        System.out.println("add_user succeeded");
    }

    @Override
    public void uploadVideo(int uploaderId, int videoId, String type)
            throws UserIdNotFoundException, EqualVideoIdException, InvalidTypeException {
        if (!users.containsKey(uploaderId)) { throw new UserIdNotFoundException(uploaderId); }
        if (videos.containsKey(videoId)) { throw new EqualVideoIdException(videoId); }
        if (!isValidType(type)) { throw new InvalidTypeException(type); }
        VideoInterface video = new Video(videoId, uploaderId, type);
        videos.put(videoId, video);
        UserInterface user = users.get(uploaderId);
        HashSet<VideoInterface> userVideos = ((User) user).getVideos();
        userVideos.add(video);
        HashSet<UserInterface> followers = ((User) user).getFollowers();
        for (UserInterface follower : followers) {
            if (follower instanceof User) {
                ((User) follower).getReceivedVideos().addFirst(videoId);
            }
        }
        System.out.println("upload_video succeeded");
    }

    @Override
    public boolean isValidType(String type) {
        if (type == null) { return false; }
        return (type.equals("tech") || type.equals("music") || type.equals("sport")
                || type.equals("game") || type.equals("food") || type.equals("travel")
                || type.equals("comedy"));
    }

    @Override
    public void followUser(int id1, int id2)
            throws UserIdNotFoundException, SelfSubscriptionException,
            DuplicateSubscriptionException {
        if (!users.containsKey(id1)) { throw new UserIdNotFoundException(id1); }
        if (!users.containsKey(id2)) { throw new UserIdNotFoundException(id2); }
        if (id1 == id2) { throw new SelfSubscriptionException(id1); }
        if (users.get(id1).isFollowing(users.get(id2))) {
            throw new DuplicateSubscriptionException(id1, id2);
        }
        ((User) users.get(id1)).getFollowing().add(users.get(id2));
        ((User) users.get(id2)).getFollowers().add(users.get(id1));
        System.out.println("follow_user succeeded");
    }

    @Override
    public void unfollowUser(int id1, int id2)
            throws UserIdNotFoundException, FollowLinkNotFoundException {
        if (!users.containsKey(id1)) { throw new UserIdNotFoundException(id1); }
        if (!users.containsKey(id2)) { throw new UserIdNotFoundException(id2); }
        if (!users.get(id1).isFollowing(users.get(id2))) {
            throw new FollowLinkNotFoundException(id1, id2);
        }
        ((User) users.get(id1)).getFollowing().remove(users.get(id2));
        ((User) users.get(id2)).getFollowers().remove(users.get(id1));
        System.out.println("unfollow_user succeeded");
    }

    @Override
    public void watchVideo(int userId, int videoId)
            throws UserIdNotFoundException, VideoIdNotFoundException {
        if (!users.containsKey(userId)) { throw new UserIdNotFoundException(userId); }
        if (!videos.containsKey(videoId)) { throw new VideoIdNotFoundException(videoId); }
        User user = (User) users.get(userId);
        LinkedList<Integer> receivedVideos = user.getReceivedVideos();
        HashSet<VideoInterface> watchedVideos = user.getWatchedVideos();
        VideoInterface video = videos.get(videoId);
        watchedVideos.add(video);
        receivedVideos.removeIf(id -> id == videoId);
        int before = video.getPlayCount();
        ((Video) video).setPlayCount(before + 1);
        HashMap<String, Integer> typeCounts = user.getTypeCounts();
        typeCounts.put(video.getType(), typeCounts.get(video.getType()) + 1);
        System.out.println("watch_video succeeded");
    }

    @Override
    public List<Integer> queryReceivedUnwatchedVideos(int userId)
            throws UserIdNotFoundException {
        if (!users.containsKey(userId)) { throw new UserIdNotFoundException(userId); }
        User user = (User) users.get(userId);
        return user.queryReceivedUnwatchedVideos();
    }

    @Override
    public double[] queryUpFollowersAgeRatio(int upId)
            throws UserIdNotFoundException {
        if (!users.containsKey(upId)) { throw new UserIdNotFoundException(upId); }
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
        if (!users.containsKey(id1)) { throw new UserIdNotFoundException(id1); }
        if (!users.containsKey(id2)) { throw new UserIdNotFoundException(id2); }
        if (id1 == id2) { return 0; }
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
                    if (next.getId() == id2) { return curDistance + 1; }
                }
            }
        }
        throw new UncessException(id1, id2);
    }

    @Override
    public void addUserCoins(int userId, int coins)
            throws UserIdNotFoundException {
        if (!containsUser(userId)) { throw new UserIdNotFoundException(userId); }
        UserInterface user = users.get(userId);
        int before = user.getCoins();
        int after = before + coins;
        ((User) user).setCoins(after);
        System.out.println("add_user_coins succeeded");
    }

    @Override
    public void likeVideo(int userId, int videoId)
            throws UserIdNotFoundException, VideoIdNotFoundException,
            VideoUnwatchedException, EqualUserIdException {
        if (!containsUser(userId)) { throw new UserIdNotFoundException(userId); }
        if (!containsVideo(videoId)) { throw new VideoIdNotFoundException(videoId); }
        VideoInterface video = videos.get(videoId);
        if (video.getUploaderId() == userId) { throw new EqualUserIdException(userId); }
        if (!users.get(userId).hasWatchedVideo(video)) {
            throw new VideoUnwatchedException(userId, videoId);
        }
        UserInterface user = users.get(userId);
        HashSet<VideoInterface> likedVideos = ((User) user).getLikedVideos();
        if (!likedVideos.contains(video)) {
            likedVideos.add(video);
            int before = video.getLikes();
            int after = before + 1;
            ((Video) video).setLikes(after);
            System.out.println("like_video succeeded");
        } else {
            likedVideos.remove(video);
            int before = video.getLikes();
            int after = before - 1;
            ((Video) video).setLikes(after);
            System.out.println("unlike_video succeeded");
        }
    }

    @Override
    public void coinVideo(int userId, int videoId, int amount)
            throws UserIdNotFoundException, VideoIdNotFoundException, InsufficientCoinsException,
            VideoUnwatchedException, InvalidCoinsException, EqualUserIdException {
        if (!containsUser(userId)) { throw new UserIdNotFoundException(userId); }
        if (!containsVideo(videoId)) { throw new VideoIdNotFoundException(videoId); }
        UserInterface user = users.get(userId);
        VideoInterface video = videos.get(videoId);
        if (user.getId() == video.getUploaderId()) { throw new EqualUserIdException(userId); }
        if (!user.hasWatchedVideo(video)) { throw new VideoUnwatchedException(userId, videoId); }
        if (amount != 1 && amount != 2) { throw new InvalidCoinsException(amount); }
        if (user.getCoins() < amount) { throw new InsufficientCoinsException(userId); }
        UserInterface up = getUser(video.getUploaderId());
        final HashSet<UserInterface> contributors = ((User) up).getContributors();
        final HashMap<UserInterface, Integer> contributions = ((User) up).getContributions();
        ((Video) video).setCoins(video.getCoins() + amount);
        ((User) user).setCoins(user.getCoins() - amount);
        ((User) up).setCoins(up.getCoins() + amount);
        if (!contributors.contains(user)) {
            contributors.add(user);
            contributions.put(user, amount);
        } else {
            Integer before = contributions.get(user);
            contributions.replace(user, before + amount);
        }
        System.out.println("coin_video succeeded");
    }

    @Override
    public int queryBestContributor(int id)
            throws UserIdNotFoundException, NoContributorsException {
        if (!containsUser(id)) { throw new UserIdNotFoundException(id); }
        UserInterface user = users.get(id);
        HashMap<UserInterface, Integer> contributions = ((User) user).getContributions();
        if (contributions.isEmpty()) { throw new NoContributorsException(id); }
        int bestId = -1;
        int max = -1;
        for (UserInterface contributor : contributions.keySet()) {
            int tmp = contributions.get(contributor);
            if (tmp > max || (tmp == max && contributor.getId() < bestId)) {
                max = tmp;
                bestId = contributor.getId();
            }
        }
        return bestId;
    }

    private void throwVideoUnwatchedException(int userId, int videoId)
            throws VideoUnwatchedException {
        UserInterface user = users.get(userId);
        if (!user.hasWatchedVideo(videos.get(videoId))) {
            throw new VideoUnwatchedException(userId, videoId);
        }
    }

    private void throwFollowLinkNotFoundException(int userId, int followerId)
            throws FollowLinkNotFoundException {
        UserInterface user1 = users.get(userId);
        UserInterface user2 = users.get(followerId);
        if (!user1.containsFollower(user2)) {
            throw new FollowLinkNotFoundException(userId, followerId);
        }
    }

    @Override
    public void forwardVideo(int userId, int videoId, int followerId)
            throws UserIdNotFoundException, VideoIdNotFoundException,
            FollowLinkNotFoundException, VideoUnwatchedException {
        if (!containsUser(userId)) { throw new UserIdNotFoundException(userId); }
        if (!containsUser(followerId)) { throw new UserIdNotFoundException(followerId); }
        if (!containsVideo(videoId)) { throw new VideoIdNotFoundException(videoId); }
        final UserInterface follower = users.get(followerId);
        VideoInterface video = videos.get(videoId);
        throwVideoUnwatchedException(userId, videoId);
        throwFollowLinkNotFoundException(userId, followerId);
        int before = video.getForwardCount();
        ((Video) video).setForwardCount(before + 1);
        ((User) follower).getReceivedVideos().addFirst(videoId);
        System.out.println("forward_video succeeded");
    }

    @Override
    public void sendComment(int userId, int videoId, int commentId, String comment)
            throws UserIdNotFoundException, VideoIdNotFoundException,
            EqualCommentIdException, InvalidCommentException {
        if (!containsUser(userId)) { throw new UserIdNotFoundException(userId); }
        if (!containsVideo(videoId)) { throw new VideoIdNotFoundException(videoId); }
        VideoInterface video = videos.get(videoId);
        if (video.containsComment(commentId)) { throw new EqualCommentIdException(commentId); }
        if (comment == null || comment.isEmpty()) { throw new InvalidCommentException(); }
        HashSet<Integer> commentIds = ((Video) video).getCommentIds(1);
        HashMap<Integer, String> commentContents = ((Video) video).getCommentContents(1);
        commentIds.add(commentId);
        commentContents.put(commentId, comment);
        System.out.println("send_comment succeeded");
    }

    @Override
    public int[] cleanSpamComments(int videoId, String keyword)
            throws VideoIdNotFoundException {
        if (!containsVideo(videoId)) { throw new VideoIdNotFoundException(videoId); }
        VideoInterface video = videos.get(videoId);
        HashSet<Integer> commentIds = ((Video) video).getCommentIds(1);
        HashMap<Integer, String> commentContents = ((Video) video).getCommentContents(1);
        Iterator<Integer> iterator = commentIds.iterator();
        int count = 0;
        int keyCount = 0;
        while (iterator.hasNext()) {
            int commentId = iterator.next();
            String comments = commentContents.get(commentId);
            if (comments.contains(keyword)) {
                iterator.remove();
                commentContents.remove(commentId);
                count++;
                int tmp = getNum(comments, keyword);
                if (tmp > keyCount) { keyCount = tmp; }
            }
        }
        int[] result = new int[2];
        if (count == 0) {
            result[0] = 0;
            result[1] = 0;
        }
        result[0] = count;
        result[1] = keyCount;
        return result;
    }

    private int getNum(String comment, String keyword) {
        if (comment == null || comment.isEmpty()) { return 0; }
        if (keyword == null) { return 0; }
        if (keyword.isEmpty()) { return comment.length() + 1; }
        int count = 0;
        int index = 0;
        while ((index = comment.indexOf(keyword, index)) != -1) {
            count++;
            index++;
        }
        return count;
    }

    @Override
    public VideoInterface queryMostPopularVideo(String type)
            throws InvalidTypeException {
        if (!isValidType(type)) { throw new InvalidTypeException(type); }
        int bestId = -1;
        double max = -1;
        for (Integer videoId : videos.keySet()) {
            VideoInterface video = videos.get(videoId);
            if (video.getType().equals(type)) {
                if (video.getHeat() > max || (video.getHeat() == max && videoId < bestId)) {
                    max = video.getHeat();
                    bestId = videoId;
                }
            }
        }
        if (bestId == -1) { return null; }
        return videos.get(bestId);
    }

    @Override
    public void purchaseMedal(int userId, int videoId, int amount)
            throws UserIdNotFoundException, VideoIdNotFoundException,
            EqualUserIdException, InsufficientCoinsException, DuplicateMedalException {
        if (!containsUser(userId)) { throw new UserIdNotFoundException(userId); }
        if (!containsVideo(videoId)) { throw new VideoIdNotFoundException(videoId); }
        VideoInterface video = videos.get(videoId);
        UserInterface user = users.get(userId);
        if (userId == video.getUploaderId()) {
            throw new EqualUserIdException(userId);
        }
        if (user.getCoins() < amount) {
            throw new InsufficientCoinsException(userId);
        }
        if (user.hasMedal(video.getUploaderId())) {
            throw new DuplicateMedalException(userId, video.getUploaderId());
        }
        ((User) user).setCoins(user.getCoins() - amount);
        ((User) user).getMedals().add(video.getUploaderId());
        UserInterface up = users.get(video.getUploaderId());
        ((User) up).setCoins(up.getCoins() + amount);
        System.out.println("purchase_medal succeeded");
    }

    @Override
    public int queryLongestDecSeq() {
        if (users.isEmpty()) { return 0; }
        int longestPath = 1;
        HashMap<UserInterface, Integer> distance = new HashMap<>();
        for (UserInterface user : users.values()) {
            int tmp = dfs(user, distance);
            longestPath = Math.max(longestPath, tmp);
        }
        return longestPath;
    }

    private int dfs(UserInterface user, HashMap<UserInterface, Integer> distance) {
        int longestPath = 1;
        if (distance.containsKey(user)) { return distance.get(user); }
        for (UserInterface user2 : ((User) user).getFollowing()) {
            if (user.getAge() > user2.getAge()) {
                longestPath = Math.max(longestPath, dfs(user2, distance) + 1);
            }
        }
        distance.put(user, longestPath);
        return longestPath;
    }

    // 这个异常我该怎么抛
    @Override
    public int[] queryGlobalBestContributor()
            throws NoUserException {
        if (users.isEmpty()) { throw new NoUserException(); }
        int bestId = 0;
        int bestCount = 0;
        HashMap<Integer, Integer> ans = new HashMap<>();
        for (UserInterface user : users.values()) {
            if (((User) user).getContributors().isEmpty()) { continue; }
            int contributorId = ((User) user).getMostContributorId();
            int cnt = ans.getOrDefault(contributorId, 0) + 1;
            ans.put(contributorId, cnt);
            if (cnt > bestCount || (cnt == bestCount && contributorId < bestId)) {
                bestCount = cnt;
                bestId = contributorId;
            }
        }
        return new int[]{bestId, bestCount};
    }

    @Override
    public int recommendVideo(int userId)
            throws UserIdNotFoundException, NoVideoUploadedException, ColdStartVideoException {
        if (!containsUser(userId)) { throw new UserIdNotFoundException(userId); }
        if (videos.isEmpty()) { throw new NoVideoUploadedException(); }
        UserInterface user = users.get(userId);
        throwColdStartVideoException(userId);
        int bestId = -1;
        long max = -1;
        for (Integer videoId : videos.keySet()) {
            long tmp = computeVideoScore(user, videos.get(videoId));
            if (tmp > max || (tmp == max && videoId < bestId)) {
                max = tmp;
                bestId = videoId;
            }
        }
        return bestId;
    }

    @Override
    public long computeVideoScore(UserInterface user, VideoInterface video) {
        return (long) video.getHeat() * user.getInterest(video.getType(), videos.size());
    }

    @Override
    public int recommendNthUp(int userId, int rank)
            throws UserIdNotFoundException, InvalidRankException,
            NoVideoUploadedException, ColdStartUserException {
        if (!containsUser(userId)) { throw new UserIdNotFoundException(userId); }
        if (rank <= 0) { throw new InvalidRankException(rank); }
        if (videos.isEmpty()) { throw new NoVideoUploadedException(); }
        UserInterface user = users.get(userId);
        int count = 0;
        for (UserInterface u : users.values()) {
            if ((u.getId() != userId) && !(user.isFollowing(u))) { count++; }
        }
        if (count < rank) { throw new ColdStartUserException(userId); }

        List<UserInterface> candidates = new ArrayList<>();
        for (UserInterface u : users.values()) {
            if ((u.getId() != userId) && !(user.isFollowing(u))) { candidates.add(u); }
        }
        candidates.sort((u1, u2) -> {
            long score1 = user.computeUpScore(u1, videos.size());
            long score2 = user.computeUpScore(u2, videos.size());
            if (score1 != score2) {
                return Long.compare(score2, score1);
            } else { return Integer.compare(u1.getId(), u2.getId()); }
        });
        return candidates.get(rank - 1).getId();
    }

    @Override
    public int queryMostInfluentialUp(String type)
            throws InvalidTypeException, NoUserException {
        if (!isValidType(type)) { throw new InvalidTypeException(type); }
        if (users.isEmpty()) { throw new NoUserException(); }
        int max = -1;
        int bestId = -1;
        for (Integer userId : users.keySet()) {
            UserInterface user = users.get(userId);
            if (user.getInfluence(type) > max
                    || (user.getInfluence(type) == max && userId < bestId)) {
                max = user.getInfluence(type);
                bestId = userId;
            }
        }
        return bestId;
    }

    @Override
    public List<Integer> queryUserProfile(int userId)
            throws UserIdNotFoundException, ColdStartVideoException {
        if (!containsUser(userId)) { throw new UserIdNotFoundException(userId); }
        throwColdStartVideoException(userId);
        UserInterface user = users.get(userId);
        return user.getProfile(videos.size());
    }

    private void throwColdStartVideoException(int userId) throws ColdStartVideoException {
        UserInterface user = users.get(userId);
        if (((User) user).getWatchedVideos().isEmpty()) {
            throw new ColdStartVideoException(userId);
        }
    }

    public UserInterface[] getUsers() {
        return null;
    }

}