import com.oocourse.library2.LibraryBookIsbn;
import com.oocourse.library2.LibraryBookState;
import com.oocourse.library2.LibraryMoveInfo;
import com.oocourse.library2.annotation.Trigger;

import static com.oocourse.library2.LibraryIO.PRINTER;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 处理开馆和闭关整理
public class ArrangeHandler {
    private final Bookshelves bookshelves;
    private final AppointmentOffice appointmentOffice;
    private final BorrowAndReturnOffice borrowAndReturnOffice;
    private final TraceRecorder traceRecorder;
    private final GradeManager gradeManager;
    private final ReadingRoom readingRoom;

    public ArrangeHandler(Bookshelves bookshelves,
                          AppointmentOffice appointmentOffice,
                          BorrowAndReturnOffice borrowAndReturnOffice,
                          TraceRecorder traceRecorder,
                          GradeManager gradeManager,
                          ReadingRoom readingRoom) {
        this.bookshelves = bookshelves;
        this.appointmentOffice = appointmentOffice;
        this.borrowAndReturnOffice = borrowAndReturnOffice;
        this.traceRecorder = traceRecorder;
        this.gradeManager = gradeManager;
        this.readingRoom = readingRoom;
    }

    public void handleOpen(LocalDate date) {
        List<LibraryMoveInfo> infos = new ArrayList<>();
        // 预约处还到书架
        infos.addAll(moveExpiredAppointmentToShelf(date, false));
        // 借还处符合条件的给到预约处
        infos.addAll(moveQualifiedBorrowedToAppointment(date));
        // 书架内部重新分配
        infos.addAll(moveInsideBookshelves(date));
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
        // 阅览室还到书架
        infos.addAll(moveRemainingReadToShelf(date));
        PRINTER.move(date, infos);
    }

    @Trigger(from = "BOOKSHELF", to = "TREASURED_BOOKSHELF")
    @Trigger(from = "TREASURED_BOOKSHELF", to = "BOOKSHELF")
    private List<LibraryMoveInfo> moveInsideBookshelves(LocalDate date) {
        List<LibraryMoveInfo> infos = new ArrayList<>();
        NormalBookshelf normalBookshelf = bookshelves.getNormalBookshelf();
        TreasuredBookshelf treasuredBookshelf = bookshelves.getTreasuredBookshelf();
        Map<LibraryBookIsbn, List<Book>> booksInShelfNormal = normalBookshelf.getBooksInShelf();
        Map<LibraryBookIsbn, List<Book>> booksInShelfTreasured
                = treasuredBookshelf.getBooksInShelf();
        Iterator<Map.Entry<LibraryBookIsbn, List<Book>>> iterator
                = booksInShelfNormal.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<LibraryBookIsbn, List<Book>> entry = iterator.next();
            LibraryBookIsbn isbn = entry.getKey();
            List<Book> books = entry.getValue();
            if (gradeManager.getGrade(isbn) >= 4) {
                for (Book book : books) {
                    treasuredBookshelf.addBook(book);
                    traceRecorder.record(book, date,
                            LibraryBookState.BOOKSHELF, LibraryBookState.TREASURED_BOOKSHELF);
                    infos.add(new LibraryMoveInfo(book.getBookId(),
                            LibraryBookState.BOOKSHELF, LibraryBookState.TREASURED_BOOKSHELF));
                }
                iterator.remove();

            }
        }

