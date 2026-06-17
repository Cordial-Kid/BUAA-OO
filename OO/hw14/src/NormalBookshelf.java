import com.oocourse.library2.LibraryBookState;

public class NormalBookshelf extends Bookshelf {
    @Override
    protected LibraryBookState getBookState() {
        return LibraryBookState.BOOKSHELF;
    }
}
