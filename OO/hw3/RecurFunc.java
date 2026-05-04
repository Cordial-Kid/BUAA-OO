import java.math.BigInteger;

public class RecurFunc implements Factor {
    private int idx;
    private Factor inner;
    private int sign = 1;
    private BigInteger exp = BigInteger.ONE;

    public RecurFunc(int n, Factor inner, int sign, BigInteger exp) {
        this.idx = n;
        this.inner = inner;
        this.sign = sign;
        this.exp = exp;
    }

    private Polynomial recurCalc() {
        RecurFuncF f = RecurFuncF.getInstance();
        Polynomial f0 = f.getF0();  //n-2
        Polynomial f1 = f.getF1();   //n-1
        BigInteger num1 = f.getNum1();
        BigInteger num2 = f.getNum2();
        Polynomial arg1Poly = f.getArg1().toPoly();
        Polynomial arg2Poly = f.getArg2().toPoly();
        Polynomial exprPoly = f.getExpr().toPoly();
        Polynomial tmp = new Polynomial();

        if (idx == 0) {
            return f0;
        } else if (idx == 1) {
            return f1;
        }

        for (int i = 2; i <= idx; i++) {
            Polynomial num1Poly = new Polynomial(
                    new Mono(BigInteger.ZERO, BigInteger.ZERO, new Polynomial()),
                    num1);
            Polynomial num2Poly = new Polynomial(
                    new Mono(BigInteger.ZERO, BigInteger.ZERO, new Polynomial()),
                    num2);
            Polynomial part1 = f1.substitute(arg1Poly).multi(num1Poly);
            Polynomial part2 = f0.substitute(arg2Poly).multi(num2Poly);
            tmp = part1.add(part2).add(exprPoly);
            f0 = f1;
            f1 = tmp;
        }
        return tmp;
    }

    @Override
    public Polynomial toPoly() {
        Polynomial res = new Polynomial();
        Polynomial innerPoly = inner.toPoly();
        Polynomial basicPoly = recurCalc();
        Polynomial tmp = basicPoly.substitute(innerPoly);
        tmp = tmp.pow(exp);
        res = tmp.multi(new Polynomial(
                new Mono(BigInteger.ZERO, BigInteger.ZERO, new Polynomial()),
                BigInteger.valueOf(sign)));
        return res;
    }

    @Override
    public void setSign(int i) {
        this.sign = this.sign * i;
    }

    @Override
    public void setExp(BigInteger i) {
        this.exp = i;
    }

}
