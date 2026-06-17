import java.time.LocalDate;

import com.oocourse.library3.annotation.SendMessage;

public class Reservation {
    private User user;
    private Book book;
    private final LocalDate expireDate;

    public Reservation(User user, Book book, LocalDate expireDate) {
        this.user = user;
        this.book = book;
        this.expireDate = expireDate;
    }

    @SendMessage(from = "AppointmentOffice", to = "Reservation")
    public Book getBook() {
        return book;
    }

    public LocalDate getExpireDate() {
        return expireDate;
    }
}
