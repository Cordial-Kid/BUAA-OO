import java.math.BigInteger;

public class Exp implements Factor {
    private Factor factor;
    private BigInteger exp = BigInteger.ONE;
    private int sign = 1;

    public Exp(Factor factor, BigInteger exp, int sign) {
        this.factor = factor;
        this.exp = exp;
        this.sign = sign;
    }

    @Override
    public Polynomial toPoly() {
        Polynomial expPoly = new Polynomial(
                new Mono(BigInteger.ZERO, BigInteger.ZERO, new Polynomial()),
                exp);
        Polynomial ans = factor.toPoly().multi(expPoly);
        Polynomial result = new Polynomial();
        Mono mono = new Mono(BigInteger.ZERO, BigInteger.ZERO, ans);
        result.addTerm(mono, BigInteger.valueOf(sign));
        return result;
    }

    @Override
    public void setSign(int i) {
        this.sign = this.sign * i;
    }

    @Override
    public void setExp(BigInteger b) {
        this.exp = b;
    }

}
