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
        // System.out.println(firstSign+"factor "+lexer.peek());
        if (lexer.peek().equals("(")) {
            lexer.match("(");
            Factor anotherExpr = parseExpression();
            lexer.match(")");  // 已经到下一位了
            BigInteger exponent = parseExponent();
            anotherExpr.setExp(exponent);
            anotherExpr.setSign(firstSign);
            return anotherExpr;
        } else if (lexer.peek().equals("x")) {
            lexer.match("x");
            BigInteger exponent = parseExponent();
            return new Variable(firstSign, exponent);
        } else if (lexer.peek().equals("exp")) {
            lexer.match("exp");
            lexer.match("(");
            Factor innerExp = parseFactor();
            lexer.match(")");
            BigInteger exponent = parseExponent();
            return new Exp(innerExp, exponent, firstSign);
        } else if (lexer.peek().equals("f")) {
            lexer.match("f");
            lexer.match("(");
            Factor innerF = parseFactor();
            lexer.match(")");
            BigInteger exponent = parseExponent();
            return new Function(innerF, firstSign, exponent);
        } else if (lexer.peek().equals("[")) {
            lexer.match("[");
            lexer.match("(");
            final Factor innerA = parseFactor();
            lexer.match("=");
            lexer.match("=");
            final Factor innerB = parseFactor();
            lexer.match(")");
            lexer.match("?");
            final Factor innerC = parseFactor();
            lexer.match(":");
            final Factor innerD = parseFactor();
            lexer.match("]");
            BigInteger exponent = parseExponent();
            return new Select(innerA, innerB, innerC, innerD, firstSign);
        } else {
            // System.out.println("Unexpected token: " + lexer.peek());
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

    private BigInteger parseExponent() {
        BigInteger rowExp = BigInteger.ONE;
        if (lexer.peek().equals("^")) {
            lexer.match("^");
            if (lexer.peek().equals("+")) {
                lexer.match("+");
            }
            rowExp = new BigInteger(lexer.peek());
            lexer.next();
        }
        return rowExp;
    }
}

//0
//        1
//f{0}(x)=-x^+0
//f{1}(x)=3
//f{n}(x)=6*f{n-1}(x^2)+5*f{n-2}(x)+exp(x)
//--f{2}([(3==3) ? f{0}(5):1])