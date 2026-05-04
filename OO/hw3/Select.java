import java.math.BigInteger;

public class Select implements Factor {
    private Factor leftCond;
    private Factor rightCond;
    private Factor trueBranch;
    private Factor falseBranch;
    private int sign = 1;

    public Select(Factor leftCond, Factor rightCond, Factor trueBranch, Factor falseBranch,
                  int sign) {
        this.leftCond = leftCond;
        this.rightCond = rightCond;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
        this.sign = sign;
    }

    @Override
    public Polynomial toPoly() {
        Polynomial polyA = leftCond.toPoly();
        Polynomial polyB = rightCond.toPoly();
        Polynomial tmp = new Polynomial();

        if (polyA.equals(polyB)) {
            tmp = trueBranch.toPoly();
        } else {
            tmp = falseBranch.toPoly();
        }

        Polynomial emptyPoly = new Polynomial();
        emptyPoly.addTerm(new Mono(
                BigInteger.ZERO, BigInteger.ZERO, new Polynomial()),
                BigInteger.valueOf(sign));
        return tmp.multi(emptyPoly);
    }

    @Override
    public void setSign(int value) {
        this.sign *= value;
    }

    @Override
    public void setExp(BigInteger g) {

    }
}
