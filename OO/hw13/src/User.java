import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// 用户，维护借阅和预约状态
public class User {
    private String userId;
    private final Map<LibraryBookIsbn, Book> borrowedBooks;    // 已经借到的书
    private final Set<LibraryBookIsbn> orderedBooks;
    private final Set<LibraryBookId> borrowedBookIds;

    public User(String userId) {
        this.userId = userId;
        this.borrowedBooks = new HashMap<>();
        this.orderedBooks = new HashSet<>();
        this.borrowedBookIds = new HashSet<>();
    }

    public boolean hasBorrowedB() {
        for (Map.Entry<LibraryBookIsbn, Book> entry : borrowedBooks.entrySet()) {
            LibraryBookIsbn isbn = entry.getKey();
            if (isbn.isTypeB()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasOrder() {
        return !orderedBooks.isEmpty();
    }

    public Book returnBook(LibraryBookIsbn isbn) {
        Book book = borrowedBooks.get(isbn);
        borrowedBooks.remove(isbn);
        return book;
    }

    public void borrowBook(Book book) {
        borrowedBooks.put(book.getBookIsbn(), book);
    }

    public boolean hasBorrowedIsbn(LibraryBookIsbn isbn) {
        return borrowedBooks.containsKey(isbn);
    }

    public void addOrder(LibraryBookIsbn isbn) {
        orderedBooks.add(isbn);
    }

    public void removeOrder(LibraryBookIsbn isbn) {
        orderedBooks.remove(isbn);
    }

    public String getUserId() {
        return userId;
    }
}
