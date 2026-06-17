// 系统管理

import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;
import com.oocourse.library2.LibraryCommand;
import com.oocourse.library2.LibraryCloseCmd;
import com.oocourse.library2.LibraryOpenCmd;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LibraryManager {
    private final Map<LibraryBookId, Book> books;
    private final Map<String, User> users;

    private final RequestHandler requestHandler;
    private final ArrangeHandler arrangeHandler;

    private final Bookshelves bookshelves;
    private final AppointmentOffice appointmentOffice;
    private final BorrowAndReturnOffice borrowAndReturnOffice;

    private final TraceRecorder traceRecorder;
    private final GradeManager gradeManager;
    private final ReadingRoom readingRoom;

    public LibraryManager(Map<LibraryBookIsbn, Integer> booklist) {
        this.books = new LinkedHashMap<>();
        this.users = new HashMap<>();
        this.bookshelves = new Bookshelves();
        this.appointmentOffice = new AppointmentOffice();
        this.borrowAndReturnOffice = new BorrowAndReturnOffice();
        this.traceRecorder = new TraceRecorder();
        this.gradeManager = new GradeManager();
        this.readingRoom = new ReadingRoom();
        initBooks(booklist);
        this.requestHandler = new RequestHandler(this, bookshelves,
                appointmentOffice,
                borrowAndReturnOffice,
                traceRecorder,
                readingRoom,
                gradeManager);
        this.arrangeHandler = new ArrangeHandler(bookshelves,
                appointmentOffice,
                borrowAndReturnOffice,
                traceRecorder,
                gradeManager,
                readingRoom);
    }

    private void initBooks(Map<LibraryBookIsbn, Integer> booklist) {
        for (Map.Entry<LibraryBookIsbn, Integer> entry : booklist.entrySet()) {
            LibraryBookIsbn isbn = entry.getKey();
            int count = entry.getValue();
            for (int i = 1; i <= count; i++) {
                LibraryBookId bookId = new LibraryBookId(isbn.getType(),
                        isbn.getUid(),
                        String.format("%02d", i));
                Book book = new Book(bookId);
                this.books.put(bookId, book);
                this.bookshelves.addToNormal(book);
            }
        }
    }

    public void handleCommand(LibraryCommand command) {
        LocalDate date = command.getDate(); // 指令对应的日期
        if (command instanceof LibraryOpenCmd) {
            arrangeHandler.handleOpen(date);
        } else if (command instanceof LibraryCloseCmd) {
            arrangeHandler.handleClose(date);
        } else {
            requestHandler.handle(command);
        }
    }

    public User getUser(String studentId) {
        if (!users.containsKey(studentId)) {
            users.put(studentId, new User(studentId));
        }
        return users.get(studentId);
    }

    public Book getBook(LibraryBookId bookId) {
        return books.get(bookId);
    }
}
