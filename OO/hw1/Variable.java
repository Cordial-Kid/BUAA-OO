import java.math.BigInteger;

public class Variable implements Factor {
    private int sign = 1;
    private int exp;

    public Variable(int sign, int exp) {
        this.sign = sign;
        this.exp = exp;
    }

    @Override
    public Polynomial toPoly() {
        BigInteger coefficient = sign == 1 ? BigInteger.ONE : BigInteger.valueOf(-1);
        return new Polynomial(this.exp, coefficient);
    }

    @Override
    public void setSign(int i) {
        this.sign = this.sign * i;
    }

    @Override
    public void setExp(int b) {
        this.exp = b;
    }
}
