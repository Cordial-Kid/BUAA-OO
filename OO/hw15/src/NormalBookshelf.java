import com.oocourse.library3.LibraryBookState;

public class NormalBookshelf extends Bookshelf {
    @Override
    protected LibraryBookState getBookState() {
        return LibraryBookState.BOOKSHELF;
    }
}
