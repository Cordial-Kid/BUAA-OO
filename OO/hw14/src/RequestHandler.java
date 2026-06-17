import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;
import com.oocourse.library2.LibraryBookState;
import com.oocourse.library2.LibraryCommand;
import com.oocourse.library2.LibraryReqCmd;
import com.oocourse.library2.LibraryTrace;
import com.oocourse.library2.annotation.Trigger;

import java.time.LocalDate;
import java.util.List;

import static com.oocourse.library2.LibraryIO.PRINTER;

// 处理用户请求，借书，还书，预约，查询
public class RequestHandler {
    private final LibraryManager libraryManager;             // 关于book和user
    private final Bookshelves bookshelves;
    private final AppointmentOffice appointmentOffice;
    private final BorrowAndReturnOffice borrowAndReturnOffice;
    private final TraceRecorder traceRecorder;
    private final ReadingRoom readingRoom;
    private final GradeManager gradeManager;

    public RequestHandler(LibraryManager libraryManager,
                          Bookshelves bookshelves,
                          AppointmentOffice appointmentOffice,
                          BorrowAndReturnOffice borrowAndReturnOffice,
                          TraceRecorder traceRecorder,
                          ReadingRoom readingRoom,
                          GradeManager gradeManager) {
        this.libraryManager = libraryManager;
        this.bookshelves = bookshelves;
        this.appointmentOffice = appointmentOffice;
        this.borrowAndReturnOffice = borrowAndReturnOffice;
        this.traceRecorder = traceRecorder;
        this.readingRoom = readingRoom;
        this.gradeManager = gradeManager;
    }

    public void handle(LibraryCommand command) {
        LibraryReqCmd req = (LibraryReqCmd) command;
        LibraryReqCmd.Type type = req.getType(); // 指令对应的类型（查询/借阅/预约/还书/取书）
        if (type == LibraryReqCmd.Type.BORROWED) {
            handleBorrow(req);
        } else if (type == LibraryReqCmd.Type.ORDERED) {
            handleOrder(req);
        } else if (type == LibraryReqCmd.Type.PICKED) {
            handlePick(req);
        } else if (type == LibraryReqCmd.Type.RETURNED) {
            handleReturn(req);
        } else if (type == LibraryReqCmd.Type.READ) {
            handleRead(req);
        } else if (type == LibraryReqCmd.Type.RESTORED) {
            handleRestore(req);
        } else if (type == LibraryReqCmd.Type.GRADED) {
            handleGrade(req);
        } else {
            handleQuery(req);
        }
    }

    @Trigger(from = "BOOKSHELF", to = "READING_ROOM")
    @Trigger(from = "TREASURED_BOOKSHELF", to = "READING_ROOM")
    private void handleRead(LibraryReqCmd req) {
        LibraryBookIsbn isbn = req.getBookIsbn();
        String studentId = req.getStudentId();
        User user = libraryManager.getUser(studentId);
        if (!canRead(user, isbn)) {
            PRINTER.reject(req);
            return;
        }
        Book book = bookshelves.getAvailableBook(isbn);
        bookshelves.removeBook(book);
        readingRoom.addBooks(book);
        user.readBook(isbn);
        book.setReader(user);
        traceRecorder.record(book, req.getDate(), book.getState(), LibraryBookState.READING_ROOM);
        book.setState(LibraryBookState.READING_ROOM);
        PRINTER.accept(req, book.getBookId());
    }

    @Trigger(from = "READING_ROOM", to = "BORROW_RETURN_OFFICE")
    private void handleRestore(LibraryReqCmd req) {
        String studentId = req.getStudentId();
        LibraryBookId bookId = req.getBookId();
        final User user = libraryManager.getUser(studentId);
        Book book = libraryManager.getBook(bookId);
        readingRoom.removeBooks(book);
        borrowAndReturnOffice.addBook(book);
        book.setState(LibraryBookState.BORROW_RETURN_OFFICE);
        book.setReader(null);
        user.removeReadingBook(book);
        traceRecorder.record(book, req.getDate(),
                LibraryBookState.READING_ROOM, LibraryBookState.BORROW_RETURN_OFFICE);
        PRINTER.accept(req);
    }

    private void handleGrade(LibraryReqCmd req) {
        LibraryBookIsbn isbn = req.getBookIsbn();
        int grade = req.getScore();
        gradeManager.changeGrade(isbn, grade);
        PRINTER.accept(req);
    }

