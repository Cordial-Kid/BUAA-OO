import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = Input.inputHandler(scanner);

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Expression expression = parser.parseExpression();
        Polynomial polynomial = expression.toPoly();
        System.out.println(Output.optimizePoly(polynomial));
        scanner.close();
    }
}
