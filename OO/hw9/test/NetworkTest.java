import com.oocourse.spec1.main.UserInterface;
import org.junit.Before;
import org.junit.Test;
import com.oocourse.spec1.exceptions.*;

import java.util.HashMap;

import static org.junit.Assert.*;

public class NetworkTest {

    private Network network;

    @Before
    public void setUp() {
        network = new Network();
    }

    @Test
    public void testEmptyNetwork() {
        assertEquals(0, network.queryMutualFollowingSum());
    }

    @Test
    public void testSingleFollow() throws Exception {
        network.addUser(1, "Alice", 30);
        network.addUser(2, "Bob", 25);
        network.followUser(1, 2);
        assertEquals(0, network.queryMutualFollowingSum());
    }

    @Test
    public void testOneMutualFollow() throws Exception {
        network.addUser(1, "Alice", 30);
        network.addUser(2, "Bob", 25);
        network.followUser(1, 2);
        network.followUser(2, 1);
        assertEquals(1, network.queryMutualFollowingSum());
    }

    @Test
    public void testMultipleMutualFollows() throws Exception {
        network.addUser(1, "Alice", 30);
        network.addUser(2, "Bob", 25);
        network.addUser(3, "Charlie", 20);
        network.followUser(1, 2);
        network.followUser(2, 1);
        network.followUser(1, 3);
        network.followUser(3, 1);
        network.followUser(2, 3);
        assertEquals(2, network.queryMutualFollowingSum());
    }

    @Test
    public void testPure() throws Exception {
        Network network2 = new Network();

        network.addUser(1, "Alice", 30);
        network.addUser(2, "Bob", 25);
        network.addUser(3, "Charlie", 20);
        network.addUser(4, "David", 35);
        network.followUser(1, 2);
        network.followUser(2, 1);
        network.followUser(3, 1);
        network.followUser(1, 3);
        network.followUser(2, 3);
        network.followUser(1, 4);
        network.followUser(2, 4);

        network2.addUser(1, "Alice", 30);
        network2.addUser(2, "Bob", 25);
        network2.addUser(3, "Charlie", 20);
        network2.addUser(4, "David", 35);
        network2.followUser(1, 2);
        network2.followUser(2, 1);
        network2.followUser(3, 1);
        network2.followUser(1, 3);
        network2.followUser(2, 3);
        network2.followUser(1, 4);
        network2.followUser(2, 4);

        UserInterface[] beforeUsers = network2.getUsers();
        int ans = network.queryMutualFollowingSum();
        UserInterface[] afterUsers = network.getUsers();
        assertEquals(2, ans);
        assertEquals(beforeUsers.length, afterUsers.length);
        for (int i = 0; i < beforeUsers.length; i++) {
            boolean isEqual = ((User) beforeUsers[i]).strictEquals(afterUsers[i]);
            assertTrue(isEqual);
        }
    }
}