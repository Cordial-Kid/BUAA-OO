import com.oocourse.spec3.main.VideoInterface;

import java.util.HashMap;
import java.util.HashSet;

public class Video implements VideoInterface {
    private int id;
    private int uploaderId;
    private String type;
    private int playCount;
    private int likes;
    private int forwardCount;
    private int coins;
    private HashSet<Integer> commentIds;
    private HashMap<Integer, String> commentContents;

    public Video(int id, int uploadId, String type) {
        this.id = id;
        this.uploaderId = uploadId;
        this.type = type;
        this.commentIds = new HashSet<>();
        this.commentContents = new HashMap<>();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VideoInterface)) {
            return false;
        }
        return ((VideoInterface) obj).getId() == id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public boolean containsComment(int id) {
        if (commentIds.contains(id)) {
            return true;
        }
        return false;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getUploaderId() {
        return uploaderId;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public int getPlayCount() {
        return this.playCount;
    }

    @Override
    public int getLikes() {
        return this.likes;
    }

    @Override
    public int getForwardCount() {
        return this.forwardCount;
    }

    @Override
    public int getCoins() {
        return this.coins;
    }

    @Override
    public int getHeat() {
        return playCount * 2 + likes * 3 + forwardCount * 4 + coins * 5;
    }

    public HashSet<Integer> getCommentIds(int n) {
        return commentIds;
    }

    public HashMap<Integer, String> getCommentContents(int n) {
        return commentContents;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void setForwardCount(int forwardCount) {
        this.forwardCount = forwardCount;
    }

    public void setPlayCount(int n) {
        this.playCount = n;
    }
}
