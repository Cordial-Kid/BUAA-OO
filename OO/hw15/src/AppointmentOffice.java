import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.annotation.SendMessage;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// 预约处，保留为用户预约的书籍
public class AppointmentOffice {
    private final Map<LibraryBookIsbn, Deque<User>> waitingOrders;    // 等待预约的表
    private final Map<String, Map<LibraryBookIsbn, Reservation>> reservedBooks;    // 可以取书的表

    public AppointmentOffice() {
        this.waitingOrders = new LinkedHashMap<>();
        this.reservedBooks = new LinkedHashMap<>();
    }

    @SendMessage(from = "User", to = "AppointmentOffice")
    public void orderNewBook() {
    }

    public void addOrder(LibraryBookIsbn isbn, User user) {
        waitingOrders.computeIfAbsent(isbn, key -> new ArrayDeque<>()).addLast(user);
        user.addOrder(isbn);
    }

    public void reserveBookFor(Book book, User user, LocalDate date) {
        LibraryBookIsbn isbn = book.getBookIsbn();
        String userId = user.getUserId();
        Reservation reservation = new Reservation(user, book, date.plusDays(4));
        reservedBooks.computeIfAbsent(userId, key -> new LinkedHashMap<>()).put(isbn, reservation);
        waitingOrders.get(isbn).removeFirst();
    }

    @SendMessage(from = "User", to = "AppointmentOffice")
    public Book removeReservedBook(User user, LibraryBookIsbn isbn) {
        String userId = user.getUserId();
        Book res = null;
        Map<LibraryBookIsbn, Reservation> userReservations = reservedBooks.get(userId);
        if (userReservations == null) {
            return null;
        }
        Reservation reservation = userReservations.get(isbn);
        if (reservation == null) {
            return null;
        }
        res = reservation.getBook();
        userReservations.remove(isbn);
        if (userReservations.isEmpty()) {
            reservedBooks.remove(userId);
        }
        return res;
    }

    public boolean hasReservedBook(User user, LibraryBookIsbn isbn) {
        String userId = user.getUserId();
        Map<LibraryBookIsbn, Reservation> userReservations = reservedBooks.get(userId);
        if (userReservations == null) {
            return false;
        }
        return userReservations.containsKey(isbn);
    }

    public List<Book> getExpiredBooks(LocalDate date, boolean includeToday) {
        List<Book> expired = new ArrayList<>();
        List<Reservation> reservations = new ArrayList<>();
        for (Map<LibraryBookIsbn, Reservation> userReservations : reservedBooks.values()) {
            reservations.addAll(userReservations.values());
        }
        for (Reservation reservation : reservations) {
            Book book = reservation.getBook();
            LocalDate expireDate = reservation.getExpireDate();
            if (expireDate.isBefore(date)
                    || (includeToday && expireDate.isEqual(date))) {
                expired.add(book);
            }
        }
        return expired;
    }

    public Map<LibraryBookIsbn, Deque<User>> getWaitingOrders() {
        return waitingOrders;
    }

}
