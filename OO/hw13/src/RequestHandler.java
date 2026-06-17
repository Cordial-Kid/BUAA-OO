import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryBookState;
import com.oocourse.library1.LibraryCommand;
import com.oocourse.library1.LibraryReqCmd;
import com.oocourse.library1.LibraryTrace;

import java.time.LocalDate;
import java.util.List;

import static com.oocourse.library1.LibraryIO.PRINTER;

// 处理用户请求，借书，还书，预约，查询
public class RequestHandler {
    private final LibraryManager libraryManager;             // 关于book和user
    private final Bookshelf bookshelf;
    private final AppointmentOffice appointmentOffice;
    private final BorrowAndReturnOffice borrowAndReturnOffice;
    private final TraceRecorder traceRecorder;

    public RequestHandler(LibraryManager libraryManager,
                          Bookshelf bookshelf,
                          AppointmentOffice appointmentOffice,
                          BorrowAndReturnOffice borrowAndReturnOffice,
                          TraceRecorder traceRecorder) {
        this.libraryManager = libraryManager;
        this.bookshelf = bookshelf;
        this.appointmentOffice = appointmentOffice;
        this.borrowAndReturnOffice = borrowAndReturnOffice;
        this.traceRecorder = traceRecorder;
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
        } else {
            handleQuery(req);
        }
    }

    private void handleBorrow(LibraryReqCmd req) {
        String studentId = req.getStudentId(); // 指令对应的用户Id
        LibraryBookIsbn isbn = req.getBookIsbn(); // 指令对应的书籍ISBN号
        final LocalDate date = req.getDate(); // 指令对应的日期
        User user = libraryManager.getUser(studentId);
        if (!canBorrow(user, isbn)) {
            PRINTER.reject(req);
            return;
        }
        Book book = bookshelf.getAvailableBook(isbn);
        bookshelf.removeBook(book);
        user.borrowBook(book);   // 目前只有user知道自己拿着book， book并不知道自己被谁拿着
        book.setState(LibraryBookState.USER);
        book.setOwner(user);
        traceRecorder.record(book, date, LibraryBookState.BOOKSHELF, LibraryBookState.USER);
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
        if (!bookshelf.hasAvailableBook(isbn)) {
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
}
