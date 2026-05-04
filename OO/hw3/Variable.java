import java.math.BigInteger;

public class Variable implements Factor {
    private int sign = 1;
    private String name;
    private BigInteger exp = BigInteger.ONE;

    public Variable(int sign, BigInteger exp, String name) {
        this.sign = sign;
        this.exp = exp;
        this.name = name;
    }

    @Override
    public Polynomial toPoly() {
        BigInteger coefficient = sign == 1 ? BigInteger.ONE : BigInteger.valueOf(-1);
        if (name.equals("x")) {
            return new Polynomial(new Mono(exp, BigInteger.ZERO, new Polynomial()), coefficient);
        } else {
            return new Polynomial(new Mono(BigInteger.ZERO, exp, new Polynomial()), coefficient);
        }
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
