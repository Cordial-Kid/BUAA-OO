import java.math.BigInteger;

public class Number implements Factor {
    private BigInteger num;
    private int sign = 1;

    public Number(BigInteger num) {
        this.num = num;
    }

    @Override
    public Polynomial toPoly() {
        BigInteger newNum = this.num.multiply(BigInteger.valueOf(sign));
        return new Polynomial(new Mono(BigInteger.ZERO, BigInteger.ZERO, new Polynomial()), newNum);
    }

    @Override
    public void setSign(int i) {
        this.sign = this.sign * i;
    }

    @Override
    public void setExp(BigInteger b) {
        // Number类型的指数没有意义，可以不实现
    }
}
