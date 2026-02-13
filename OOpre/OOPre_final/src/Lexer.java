public class Lexer {
    //存储原始字符串
    private final String input;
    //标记当前解析到的位置
    private int pos = 0;
    //当前识别出的token
    private String curToken;

    //消除空格
    private String removeWhiteSpace(String s) {
        s.replaceAll("\\s+", "");
        return s;
    }

    //初始化Lexer
    public Lexer(String input) {
        this.input = removeWhiteSpace(input);
        next();
    }

    //判断标识符是合法字符
    private boolean isIdChar(char c) {
        return Character.isLetter(c) || Character.isDigit(c) || c == '_';
    }

    //读取完整标识符
    private String getID() {
        StringBuilder sb = new StringBuilder();   //用于拼接标识字符
        while (pos < input.length() && isIdChar(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            pos++;
        }
        return sb.toString();
    }

    //读取下一个token
    public void next() {
        if (pos == input.length()) {
            curToken = null;
            return;
        }
        //获取当前指针位置的字符
        char c = input.charAt(pos);

        //当前字符是合法的标识符
        if (isIdChar(c)) {
            curToken = getID();
        }
        //当前字符是特殊符号
        else if (c == '(' || c == ')' || c == ',') {
            pos += 1;
            curToken = String.valueOf(c);   //特殊符号单独作token，转成字符串类型
        }
        //非法字符
        else {
            throw new RuntimeException("Unexpected Character" + c);
        }
    }

    //查看当前token
    public String peek() {
        return curToken;
    }

    //匹配目标方法
    public void match(String target) {
        if (target.equals(curToken)) {
            next();
        } else {
            throw new RuntimeException("Expected token: " + target + ", but got: " + curToken);
        }
    }
}
