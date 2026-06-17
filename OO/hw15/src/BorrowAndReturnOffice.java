import com.oocourse.library3.LibraryBookIsbn;

import java.util.ArrayList;
import java.util.List;

// 借还处，保存归还但尚未处理的书
public class BorrowAndReturnOffice {
    private final ArrayList<Book> books;

    public BorrowAndReturnOffice() {
        this.books = new ArrayList<>();
    }

    public void addBook(Book book) {
        books.add(book);
    }

    public List<Book> getBooks() {
        return books;
    }

    public List<Book> getBooksByIsbn(LibraryBookIsbn isbn) {
        List<Book> res = new ArrayList<>();
        for (Book book : books) {
            if (book.getBookIsbn().equals(isbn)) {
                res.add(book);
            }
        }
        return res;
    }
}
