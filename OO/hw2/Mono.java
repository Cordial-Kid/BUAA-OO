import java.math.BigInteger;
import java.util.Objects;

public class Mono {
    private BigInteger xexp;
    private Polynomial eexp;

    public Mono(BigInteger xexp, Polynomial eexp) {
        this.xexp = xexp;
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
        return mono.xexp.equals(this.xexp) && this.eexp.equals(mono.eexp);
    }

    // 传入的多个属性混合计算，返回一个 int 数字
    @Override
    public int hashCode() {
        return Objects.hash(xexp, eexp);
    }

    public Polynomial substitute(Polynomial subPoly) {
        Polynomial xexpModule = subPoly.pow(xexp);

        Polynomial eexpModule = eexp.substitute(subPoly);

        return xexpModule.multi(new Polynomial(new Mono(BigInteger.ZERO, eexpModule),
                BigInteger.ONE));
    }

    public BigInteger getXexp() {
        return xexp;
    }

    public Polynomial geteExp() {
        return eexp;
    }

}
