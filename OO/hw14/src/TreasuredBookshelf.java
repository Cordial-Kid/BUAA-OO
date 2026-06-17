import com.oocourse.library2.LibraryBookState;

public class TreasuredBookshelf extends Bookshelf {
    @Override
    protected LibraryBookState getBookState() {
        return LibraryBookState.TREASURED_BOOKSHELF;
    }
}
