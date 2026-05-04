import java.math.BigInteger;

public class Function implements Factor {
    private Factor factor;   // 括号里的参数
    private int sign = 1;
    private BigInteger exp = BigInteger.ONE;

    public Function(Factor factor, int sign, BigInteger exp) {
        this.sign = sign;
        this.factor = factor;
        this.exp = exp;
    }

    @Override
    public Polynomial toPoly() {
        Polynomial result = new Polynomial();
        Polynomial factorPoly = factor.toPoly();
        FunctionF f = FunctionF.getInstance();
        Polynomial tmp = f.getfPoly().substitute(factorPoly);     // 这里不能用result，要用那个fx的表达式
        tmp = tmp.pow(exp);

        result = tmp.multi(new Polynomial(new Mono(BigInteger.ZERO, new Polynomial()),
                BigInteger.valueOf(sign)));
        return result;
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
