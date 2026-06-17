import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.LibraryBookState;
import com.oocourse.library3.annotation.SendMessage;

import java.time.LocalDate;

// 表示具体的一本副本
public class Book {
    private User owner;
    private LibraryBookId bookId;
    private LibraryBookIsbn bookIsbn;
    private LibraryBookState state;
    private User reservedFor;
    private User reader;
    private LocalDate dueDate;
    private boolean overduePenalized;

    public Book(LibraryBookId bookId) {
        this.bookId = bookId;
        this.bookIsbn = bookId.getBookIsbn();
        this.state = LibraryBookState.BOOKSHELF;
    }

    @SendMessage(from = "User", to = "Book")
    public LibraryBookId getBookId() {
        return this.bookId;
    }

    public LibraryBookIsbn getBookIsbn() {
        return bookIsbn;
    }

    public void setState(LibraryBookState state) {
        this.state = state;
    }

    @SendMessage(from = "Reservation", to = "Book")
    public void setOwner(User user) {
        this.owner = user;
    }

    public User getOwner() {
        return owner;
    }

    public User getReservedFor() {
        return reservedFor;
    }

    public void setReservedFor(User reservedFor) {
        this.reservedFor = reservedFor;
    }

    public LibraryBookState getState() {
        return state;
    }

    public void setReader(User reader) {
        this.reader = reader;
    }

    public User getReader() {
        return reader;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isOverduePenalized() {
        return overduePenalized;
    }

    public void setOverduePenalized(boolean overduePenalized) {
        this.overduePenalized = overduePenalized;
    }
}
