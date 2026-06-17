import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookState;
import com.oocourse.library1.LibraryTrace;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 记录每本书的历史移动轨迹
public class TraceRecorder {
    private final Map<LibraryBookId, List<LibraryTrace>> traces;

    public TraceRecorder() {
        this.traces = new HashMap<>();
    }

    public void record(Book book, LocalDate date,
                       LibraryBookState from, LibraryBookState to) {
        LibraryBookId bookId = book.getBookId();
        traces.computeIfAbsent(bookId, key -> new ArrayList<>())
                .add(new LibraryTrace(date, from, to));
    }

    public List<LibraryTrace> getTraces(LibraryBookId bookId) {
        return traces.getOrDefault(bookId, new ArrayList<>());
    }
}
