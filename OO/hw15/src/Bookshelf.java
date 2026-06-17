import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.LibraryBookState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

// 管理书架书目
public abstract class Bookshelf {
    private Map<LibraryBookIsbn, List<Book>> booksInShelf;       // 这种只有写开才能遇到数据结构不合理的问题啊

    public Bookshelf() {
        this.booksInShelf = new LinkedHashMap<>();
    }

    protected abstract LibraryBookState getBookState();

    public void addBook(Book book) {
        LibraryBookIsbn isbn = book.getBookIsbn();
        booksInShelf.computeIfAbsent(isbn, key -> new ArrayList<>()).add(book);
        book.setState(getBookState());
    }

    public boolean hasAvailableBook(LibraryBookIsbn isbn) {
        List<Book> books = booksInShelf.get(isbn);
        return books != null && !books.isEmpty();
    }

    public Book getAvailableBook(LibraryBookIsbn isbn) {
        List<Book> books = booksInShelf.get(isbn);
        if (books == null || books.isEmpty()) {
            return null;
        }
        return books.get(0);
    }

    public void removeBook(Book book) {
        LibraryBookIsbn isbn = book.getBookIsbn();
        List<Book> books = booksInShelf.get(isbn);
        if (books != null) {
            books.remove(book);
            if (books.isEmpty()) {
                booksInShelf.remove(isbn);
            }
        }
    }

    public Map<LibraryBookIsbn, List<Book>> getBooksInShelf() {
        return booksInShelf;
    }

}
