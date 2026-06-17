import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.LibraryBookState;
import com.oocourse.library3.LibraryCommand;
import com.oocourse.library3.LibraryQcsCmd;
import com.oocourse.library3.LibraryReqCmd;
import com.oocourse.library3.LibraryTrace;
import com.oocourse.library3.annotation.Trigger;

import java.time.LocalDate;
import java.util.List;

import static com.oocourse.library3.LibraryIO.PRINTER;

public class RequestHandler {
    private final LibraryManager libraryManager;
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
        if (command instanceof LibraryQcsCmd) {
            handleCreditQuery((LibraryQcsCmd) command);
            return;
        }
        LibraryReqCmd req = (LibraryReqCmd) command;
        LibraryReqCmd.Type type = req.getType();
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
        } else if (type == LibraryReqCmd.Type.RENEWED) {
            handleRenew(req);
        } else {
            handleQuery(req);
        }
    }

    private void handleCreditQuery(LibraryQcsCmd req) {
        User user = libraryManager.getUser(req.getStudentId());
        PRINTER.info(req.getDate(), req.getStudentId(), user.getCreditScore());
    }

    @Trigger(from = "BOOKSHELF", to = "READING_ROOM")
    @Trigger(from = "TREASURED_BOOKSHELF", to = "READING_ROOM")
    private void handleRead(LibraryReqCmd req) {
        LibraryBookIsbn isbn = req.getBookIsbn();
        User user = libraryManager.getUser(req.getStudentId());
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
        final User user = libraryManager.getUser(req.getStudentId());
        Book book = libraryManager.getBook(req.getBookId());
        readingRoom.removeBooks(book);
        borrowAndReturnOffice.addBook(book);
        book.setState(LibraryBookState.BORROW_RETURN_OFFICE);
        book.setReader(null);
        user.removeReadingBook(book);
        user.increaseCredit(10);
        traceRecorder.record(book, req.getDate(),
                LibraryBookState.READING_ROOM, LibraryBookState.BORROW_RETURN_OFFICE);
        PRINTER.accept(req);
    }

    private void handleGrade(LibraryReqCmd req) {
        gradeManager.changeGrade(req.getBookIsbn(), req.getScore());
        PRINTER.accept(req);
    }

    @Trigger(from = "BOOKSHELF", to = "USER")
    @Trigger(from = "TREASURED_BOOKSHELF", to = "USER")
    private void handleBorrow(LibraryReqCmd req) {
        LibraryBookIsbn isbn = req.getBookIsbn();
        LocalDate date = req.getDate();
        User user = libraryManager.getUser(req.getStudentId());
        if (!canBorrow(user, isbn)) {
            PRINTER.reject(req);
            return;
        }
        Book book = bookshelves.getAvailableBook(isbn);
        bookshelves.removeBook(book);
        user.borrowBook(book, date);
        book.setOwner(user);
        traceRecorder.record(book, date, book.getState(), LibraryBookState.USER);
        book.setState(LibraryBookState.USER);
        PRINTER.accept(req, book.getBookId());
    }

    private void handleOrder(LibraryReqCmd req) {
        LibraryBookIsbn isbn = req.getBookIsbn();
        User user = libraryManager.getUser(req.getStudentId());
        if (!canOrder(user, isbn)) {
            PRINTER.reject(req);
            return;
        }
        appointmentOffice.addOrder(isbn, user);
        PRINTER.accept(req);
    }

    @Trigger(from = "APPOINTMENT_OFFICE", to = "USER")
    private void handlePick(LibraryReqCmd req) {
        LibraryBookIsbn isbn = req.getBookIsbn();
        final LocalDate date = req.getDate();
        User user = libraryManager.getUser(req.getStudentId());
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
        user.borrowBook(book, date);
        user.removeOrder(isbn);
        traceRecorder.record(book, date,
                LibraryBookState.APPOINTMENT_OFFICE,
                LibraryBookState.USER);
        PRINTER.accept(req, book.getBookId());
    }

    @Trigger(from = "USER", to = "BORROW_RETURN_OFFICE")
    private void handleReturn(LibraryReqCmd req) {
        LibraryBookId bookId = req.getBookId();
        LocalDate date = req.getDate();
        User user = libraryManager.getUser(req.getStudentId());
        Book book = user.getBorrowedBook(bookId);
        boolean overdue = user.isOverdue(book, date);
        if (overdue && !book.isOverduePenalized()) {
            user.decreaseCredit(15);
            book.setOverduePenalized(true);
        } else if (!overdue) {
            user.increaseCredit(10);
        }
        user.returnBook(bookId);
        borrowAndReturnOffice.addBook(book);
        book.setState(LibraryBookState.BORROW_RETURN_OFFICE);
        book.setOwner(null);
        traceRecorder.record(book, date,
                LibraryBookState.USER,
                LibraryBookState.BORROW_RETURN_OFFICE);
        PRINTER.accept(req, overdue ? "overdue" : "not overdue");
    }

    private void handleRenew(LibraryReqCmd req) {
        User user = libraryManager.getUser(req.getStudentId());
        if (user.renewBook(req.getBookId(), req.getDate())) {
            PRINTER.accept(req);
        } else {
            PRINTER.reject(req);
        }
    }

    private void handleQuery(LibraryReqCmd req) {
        LibraryBookId bookId = req.getBookId();
        List<LibraryTrace> traces = traceRecorder.getTraces(bookId);
        PRINTER.info(req.getDate(), bookId, traces);
    }

    private boolean canBorrow(User user, LibraryBookIsbn isbn) {
        if (!bookshelves.hasAvailableBook(isbn)) {
            return false;
        }
        if (isbn.isTypeA() || !user.canBorrowOrOrder()) {
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
        if (isbn.isTypeA() || !user.canBorrowOrOrder()) {
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
        return user.canRead(isbn);
    }
}
