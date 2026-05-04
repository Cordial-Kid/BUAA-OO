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

        int m = Integer.parseInt(scanner.nextLine());
        RecurFuncF fr = RecurFuncF.getInstance();
        String f0Str = "";
        String f1Str = "";
        String fnStr = "";
        if (m == 1) {
            for (int i = 0; i < 3; i++) {
                String line = scanner.nextLine().replaceAll("\\s+", "");
                if (line.startsWith("f{0}")) {
                    f0Str = line.substring(line.indexOf("=") + 1);
                } else if (line.startsWith("f{1}")) {
                    f1Str = line.substring(line.indexOf("=") + 1);
                } else {
                    fnStr = line.substring(line.indexOf("=") + 1);
                }
            }

            fr.setF0(new Parser(new Lexer(f0Str)).parseExpression().toPoly());
            fr.setF1(new Parser(new Lexer(f1Str)).parseExpression().toPoly());

            new Parser(new Lexer(fnStr)).parseRecursive();
        }
        String exprString = scanner.nextLine().replaceAll("\\s+", "");
        return exprString;
    }
}
