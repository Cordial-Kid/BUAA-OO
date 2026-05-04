// 计算器

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Polynomial {
    private HashMap<Mono, BigInteger> terms;

    public Polynomial(Mono m, BigInteger c) {
        if ((BigInteger.ZERO).equals(c)) {
            this.terms = new HashMap<>();
        } else {
            this.terms = new HashMap<>();
            this.terms.put(m, c);
        }
    }

    public Polynomial() {
        this.terms = new HashMap<>();
    }

    // 新加入多项式
    // hashMap put 新值会覆盖旧值
    public void addTerm(Mono mono, BigInteger coefficient) {
        if ((BigInteger.ZERO).equals(coefficient)) {
            return;
        }
        BigInteger oldCoefficient = terms.getOrDefault(mono, BigInteger.ZERO);
        BigInteger newCoefficient = oldCoefficient.add(coefficient);
        if ((BigInteger.ZERO).equals(newCoefficient)) {
            terms.remove(mono);
        } else {
            terms.put(mono, newCoefficient);
        }
    }

    // A + B
    public Polynomial add(Polynomial b) {
        Polynomial tmp = new Polynomial();
        tmp.terms.putAll(this.terms);
        for (HashMap.Entry<Mono, BigInteger> m : b.terms.entrySet()) {
            tmp.addTerm(m.getKey(), m.getValue());
        }
        return tmp;
    }

    // A * B
    public Polynomial multi(Polynomial b) {
        Polynomial tmp = new Polynomial();
        for (HashMap.Entry<Mono, BigInteger> m : this.terms.entrySet()) {
            for (HashMap.Entry<Mono, BigInteger> mono : b.terms.entrySet()) {
                Mono m1 = m.getKey();
                Mono m2 = mono.getKey();

                BigInteger newCoef = m.getValue().multiply(mono.getValue());

                BigInteger newXExp = m1.getXexp().add(m2.getXexp());

                BigInteger newYExp = m1.getYexp().add(m2.getYexp());

                Polynomial newEExp = m1.geteExp().add(m2.geteExp());

                tmp.addTerm(new Mono(newXExp, newYExp, newEExp), newCoef);
            }
        }
        return tmp;
    }

    // A ^ Num
    public Polynomial pow(BigInteger exp) {
        Polynomial tmp = new Polynomial();
        if (exp.equals(BigInteger.ZERO)) {
            tmp.addTerm(new Mono(
                    BigInteger.ZERO, BigInteger.ZERO, new Polynomial()),
                    BigInteger.ONE);
        } else {
            tmp.terms.putAll(this.terms);
            for (BigInteger i = BigInteger.ONE; i.compareTo(exp) < 0; i = i.add(BigInteger.ONE)) {
                tmp = tmp.multi(this);
            }
        }
        return tmp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Polynomial poly = (Polynomial) o;
        // terms的equals方法不需要自己实现，底层实现过程中会调用mono的equals方法。
        return this.terms.equals(poly.terms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(terms);
    }

    public Polynomial substitute(Polynomial subPoly) {
        Polynomial result = new Polynomial();
        for (Map.Entry<Mono, BigInteger> entry : terms.entrySet()) {
            Mono m = entry.getKey();
            BigInteger coef = entry.getValue();
            Polynomial monoPoly = m.substitute(subPoly);
            Polynomial coefPoly = new Polynomial(
                    new Mono(BigInteger.ZERO, BigInteger.ZERO, new Polynomial()),
                    coef);
            result = result.add(monoPoly.multi(coefPoly));
        }
        return result;
    }

    public Polynomial derive(String var) {
        Polynomial result = new Polynomial();
        for (Map.Entry<Mono, BigInteger> entry : terms.entrySet()) {
            Polynomial monoDerived = entry.getKey().derive(var);
            Polynomial coefPoly = new Polynomial(
                    new Mono(BigInteger.ZERO, BigInteger.ZERO, new Polynomial()),
                    entry.getValue());
            result = result.add(monoDerived.multi(coefPoly));
        }
        return result;
    }

    public HashMap<Mono, BigInteger> getTerms() {
        return terms;
    }
}
