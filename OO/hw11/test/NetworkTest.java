import com.oocourse.spec3.exceptions.ColdStartUserException;
import com.oocourse.spec3.exceptions.InvalidRankException;
import com.oocourse.spec3.exceptions.NoVideoUploadedException;
import com.oocourse.spec3.exceptions.UserIdNotFoundException;
import com.oocourse.spec3.main.UserInterface;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class NetworkTest {
    Network network;

    @Before
    public void setUp() throws Exception {
        network = new Network();
        network.addUser(1, "Alice", 20);
        network.addUser(2, "Bob", 25);
        network.addUser(3, "Charlie", 30);
        network.addUser(4, "David", 35);
        network.uploadVideo(4, 100, "tech");
    }

    @Test
    public void noFollowing() throws Exception {
        assertEquals(2, network.recommendNthUp(1, 1));
        assertEquals(3, network.recommendNthUp(1, 2));
        assertEquals(4, network.recommendNthUp(1, 3));
    }

    @Test
    public void hasFollowing() throws Exception {
        network.followUser(1, 2);
        assertEquals(3, network.recommendNthUp(1, 1));
        assertEquals(4, network.recommendNthUp(1, 2));
    }

    @Test
    public void hasInfluence() throws Exception {
        network.uploadVideo(3, 101, "music");
        network.watchVideo(1,100);
        network.watchVideo(2,100);
        network.watchVideo(2,101);
        assertEquals(4, network.recommendNthUp(1, 1));
        assertEquals(2, network.recommendNthUp(1, 2));
        assertEquals(3, network.recommendNthUp(1, 3));
    }

    @Test
    public void hasSameType() throws Exception {
        network.uploadVideo(3,101, "tech");
        network.watchVideo(2, 100);
        network.watchVideo(2, 101);
        network.watchVideo(4, 100);
        network.watchVideo(1, 100);
        assertEquals(4, network.recommendNthUp(1, 1));
        assertEquals(3, network.recommendNthUp(1, 2));
        assertEquals(2, network.recommendNthUp(1, 3));
    }

    @Test
    public void testException() throws Exception {
        try {
            network.recommendNthUp(5, 1);
        } catch (Exception e) {
            assertTrue(e instanceof UserIdNotFoundException);
        }

        try {
            network.recommendNthUp(1, 0);
        } catch (Exception e) {
            assertTrue(e instanceof InvalidRankException);
        }

        try {
            network.followUser(1, 2);
            network.recommendNthUp(1, 3);
        } catch (Exception e) {
            assertTrue(e instanceof ColdStartUserException);
        }

        Network network2 = new Network();
        network2.addUser(1, "Alice", 20);
        network2.addUser(2, "Bob", 25);
        try {
            network2.recommendNthUp(1, 1);
        } catch (Exception e) {
            assertTrue(e instanceof NoVideoUploadedException);
        }
    }

    @Test
    public void testPure() throws Exception {
        Network network2 = new Network();
        network2.addUser(1, "Alice", 20);
        network2.addUser(2, "Bob", 25);
        network2.addUser(3, "Charlie", 30);
        network2.addUser(4, "David", 35);
        network2.uploadVideo(4, 100, "tech");

        network.recommendNthUp(1, 1);
        UserInterface[] users1 = network.getUsers();
        UserInterface[] users2 = network2.getUsers();
        for (int i = 0; i < users1.length; i++) {
            User u1 = (User) users1[i];
            User u2 = (User) users2[i];
            assertTrue(u1.strictEquals(u2));
        }
    }
}