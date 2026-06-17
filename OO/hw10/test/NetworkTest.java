import com.oocourse.spec2.exceptions.VideoIdNotFoundException;
import com.oocourse.spec2.main.VideoInterface;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

public class NetworkTest {

    private Network network;

    @Before
    public void setUp() {
        network = new Network();
    }

    @Test
    public void testException() throws Exception {
        network.addUser(1, "Alice", 25);
        network.addUser(2, "Bob", 30);
        try {
            network.cleanSpamComments(101, "a");
            fail("Expected exception not thrown");
        } catch (Exception e) {
            assertTrue(e instanceof VideoIdNotFoundException);
        }
    }

    @Test
    public void testNoComment() throws Exception {
        network.addUser(1, "Alice", 25);
        network.addUser(2, "Bob", 30);
        network.uploadVideo(2, 101, "tech");
        assertArrayEquals(new int[]{0, 0}, network.cleanSpamComments(101, "a"));
    }

    @Test
    public void testNohit() throws Exception {
        network.addUser(1, "Alice", 25);
        network.addUser(2, "Bob", 30);
        network.uploadVideo(2, 101, "tech");
        network.sendComment(1, 101, 111, "a");
        network.sendComment(1, 101, 112, "b");
        assertArrayEquals(new int[]{0, 0}, network.cleanSpamComments(101, "c"));
    }

    @Test
    public void testManyVideos() throws Exception {
        network.addUser(1,  "Alice", 25);
        network.addUser(2, "Bob", 30);
        network.uploadVideo(2, 101, "tech");
        network.uploadVideo(2, 102, "tech");
        network.sendComment(1, 101, 111, "a");
        network.sendComment(1, 101, 112, "b");
        network.sendComment(1, 102, 113, "a");
        HashMap<Integer, String> comments102Before = getCommentMap(network, 102);

        assertArrayEquals(new int[]{1, 1}, network.cleanSpamComments(101, "a"));
        assertEquals(comments102Before, getCommentMap(network, 102));
        assertEquals("b", getCommentMap(network, 101).get(112));
    }

    @Test
    public void testOneComment() throws Exception {
        network.addUser(1, "Alice", 25);
        network.addUser(2, "Bob", 30);
        network.uploadVideo(2, 101, "tech");
        network.sendComment(1, 101, 111, "a");
        network.sendComment(1, 101, 112, "b");
        assertArrayEquals(new int[]{1, 1}, network.cleanSpamComments(101, "a"));
        assertArrayEquals(new int[]{0, 0}, network.cleanSpamComments(101, "a"));
    }

    @Test
    public void testManyComment() throws Exception {
        network.addUser(1, "Alice", 25);
        network.addUser(2, "Bob", 30);
        network.uploadVideo(2, 101, "tech");
        network.sendComment(1, 101, 111, "xaaaxaa");
        network.sendComment(1, 101, 112, "xaaa");
        assertArrayEquals(new int[]{2, 2}, network.cleanSpamComments(101, "xaa"));
    }

    @Test
    public void testOverlapMaxCount() throws Exception {
        network.addUser(1, "Alice", 25);
        network.addUser(2, "Bob", 30);
        network.uploadVideo(2, 101, "tech");
        network.sendComment(1, 101, 111, "xaaaaxaa");
        network.sendComment(1, 101, 112, "banana");
        network.sendComment(1, 101, 113, "xaaxaa");

        assertArrayEquals(new int[]{2, 4}, network.cleanSpamComments(101, "aa"));
        HashMap<Integer, String> comments = getCommentMap(network, 101);
        assertEquals(1, comments.size());
        assertEquals("banana", comments.get(112));
    }

    @Test
    public void testEmptyKeyword() throws Exception {
        network.addUser(1, "Alice", 25);
        network.addUser(2, "Bob", 30);
        network.uploadVideo(2, 101, "tech");
        network.sendComment(1, 101, 111, "abc");
        network.sendComment(1, 101, 112, "hello world");
        network.sendComment(1, 101, 113, "banana");

        assertArrayEquals(new int[]{3, 12}, network.cleanSpamComments(101, ""));
        assertEquals(0, getCommentMap(network, 101).size());
    }

