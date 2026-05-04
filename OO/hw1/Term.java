import java.math.BigInteger;
import java.util.ArrayList;

public class Term {
    private int sign = 1;
    private ArrayList<Factor> factors;

    public Term() {
        this.factors = new ArrayList<>();
    }

    public void addFactor(Factor factor) {
        this.factors.add(factor);
    }

    public Polynomial toPoly() {
        Polynomial tmp = new Polynomial(0, BigInteger.ONE);
        if (factors.isEmpty()) {
            return new Polynomial(1, BigInteger.ZERO);   // ？？？
        } else {
            for (Factor factor : factors) {
                tmp = tmp.multi(factor.toPoly());
            }
        }
        if (sign == -1) {
            tmp = tmp.multi(new Polynomial(0, BigInteger.valueOf(-1)));
        }
        return tmp;
    }

    public void setSign(int i) {
        this.sign = this.sign * i;
    }
}
