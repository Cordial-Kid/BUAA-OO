import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryBookState;

// 表示具体的一本副本
public class Book {
    private User owner;
    private LibraryBookId bookId;
    private LibraryBookIsbn bookIsbn;
    private LibraryBookState state;
    private User reservedFor;

    public Book(LibraryBookId bookId) {
        this.bookId = bookId;
        this.bookIsbn = bookId.getBookIsbn();
        this.state = LibraryBookState.BOOKSHELF;
    }

    public LibraryBookId getBookId() {
        return this.bookId;
    }

    public LibraryBookIsbn getBookIsbn() {
        return bookIsbn;
    }

    public void setState(LibraryBookState state) {
        this.state = state;
    }

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

    public boolean isOnShelf() {
        return this.state == LibraryBookState.BOOKSHELF;
    }

    public boolean isBorrowed() {
        return this.state == LibraryBookState.USER;
    }

    public boolean isReserved() {
        return this.state == LibraryBookState.APPOINTMENT_OFFICE;
    }

    public boolean isInBorrowAndReturnOffice() {
        return this.state == LibraryBookState.BORROW_RETURN_OFFICE;
    }
}
