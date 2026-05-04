import java.util.Scanner;

public class Input {

    public static String inputHandler(Scanner scanner) {
        int n = 0;
        String ntoString = scanner.nextLine();
        n = Integer.parseInt(ntoString);

        String funcString;
        FunctionF f = FunctionF.getInstance();
        if (n == 1) {
            funcString = scanner.nextLine().replaceAll("\\s+", "");
            int pos = funcString.indexOf("=");
            funcString = funcString.substring(pos + 1);
            f.recordF(new Parser(new Lexer(funcString)).parseExpression().toPoly());
        }

        String exprString = scanner.nextLine().replaceAll("\\s+", "");
        return exprString;
    }
}