    @Test
    public void testPure1() throws Exception {
        network.addUser(1, "Alice", 25);
        network.addUser(2, "Bob", 30);
        network.uploadVideo(2, 101, "tech");
        network.sendComment(1, 101, 111, "aaaaa");
        network.sendComment(1, 101, 112, "bbbbb");
        network.sendComment(1, 101, 113, "ccccc");
        network.cleanSpamComments(101, "a");
        VideoInterface video = network.getVideo(101);
        int[] commentIds = ((Video) video).getCommentIds();
        String[] comments = ((Video) video).getCommentContents();
        assertNotNull(commentIds);
        assertNotNull(comments);
        assertEquals(2, commentIds.length);
        assertEquals(2, comments.length);
        HashMap<Integer, String> commentMap = getCommentMap(network, 101);
        assertEquals("bbbbb", commentMap.get(112));
        assertEquals("ccccc", commentMap.get(113));
        assertFalse(commentMap.containsKey(111));
        assertTrue(Arrays.asList(comments).contains("bbbbb")
                && Arrays.asList(comments).contains("ccccc"));
    }

    @Test
    public void testPure2() throws Exception {
        Network network1 = new Network();
        network.addUser(1, "Alice", 25);
        network.addUser(2, "Bob", 30);
        network.addUser(3, "Cindy", 35);
        network1.addUser(1, "Alice", 25);
        network1.addUser(2, "Bob", 30);
        network1.addUser(3, "Cindy", 35);

        network.uploadVideo(1, 101, "tech");
        network1.uploadVideo(1, 101, "tech");
        network.watchVideo(2, 101);
        network1.watchVideo(2, 101);
        network.followUser(3, 2);
        network1.followUser(3, 2);
        network.forwardVideo(2, 101, 3);
        network1.forwardVideo(2, 101, 3);
        network.addUserCoins(2, 10);
        network1.addUserCoins(2, 10);
        network.coinVideo(2, 101, 2);
        network1.coinVideo(2, 101, 2);
        network.watchVideo(3, 101);
        network1.watchVideo(3, 101);
        network.likeVideo(3, 101);
        network1.likeVideo(3, 101);

        network.sendComment(1, 101, 111, "aaaaa");
        network.sendComment(1, 101, 112, "bbbbb");
        network.sendComment(1, 101, 113, "ccccc");
        network1.sendComment(1, 101, 111, "aaaaa");
        network1.sendComment(1, 101, 112, "bbbbb");
        network1.sendComment(1, 101, 113, "ccccc");
        VideoInterface video1 = network.getVideo(101);
        network1.cleanSpamComments(101, "a");
        VideoInterface video2 = network1.getVideo(101);
        assertEquals(video1.getId(), video2.getId());
        assertEquals(video1.getUploaderId(), video2.getUploaderId());
        assertEquals(video1.getType(), video2.getType());
        assertEquals(video1.getPlayCount(), video2.getPlayCount());
        assertEquals(video1.getLikes(), video2.getLikes());
        assertEquals(video1.getForwardCount(), video2.getForwardCount());
        assertEquals(video1.getCoins(), video2.getCoins());
        assertEquals(video1.getHeat(), video2.getHeat(), 0.0);
    }

    private HashMap<Integer, String> getCommentMap(Network targetNetwork, int videoId) {
        Video video = (Video) targetNetwork.getVideo(videoId);
        int[] ids = video.getCommentIds();
        String[] comments = video.getCommentContents();
        assertNotNull(ids);
        assertNotNull(comments);
        assertEquals(ids.length, comments.length);
        HashMap<Integer, String> commentMap = new HashMap<>();
        for (int i = 0; i < ids.length; i++) {
            commentMap.put(ids[i], comments[i]);
        }
        return commentMap;
    }
}
