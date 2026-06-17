import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.annotation.SendMessage;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class User {
    private static final int MAX_CREDIT = 180;
    private static final int MIN_CREDIT = 0;

    private final String userId;
    private final Map<LibraryBookIsbn, Book> borrowedBooks;
    private final Set<LibraryBookIsbn> orderedBooks;
    private final Set<LibraryBookIsbn> readingBooks;
    private int creditScore;

    public User(String userId) {
        this.userId = userId;
        this.borrowedBooks = new HashMap<>();
        this.orderedBooks = new HashSet<>();
        this.readingBooks = new HashSet<>();
        this.creditScore = 100;
    }

    public void readBook(LibraryBookIsbn isbn) {
        readingBooks.add(isbn);
    }

    public boolean hasReadingBook() {
        return !readingBooks.isEmpty();
    }

    public boolean hasBorrowedB() {
        for (LibraryBookIsbn isbn : borrowedBooks.keySet()) {
            if (isbn.isTypeB()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasOrder() {
        return !orderedBooks.isEmpty();
    }

    public Book returnBook(LibraryBookId bookId) {
        Book book = borrowedBooks.remove(bookId.getBookIsbn());
        if (book != null) {
            book.setDueDate(null);
        }
        return book;
    }

    public void removeReadingBook(Book book) {
        readingBooks.remove(book.getBookIsbn());
    }

    @SendMessage(from = "Book", to = "User")
    public void borrowBook(Book book, LocalDate date) {
        borrowedBooks.put(book.getBookIsbn(), book);
        book.setDueDate(date.plusDays(getBorrowingPeriod(book.getBookIsbn())));
        book.setOverduePenalized(false);
    }

    public Book getBorrowedBook(LibraryBookId bookId) {
        Book book = borrowedBooks.get(bookId.getBookIsbn());
        if (book != null && book.getBookId().equals(bookId)) {
            return book;
        }
        return null;
    }

    public boolean hasBorrowedIsbn(LibraryBookIsbn isbn) {
        return borrowedBooks.containsKey(isbn);
    }

    @SendMessage(from = "AppointmentOffice", to = "User")
    public void addOrder(LibraryBookIsbn isbn) {
        orderedBooks.add(isbn);
    }

    public void removeOrder(LibraryBookIsbn isbn) {
        orderedBooks.remove(isbn);
    }

    public String getUserId() {
        return userId;
    }

    @SendMessage(from = "Book", to = "User")
    public void getOrderedBook() {
    }

    public int getCreditScore() {
        return creditScore;
    }

    public void increaseCredit(int value) {
        creditScore = Math.min(MAX_CREDIT, creditScore + value);
    }

    public void decreaseCredit(int value) {
        creditScore = Math.max(MIN_CREDIT, creditScore - value);
    }

    public boolean canRead(LibraryBookIsbn isbn) {
        if (isbn.isTypeA()) {
            return creditScore > 40;
        }
        return creditScore > 0;
    }

    public boolean canBorrowOrOrder() {
        return creditScore > 80;
    }

    public boolean isOverdue(Book book, LocalDate date) {
        return book.getDueDate() != null && book.getDueDate().isBefore(date);
    }

    public boolean renewBook(LibraryBookId bookId, LocalDate date) {
        Book book = getBorrowedBook(bookId);
        if (book == null || isOverdue(book, date)) {
            return false;
        }
        book.setDueDate(book.getDueDate().plusDays(7));
        return true;
    }

    public void processOverdueLoans(LocalDate date) {
        for (Book book : borrowedBooks.values()) {
            if (isOverdue(book, date) && !book.isOverduePenalized()) {
                decreaseCredit(15);
                book.setOverduePenalized(true);
            }
        }
    }

    private int getBorrowingPeriod(LibraryBookIsbn isbn) {
        if (isbn.isTypeB()) {
            return 15;
        }
        return 30;
    }
}
