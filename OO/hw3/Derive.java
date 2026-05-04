import java.math.BigInteger;

public class Derive implements Factor {
    private Factor inner;
    private String type;
    private int sign = 1;

    public Derive(Factor inner, String type, int sign) {
        this.inner = inner;
        this.type = type;
        this.sign = sign;
    }

    @Override
    public Polynomial toPoly() {
        Polynomial res = new Polynomial();
        Polynomial innerPoly = inner.toPoly();
        if (type.equals("dx")) {
            res = innerPoly.derive("dx");
        } else if (type.equals("dy")) {
            res = innerPoly.derive("dy");
        } else {
            res = innerPoly.derive("dx").add(innerPoly.derive("dy"));
        }
        res = res.multi(new Polynomial(new Mono(BigInteger.ZERO, BigInteger.ZERO, new Polynomial()),
                BigInteger.valueOf(sign)));
        return res;
    }

    @Override
    public void setSign(int i) {
        this.sign = this.sign * i;
    }

    @Override
    public void setExp(BigInteger i) {

    }
}
