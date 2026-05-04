// 词法分析器
public class Lexer {
    private final String input;
    private int pos = 0;
    private String curToken;

    public Lexer(String input) {
        this.input = removeWhiteSpace(input);
        next();
    }

    private String removeWhiteSpace(String input) {
        return input.replaceAll("\\s+", "");
    }

    private String getNumber() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            pos++;
        }
        return sb.toString();
    }

    public void next() {
        if (pos == input.length()) {
            curToken = "";
            return;
        }

        char c = input.charAt(pos);

        if (Character.isDigit(c)) {
            curToken = getNumber();
        }
        else if (c == '+' || c == '-' || c == '*' || c == '^' || c == '(' || c == ')' || c == 'x') {
            curToken = String.valueOf(c);
            pos++;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public String peek() {
        return this.curToken;
    }

    public void match(String target) {
        if (target.equals(curToken)) {
            next();
        } else {
            throw new IllegalArgumentException("Expected " + target + " but found " + curToken);
        }
    }
}
