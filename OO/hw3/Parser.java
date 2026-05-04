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
        final String token = lexer.peek();
        if (token.equals("(")) {
            return parseGroupedExp(firstSign);
        } else if (token.equals("x") || lexer.peek().equals("y")) {
            return parseVariable(firstSign);
        } else if (token.equals("exp")) {
            return parseExp(firstSign);
        } else if (token.equals("f")) {
            return parseFunction(firstSign);
        } else if (token.equals("[")) {
            return parseSelect(firstSign);
        } else if (token.equals("dx") || token.equals("dy") || token.equals("grad")) {
            return parseDerive(firstSign);
        } else {
            return parseNum(firstSign);
        }
    }

    private Factor parseGroupedExp(int firstSign) {
        lexer.match("(");
        Factor anotherExpr = parseExpression();
        lexer.match(")");  // 已经到下一位了
        BigInteger exponent = parseExponent();
        anotherExpr.setExp(exponent);
        anotherExpr.setSign(firstSign);
        return anotherExpr;
    }

    private Factor parseVariable(int firstSign) {
        String var = lexer.peek();
        lexer.match(var);
        BigInteger exponent = parseExponent();
        return new Variable(firstSign, exponent, var);
    }

    private Factor parseExp(int firstSign) {
        lexer.match("exp");
        lexer.match("(");
        Factor innerExp = parseFactor();
        lexer.match(")");
        BigInteger exponent = parseExponent();
        return new Exp(innerExp, exponent, firstSign);
    }

    private Factor parseFunction(int firstSign) {
        lexer.match("f");
        if (lexer.peek().equals("{")) {
            //这里是递推函数调用
            lexer.match("{");
            final int idx = Integer.parseInt(lexer.peek());
            lexer.next();
            lexer.match("}");
            lexer.match("(");
            Factor innerArg = parseFactor();
            lexer.match(")");
            BigInteger exponent = parseExponent();
            return new RecurFunc(idx, innerArg, firstSign, exponent);
        } else {
            lexer.match("(");
            Factor innerF = parseFactor();
            lexer.match(")");
            BigInteger exponent = parseExponent();
            return new Function(innerF, firstSign, exponent);
        }
    }

    private Factor parseSelect(int firstSign) {
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
    }

    private Factor parseDerive(int firstSign) {
        final String type = lexer.peek();
        lexer.next();
        lexer.match("(");
        Factor inner = parseExpression();
        lexer.match(")");
        return new Derive(inner, type, firstSign);
    }

    private Factor parseNum(int firstSign) {
        BigInteger num = new BigInteger(lexer.peek());
        lexer.next();
        return new Number(num.multiply(BigInteger.valueOf(firstSign)));
    }

    public void parseRecursive() {
        final RecurFuncF fr = RecurFuncF.getInstance();
        // 第一项
        int firstSign = recursiveParseLeading();
        final BigInteger num1 = new BigInteger(
                lexer.peek()).multiply(BigInteger.valueOf(firstSign));
        lexer.next();
        lexer.match("*");
        lexer.match("f");
        lexer.match("{");
        final String typeA = lexer.peek();
        lexer.next();
        lexer.match("}");
        lexer.match("(");
        final Factor arg1 = parseFactor();
        lexer.match(")");

        // 第二项
        int secondSign = recursiveParseLeading();
        final BigInteger num2 = new BigInteger(
                lexer.peek()).multiply(BigInteger.valueOf(secondSign));
        lexer.next();
        lexer.match("*");
        lexer.match("f");
        lexer.match("{");
        String typeB = lexer.peek();
        lexer.next();
        lexer.match("}");
        lexer.match("(");
        Factor arg2 = parseFactor();
        lexer.match(")");

        if (typeA.equals("n-1")) {
            fr.setNum1(num1);
            fr.setArg1(arg1);
            fr.setNum2(num2);
            fr.setArg2(arg2);
        } else {
            fr.setNum1(num2);
            fr.setNum2(num1);
            fr.setArg1(arg2);
            fr.setArg2(arg1);
        }

        if (!lexer.peek().equals("")) {
            fr.setExpr(parseExpression());
        } else {
            fr.setExpr(new Expression());
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

    private int recursiveParseLeading() {
        int sign = 1;
        while (lexer.peek().equals("+") || lexer.peek().equals("-")) {
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

