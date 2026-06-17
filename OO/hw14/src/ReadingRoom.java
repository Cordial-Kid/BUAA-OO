import java.util.HashSet;
import java.util.Set;

public class ReadingRoom {
    private final Set<Book> booksInReadingRoom;

    public ReadingRoom() {
        this.booksInReadingRoom = new HashSet<>();
    }

    public void removeBooks(Book book) {
        booksInReadingRoom.remove(book);
    }

    public void addBooks(Book book) {
        booksInReadingRoom.add(book);
    }

    public Set<Book> getRestBook() {
        return booksInReadingRoom;
    }
}
