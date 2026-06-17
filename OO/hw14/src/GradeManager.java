import com.oocourse.library2.LibraryBookIsbn;

import java.util.HashMap;
import java.util.Map;

public class GradeManager {
    private final Map<LibraryBookIsbn, Integer> totalScoreMap;
    private final Map<LibraryBookIsbn, Integer> markerMap;

    public GradeManager() {
        totalScoreMap = new HashMap<>();
        markerMap = new HashMap<>();
    }

    public void changeGrade(LibraryBookIsbn isbn, int grade) {
        totalScoreMap.put(isbn, totalScoreMap.getOrDefault(isbn, 0) + grade);
        markerMap.put(isbn, markerMap.getOrDefault(isbn, 0) + 1);
    }

    public int getGrade(LibraryBookIsbn isbn) {
        int cnt = markerMap.getOrDefault(isbn,0);
        if (cnt == 0) {
            return 0;
        }
        return totalScoreMap.getOrDefault(isbn, 0) / cnt;
    }
}
