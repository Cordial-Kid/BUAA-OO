import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.LibraryBookState;

public class Bookshelves {
    private final NormalBookshelf normalBookshelf = new NormalBookshelf();
    private final TreasuredBookshelf treasuredBookshelf = new TreasuredBookshelf();

    public boolean hasAvailableBook(LibraryBookIsbn isbn) {
        if (normalBookshelf.hasAvailableBook(isbn) || treasuredBookshelf.hasAvailableBook(isbn)) {
            return true;
        }
        return false;
    }

    public Book getAvailableBook(LibraryBookIsbn isbn) {
        if (normalBookshelf.hasAvailableBook(isbn)) {
            return normalBookshelf.getAvailableBook(isbn);
        }
        if (treasuredBookshelf.hasAvailableBook(isbn)) {
            return treasuredBookshelf.getAvailableBook(isbn);
        }
        return null;
    }

    public void removeBook(Book book) {
        LibraryBookIsbn isbn = book.getBookIsbn();
        if (book.getState() == LibraryBookState.BOOKSHELF) {
            normalBookshelf.removeBook(book);
        } else if (book.getState() == LibraryBookState.TREASURED_BOOKSHELF) {
            treasuredBookshelf.removeBook(book);
        }
    }

    public NormalBookshelf getNormalBookshelf() {
        return normalBookshelf;
    }

    public TreasuredBookshelf getTreasuredBookshelf() {
        return treasuredBookshelf;
    }

    public void addToNormal(Book book) {
        normalBookshelf.addBook(book);
    }

    public void addToTreasure(Book book) {
        treasuredBookshelf.addBook(book);
    }

}
