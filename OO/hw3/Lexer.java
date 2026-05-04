// 词法分析器
public class Lexer {
    private final String input;
    private int pos = 0;
    private String curToken;

    public Lexer(String input) {
        this.input = input;
        next();
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
        } else if (c == 'e'
                && pos + 2 < input.length()
                && input.substring(pos, pos + 3).equals("exp")) {
            curToken = "exp";
            pos += 3;
        } else if (c == 'd'
                && pos + 1 < input.length()
                && input.substring(pos, pos + 2).equals("dx")) {
            curToken = "dx";
            pos += 2;
        } else if (c == 'd'
                && pos + 1 < input.length()
                && input.substring(pos, pos + 2).equals("dy")) {
            curToken = "dy";
            pos += 2;
        } else if (c == 'g'
                && pos + 3 < input.length()
                && input.substring(pos, pos + 4).equals("grad")) {
            curToken = "grad";
            pos += 4;
        } else if (c == 'n'
                && pos + 2 < input.length()
                && input.substring(pos, pos + 3).equals("n-1")) {
            curToken = "n-1";
            pos += 3;
        } else if (c == 'n'
                && pos + 2 < input.length()
                && input.substring(pos, pos + 3).equals("n-2")) {
            curToken = "n-2";
            pos += 3;
        } else {
            curToken = String.valueOf(c);
            pos++;
        }
        //这里涉及fn的词法分析
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
