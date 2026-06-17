import com.oocourse.library3.LibraryBookState;

public class TreasuredBookshelf extends Bookshelf {
    @Override
    protected LibraryBookState getBookState() {
        return LibraryBookState.TREASURED_BOOKSHELF;
    }
}