        Iterator<Map.Entry<LibraryBookIsbn, List<Book>>> iterator2
                = booksInShelfTreasured.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry<LibraryBookIsbn, List<Book>> entry = iterator2.next();
            LibraryBookIsbn isbn = entry.getKey();
            List<Book> books = entry.getValue();
            if (gradeManager.getGrade(isbn) < 4) {
                for (Book book : books) {
                    normalBookshelf.addBook(book);
                    traceRecorder.record(book, date,
                            LibraryBookState.TREASURED_BOOKSHELF, LibraryBookState.BOOKSHELF);
                    infos.add(new LibraryMoveInfo(book.getBookId(),
                            LibraryBookState.TREASURED_BOOKSHELF, LibraryBookState.BOOKSHELF));
                }
                iterator2.remove();
            }
        }
        return infos;
    }

    @Trigger(from = "READING_ROOM", to = "BOOKSHELF")
    @Trigger(from = "READING_ROOM", to = "TREASURED_BOOKSHELF")
    private List<LibraryMoveInfo> moveRemainingReadToShelf(LocalDate date) {
        List<LibraryMoveInfo> infos = new ArrayList<>();
        Set<Book> rest = readingRoom.getRestBook();
        if (rest == null || rest.isEmpty()) {
            return infos;
        }
        Iterator<Book> iterator = rest.iterator();
        while (iterator.hasNext()) {
            Book book = iterator.next();
            addBook(book);
            User user = book.getReader();
            user.removeReadingBook(book);
            book.setReader(null);
            iterator.remove();
            traceRecorder.record(book, date,
                    LibraryBookState.READING_ROOM, book.getState());
            infos.add(new LibraryMoveInfo(book.getBookId(),
                    LibraryBookState.READING_ROOM, book.getState()));
        }
        return infos;
    }

    @Trigger(from = "BORROW_RETURN_OFFICE", to = "BOOKSHELF")
    @Trigger(from = "BORROW_RETURN_OFFICE", to = "TREASURED_BOOKSHELF")
    private List<LibraryMoveInfo> moveRemainingBorrowedToShelf(LocalDate date) {
        List<LibraryMoveInfo> infos = new ArrayList<>();
        List<Book> rest = borrowAndReturnOffice.getBooks();
        Iterator<Book> iterator = rest.iterator();
        while (iterator.hasNext()) {
            Book book = iterator.next();
            addBook(book);
            book.setReservedFor(null);
            iterator.remove();
            traceRecorder.record(book, date,
                    LibraryBookState.BORROW_RETURN_OFFICE, book.getState());
            infos.add(new LibraryMoveInfo(book.getBookId(),
                    LibraryBookState.BORROW_RETURN_OFFICE, book.getState()));
        }
        return infos;
    }

    @Trigger(from = "BORROW_RETURN_OFFICE", to = "APPOINTMENT_OFFICE")
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

    @Trigger(from = "BOOKSHELF", to = "APPOINTMENT_OFFICE")
    @Trigger(from = "TREASURED_BOOKSHELF", to = "APPOINTMENT_OFFICE")
    private List<LibraryMoveInfo> moveQualifiedShelfToAppointment(LocalDate date) {
        List<LibraryMoveInfo> infos = new ArrayList<>();
        NormalBookshelf normalBookshelf = bookshelves.getNormalBookshelf();
        TreasuredBookshelf treasuredBookshelf = bookshelves.getTreasuredBookshelf();
        Map<LibraryBookIsbn, List<Book>> normalBooksInShelf = normalBookshelf.getBooksInShelf();
        Map<LibraryBookIsbn, List<Book>> treasuredBooksInShelf =
                treasuredBookshelf.getBooksInShelf();
        Map<LibraryBookIsbn, Deque<User>> waitingOrders = appointmentOffice.getWaitingOrders();
        for (Map.Entry<LibraryBookIsbn, Deque<User>> entry : waitingOrders.entrySet()) {
            LibraryBookIsbn isbn = entry.getKey();
            Deque<User> waitingUsers = entry.getValue();
            List<Book> booksA = normalBooksInShelf.getOrDefault(isbn, new ArrayList<>());
            List<Book> booksB = treasuredBooksInShelf.getOrDefault(isbn, new ArrayList<>());
            while (!(booksA.isEmpty() && booksB.isEmpty()) && !waitingUsers.isEmpty()) {
                if (!booksA.isEmpty()) {
                    Book bookA = booksA.get(0);
                    booksA.remove(bookA);
                    bookA.setReservedFor(waitingUsers.peekFirst());
                    User user = waitingUsers.peekFirst();
                    appointmentOffice.reserveBookFor(bookA, user, date);
                    traceRecorder.record(bookA, date,
                            bookA.getState(), LibraryBookState.APPOINTMENT_OFFICE);
                    infos.add(new LibraryMoveInfo(bookA.getBookId(),
                            bookA.getState(), LibraryBookState.APPOINTMENT_OFFICE,
                            user.getUserId()));
                    bookA.setState(LibraryBookState.APPOINTMENT_OFFICE);
                    continue;
                }
                if (!booksB.isEmpty()) {
                    Book bookB = booksB.get(0);
                    booksB.remove(bookB);
                    bookB.setReservedFor(waitingUsers.peekFirst());
                    User user = waitingUsers.peekFirst();
                    appointmentOffice.reserveBookFor(bookB, user, date);
                    traceRecorder.record(bookB, date,
                            bookB.getState(), LibraryBookState.APPOINTMENT_OFFICE);
                    infos.add(new LibraryMoveInfo(bookB.getBookId(),
                            bookB.getState(), LibraryBookState.APPOINTMENT_OFFICE,
                            user.getUserId()));
                    bookB.setState(LibraryBookState.APPOINTMENT_OFFICE);
                }
            }
        }
        return infos;
    }

    @Trigger(from = "APPOINTMENT_OFFICE", to = "BOOKSHELF")
    @Trigger(from = "APPOINTMENT_OFFICE", to = "TREASURED_BOOKSHELF")
    private List<LibraryMoveInfo> moveExpiredAppointmentToShelf(LocalDate date,
                                                                boolean includeToday) {
        List<LibraryMoveInfo> infos = new ArrayList<>();
        // 把超期定在晚上
        List<Book> expired = appointmentOffice.getExpiredBooks(date, includeToday);
        for (Book book : expired) {
            User user = book.getReservedFor();
            addBook(book);
            if (user != null) {
                appointmentOffice.removeReservedBook(user, book.getBookIsbn());
            }
            book.setReservedFor(null);
            if (user != null) {
                user.removeOrder(book.getBookIsbn());
            }
            traceRecorder.record(book, date,
                    LibraryBookState.APPOINTMENT_OFFICE, book.getState());
            infos.add(new LibraryMoveInfo(book.getBookId(),
                    LibraryBookState.APPOINTMENT_OFFICE, book.getState()));
        }
        return infos;
    }

    private void addBook(Book book) {
        LibraryBookIsbn isbn = book.getBookIsbn();
        if (gradeManager.getGrade(isbn) >= 4) {
            bookshelves.addToTreasure(book);
            book.setState(LibraryBookState.TREASURED_BOOKSHELF);
        } else {
            bookshelves.addToNormal(book);
            book.setState(LibraryBookState.BOOKSHELF);
        }
    }

}
