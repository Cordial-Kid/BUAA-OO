import java.math.BigInteger;

public class Variable implements Factor {
    private int sign = 1;
    private BigInteger exp = BigInteger.ONE;

    public Variable(int sign, BigInteger exp) {
        this.sign = sign;
        this.exp = exp;
    }

    @Override
    public Polynomial toPoly() {
        BigInteger coefficient = sign == 1 ? BigInteger.ONE : BigInteger.valueOf(-1);
        return new Polynomial(new Mono(exp, new Polynomial()), coefficient);
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
