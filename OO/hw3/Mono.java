import java.math.BigInteger;
import java.util.Objects;

public class Mono {
    private BigInteger xexp;
    private BigInteger yexp;
    private Polynomial eexp;

    public Mono(BigInteger xexp, BigInteger yexp, Polynomial eexp) {
        this.xexp = xexp;
        this.yexp = yexp;
        this.eexp = eexp;
    }

    // 重写Polynomial的getOrDefault,需要一起更新equals和hashCode方法,方便查找
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Mono mono = (Mono) o;
        return mono.xexp.equals(this.xexp)
                && mono.yexp.equals(this.yexp)
                && this.eexp.equals(mono.eexp);
    }

    // 传入的多个属性混合计算，返回一个 int 数字
    @Override
    public int hashCode() {
        return Objects.hash(xexp, yexp, eexp);
    }

    public Polynomial substitute(Polynomial subPoly) {
        Polynomial xexpModule = subPoly.pow(xexp);

        Polynomial eexpModule = eexp.substitute(subPoly);

        return xexpModule.multi(new Polynomial(
                new Mono(BigInteger.ZERO, BigInteger.ZERO, eexpModule),
                BigInteger.ONE));
    }

    public Polynomial derive(String var) {
        Polynomial result = new Polynomial();
        if (var.equals("dx")) {
            // 对x^a 求导
            Mono tmp1 = new Mono(xexp.subtract(BigInteger.ONE), yexp, eexp);
            result.addTerm(tmp1, xexp);
            // 对exp(E)求导
            Polynomial tmp2 = eexp.derive(var);
            Polynomial tmp3 = new Polynomial(new Mono(xexp, yexp, eexp), BigInteger.ONE);
            result = result.add(tmp2.multi(tmp3));
        } else if (var.equals("dy")) {
            // y^a
            Mono tmp1 = new Mono(xexp, yexp.subtract(BigInteger.ONE), eexp);
            result.addTerm(tmp1, yexp);
            // exp(E)
            Polynomial tmp2 = eexp.derive(var);
            Polynomial tmp3 = new Polynomial(new Mono(xexp, yexp, eexp), BigInteger.ONE);
            result = result.add(tmp2.multi(tmp3));
        }
        return result;
    }

    public BigInteger getXexp() {
        return xexp;
    }

    public BigInteger getYexp() {
        return yexp;
    }

    public Polynomial geteExp() {
        return eexp;
    }

}
