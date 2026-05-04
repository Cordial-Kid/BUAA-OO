// 语法分析器

import java.math.BigInteger;

public class Parser {
    private Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expression parseExpression() {
        Expression expr = new Expression();

        int firstSign = parseLeadingSign();
        Term firstTerm = parseTerm();
        firstTerm.setSign(firstSign);
        // 项的正负是parseTerm的事情，Expression里面的加减是parseExpression的事情
        // System.out.println(firstSign+"exp ");
        expr.addTerm(firstTerm);
        while (lexer.peek().equals("+") || lexer.peek().equals("-")) {
            int nextSign = parseLeadingSign();
            Term nextTerm = parseTerm();
            nextTerm.setSign(nextSign);
            expr.addTerm(nextTerm);
        }
        return expr;
    }

    public Term parseTerm() {
        Term term = new Term();

        int firstSign = parseLeadingSign();
        term.setSign(firstSign);
        // System.out.println(firstSign+"term ");
        term.addFactor(parseFactor());
        // 中间乘积是parseTerm的事情，每一项的正负是parseFactor的事情，
        while (lexer.peek().equals("*")) {
            lexer.next();
            term.addFactor(parseFactor());
        }
        return term;
    }

    public Factor parseFactor() {
        int firstSign = parseLeadingSign();
        if (lexer.peek().equals("(")) {
            lexer.match("(");
            Factor anotherExpr = parseExpression();
            lexer.match(")");  // 已经到下一位了
            int exponent = parseExponent();
            anotherExpr.setExp(exponent);
            anotherExpr.setSign(firstSign);
            return anotherExpr;
        } else if (lexer.peek().equals("x")) {
            lexer.match("x");
            int exponent = parseExponent();
            return new Variable(firstSign, exponent);
        } else {
            BigInteger num = new BigInteger(lexer.peek());
            lexer.next();
            return new Number(num.multiply(BigInteger.valueOf(firstSign)));
        }
    }

    private int parseLeadingSign() {
        int sign = 1;
        if (lexer.peek().equals("+") || lexer.peek().equals("-")) {     // ？？？
            if (lexer.peek().equals("-")) {
                sign *= -1;
            }
            lexer.next();
        }
        return sign;
    }

    private int parseExponent() {
        int rowExp = 1;
        if (lexer.peek().equals("^")) {
            lexer.match("^");
            if (lexer.peek().equals("+")) {
                lexer.match("+");
            }
            rowExp = Integer.parseInt(lexer.peek());
            lexer.next();
        }
        return rowExp;
    }
}