    @Trigger(from = "BOOKSHELF", to = "USER")
    @Trigger(from = "TREASURED_BOOKSHELF", to = "USER")
    private void handleBorrow(LibraryReqCmd req) {
        String studentId = req.getStudentId(); // 指令对应的用户Id
        LibraryBookIsbn isbn = req.getBookIsbn(); // 指令对应的书籍ISBN号
        final LocalDate date = req.getDate(); // 指令对应的日期
        User user = libraryManager.getUser(studentId);
        if (!canBorrow(user, isbn)) {
            PRINTER.reject(req);
            return;
        }
        Book book = bookshelves.getAvailableBook(isbn);
        bookshelves.removeBook(book);
        user.borrowBook(book);   // 目前只有user知道自己拿着book， book并不知道自己被谁拿着
        book.setOwner(user);
        traceRecorder.record(book, date, book.getState(), LibraryBookState.USER);
        book.setState(LibraryBookState.USER);
        PRINTER.accept(req, book.getBookId());
    }

    private void handleOrder(LibraryReqCmd req) {
        String studentId = req.getStudentId(); // 指令对应的用户Id
        LibraryBookIsbn isbn = req.getBookIsbn();
        User user = libraryManager.getUser(studentId);
        if (!canOrder(user, isbn)) {
            PRINTER.reject(req);
            return;
        }
        appointmentOffice.addOrder(isbn, user);
        PRINTER.accept(req);
    }

    @Trigger(from = "APPOINTMENT_OFFICE", to = "USER")
    private void handlePick(LibraryReqCmd req) {
        String studentId = req.getStudentId(); // 指令对应的用户Id
        LibraryBookIsbn isbn = req.getBookIsbn();
        User user = libraryManager.getUser(studentId);
        final LocalDate date = req.getDate();
        if (!canPick(user, isbn)) {
            PRINTER.reject(req);
            return;
        }
        Book book = appointmentOffice.removeReservedBook(user, isbn);
        if (book == null) {
            PRINTER.reject(req);
            return;
        }
        book.setState(LibraryBookState.USER);
        book.setOwner(user);
        book.setReservedFor(null);
        user.borrowBook(book);
        user.removeOrder(isbn);
        traceRecorder.record(book, date,
                LibraryBookState.APPOINTMENT_OFFICE,
                LibraryBookState.USER);
        PRINTER.accept(req, book.getBookId());
    }

    @Trigger(from = "USER", to = "BORROW_RETURN_OFFICE")
    private void handleReturn(LibraryReqCmd req) {
        String studentId = req.getStudentId(); // 指令对应的用户Id
        // LibraryBookId bookId = req.getBookId();
        LibraryBookIsbn isbn = req.getBookIsbn();
        User user = libraryManager.getUser(studentId);
        final LocalDate date = req.getDate();
        Book book = user.returnBook(isbn);
        borrowAndReturnOffice.addBook(book);
        book.setState(LibraryBookState.BORROW_RETURN_OFFICE);
        book.setOwner(null);
        traceRecorder.record(book, date,
                LibraryBookState.USER,
                LibraryBookState.BORROW_RETURN_OFFICE);
        PRINTER.accept(req);
    }

    private void handleQuery(LibraryReqCmd req) {
        LibraryBookId bookId = req.getBookId();
        LocalDate date = req.getDate();
        List<LibraryTrace> traces = traceRecorder.getTraces(bookId);
        PRINTER.info(date, bookId, traces);
    }

    private boolean canBorrow(User user, LibraryBookIsbn isbn) {
        if (!bookshelves.hasAvailableBook(isbn)) {
            return false;
        }
        if (isbn.isTypeA()) {
            return false;
        }
        if (isbn.isTypeB()) {
            return !user.hasBorrowedB();
        }
        if (isbn.isTypeC()) {
            return !user.hasBorrowedIsbn(isbn);
        }
        return true;
    }

    private boolean canOrder(User user, LibraryBookIsbn isbn) {
        if (user.hasOrder()) {
            return false;
        }
        if (isbn.isTypeA()) {
            return false;
        }
        if (isbn.isTypeB()) {
            return !user.hasBorrowedB();
        }
        if (isbn.isTypeC()) {
            return !user.hasBorrowedIsbn(isbn);
        }
        return true;
    }

    // 可能在预约完之后直接借了一本，此时就不能再取了
    private boolean canPick(User user, LibraryBookIsbn isbn) {
        if (!appointmentOffice.hasReservedBook(user, isbn)) {
            return false;
        }
        if (isbn.isTypeA()) {
            return false;
        }
        if (isbn.isTypeB()) {
            return !user.hasBorrowedB();
        }
        if (isbn.isTypeC()) {
            return !user.hasBorrowedIsbn(isbn);
        }
        return true;
    }

    private boolean canRead(User user, LibraryBookIsbn isbn) {
        if (!bookshelves.hasAvailableBook(isbn)) {
            return false;
        }
        if (user.hasReadingBook()) {
            return false;
        }
        return true;
    }
}
