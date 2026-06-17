import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryBookState;
import com.oocourse.library1.LibraryMoveInfo;
import static com.oocourse.library1.LibraryIO.PRINTER;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Deque;

// 处理开馆和闭关整理
public class ArrangeHandler {
    private final Bookshelf bookshelf;
    private final AppointmentOffice appointmentOffice;
    private final BorrowAndReturnOffice borrowAndReturnOffice;
    private final TraceRecorder traceRecorder;

    public ArrangeHandler(Bookshelf bookshelf,
                          AppointmentOffice appointmentOffice,
                          BorrowAndReturnOffice borrowAndReturnOffice,
                          TraceRecorder traceRecorder) {
        this.bookshelf = bookshelf;
        this.appointmentOffice = appointmentOffice;
        this.borrowAndReturnOffice = borrowAndReturnOffice;
        this.traceRecorder = traceRecorder;
    }

    public void handleOpen(LocalDate date) {
        List<LibraryMoveInfo> infos = new ArrayList<>();
        // 预约处还到书架
        infos.addAll(moveExpiredAppointmentToShelf(date, false));
        // 借还处符合条件的给到预约处
        infos.addAll(moveQualifiedBorrowedToAppointment(date));
        // 书架符合条件的给到预约处
        infos.addAll(moveQualifiedShelfToAppointment(date));
        // 借还处剩下的给到书架
        infos.addAll(moveRemainingBorrowedToShelf(date));
        PRINTER.move(date, infos);
    }

    public void handleClose(LocalDate date) {
        List<LibraryMoveInfo> infos = new ArrayList<>();
        // 预约处还到书架
        infos.addAll(moveExpiredAppointmentToShelf(date, true));
        PRINTER.move(date, infos);
    }

    private List<LibraryMoveInfo> moveRemainingBorrowedToShelf(LocalDate date) {
        List<LibraryMoveInfo> infos = new ArrayList<>();
        List<Book> rest = borrowAndReturnOffice.getBooks();
        Iterator<Book> iterator = rest.iterator();
        while (iterator.hasNext()) {
            Book book = iterator.next();
            bookshelf.addBook(book);
            book.setState(LibraryBookState.BOOKSHELF);
            book.setReservedFor(null);
            iterator.remove();
            traceRecorder.record(book, date,
                    LibraryBookState.BORROW_RETURN_OFFICE, LibraryBookState.BOOKSHELF);
            infos.add(new LibraryMoveInfo(book.getBookId(),
                    LibraryBookState.BORROW_RETURN_OFFICE, LibraryBookState.BOOKSHELF));
        }
        return infos;
    }

    private List<LibraryMoveInfo> moveQualifiedBorrowedToAppointment(LocalDate date) {
        List<LibraryMoveInfo> infos = new ArrayList<>();
        List<Book> returned = borrowAndReturnOffice.getBooks();
        Map<LibraryBookIsbn, Deque<User>> waitingOrders = appointmentOffice.getWaitingOrders();
        Iterator<Book> iterator = returned.iterator();
        while (iterator.hasNext()) {
            Book book = iterator.next();
            LibraryBookIsbn isbn = book.getBookIsbn();
            Deque<User> waitingUsers = waitingOrders.get(isbn);
            if (waitingUsers == null || waitingUsers.isEmpty()) {
                continue;
            }
            book.setState(LibraryBookState.APPOINTMENT_OFFICE);
            book.setReservedFor(waitingUsers.peekFirst());
            User user = waitingUsers.peekFirst();
            appointmentOffice.reserveBookFor(book, user, date);
            iterator.remove();
            traceRecorder.record(book, date,
                    LibraryBookState.BORROW_RETURN_OFFICE,
                    LibraryBookState.APPOINTMENT_OFFICE);
            infos.add(new LibraryMoveInfo(book.getBookId(),
                    LibraryBookState.BORROW_RETURN_OFFICE,
                    LibraryBookState.APPOINTMENT_OFFICE,
                    user.getUserId()));
        }
        return infos;
    }

    private List<LibraryMoveInfo> moveQualifiedShelfToAppointment(LocalDate date) {
        List<LibraryMoveInfo> infos = new ArrayList<>();
        Map<LibraryBookIsbn, List<Book>> booksInShelf = bookshelf.getBooksInShelf();
        Map<LibraryBookIsbn, Deque<User>> waitingOrders = appointmentOffice.getWaitingOrders();
        for (Map.Entry<LibraryBookIsbn, Deque<User>> entry : waitingOrders.entrySet()) {
            LibraryBookIsbn isbn = entry.getKey();
            Deque<User> waitingUsers = entry.getValue();
            List<Book> books = booksInShelf.getOrDefault(isbn, new ArrayList<>());
            while (!books.isEmpty() && !waitingUsers.isEmpty()) {
                Book tmp = books.get(0);
                books.remove(tmp);
                tmp.setState(LibraryBookState.APPOINTMENT_OFFICE);
                tmp.setReservedFor(waitingUsers.peekFirst());
                User user = waitingUsers.peekFirst();
                appointmentOffice.reserveBookFor(tmp, user, date);
                traceRecorder.record(tmp, date,
                        LibraryBookState.BOOKSHELF, LibraryBookState.APPOINTMENT_OFFICE);
                infos.add(new LibraryMoveInfo(tmp.getBookId(),
                        LibraryBookState.BOOKSHELF,
                        LibraryBookState.APPOINTMENT_OFFICE,
                        user.getUserId()));
            }
        }
        return infos;
    }

    private List<LibraryMoveInfo> moveExpiredAppointmentToShelf(LocalDate date,
                                                                boolean includeToday) {
        List<LibraryMoveInfo> infos = new ArrayList<>();
        // 把超期定在晚上
        List<Book> expired = appointmentOffice.getExpiredBooks(date, includeToday);
        for (Book book : expired) {
            User user = book.getReservedFor();
            bookshelf.addBook(book);
            if (user != null) {
                appointmentOffice.removeReservedBook(user, book.getBookIsbn());
            }
            book.setState(LibraryBookState.BOOKSHELF);
            book.setReservedFor(null);
            if (user != null) {
                user.removeOrder(book.getBookIsbn());
            }
            traceRecorder.record(book, date,
                    LibraryBookState.APPOINTMENT_OFFICE, LibraryBookState.BOOKSHELF);
            infos.add(new LibraryMoveInfo(book.getBookId(),
                    LibraryBookState.APPOINTMENT_OFFICE, LibraryBookState.BOOKSHELF));
        }
        return infos;
    }

}
