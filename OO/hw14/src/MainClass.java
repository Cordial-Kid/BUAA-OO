import com.oocourse.library2.LibraryBookIsbn;
import com.oocourse.library2.LibraryCommand;

import static com.oocourse.library2.LibraryIO.SCANNER;

import java.util.Map;

public class MainClass {
    public static void main(String[] args) {
        Map<LibraryBookIsbn, Integer> bookList = SCANNER.getInventory();  // 获取图书馆内所有书籍ISBN号及相应副本数
        LibraryManager libraryManager = new LibraryManager(bookList);
        while (true) {
            LibraryCommand command = SCANNER.nextCommand();
            if (command == null) {
                break;
            }
            libraryManager.handleCommand(command);
        }
    }

}
