// 计算器

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Polynomial {
    private HashMap<Integer, BigInteger> terms;

    public Polynomial(Integer e, BigInteger c) {
        if ((BigInteger.ZERO).equals(c)) {
            this.terms = new HashMap<>();
        } else {
            this.terms = new HashMap<>();
            this.terms.put(e, c);
        }
    }

    public Polynomial() {
        this.terms = new HashMap<>();
    }

    // 新加入多项式
    // hashMap put 新值会覆盖旧值
    public void addTerm(Integer exponent, BigInteger coefficient) {
        if ((BigInteger.ZERO).equals(coefficient)) {
            return;
        }
        BigInteger oldCoefficient = terms.getOrDefault(exponent, BigInteger.ZERO);
        BigInteger newCoefficient = oldCoefficient.add(coefficient);
        if ((BigInteger.ZERO).equals(newCoefficient)) {
            terms.remove(exponent);
        } else {
            terms.put(exponent, newCoefficient);
        }
    }

    // A + B
    public Polynomial add(Polynomial b) {
        Polynomial tmp = new Polynomial();
        tmp.terms.putAll(this.terms);
        for (HashMap.Entry<Integer, BigInteger> e : b.terms.entrySet()) {
            tmp.addTerm(e.getKey(), e.getValue());
        }
        return tmp;
    }

    // A * B
    public Polynomial multi(Polynomial b) {
        Polynomial tmp = new Polynomial();
        for (HashMap.Entry<Integer, BigInteger> e : this.terms.entrySet()) {
            for (HashMap.Entry<Integer, BigInteger> entry : b.terms.entrySet()) {
                int newExponent = e.getKey() + entry.getKey();
                BigInteger newCoefficient = e.getValue().multiply(entry.getValue());
                tmp.addTerm(newExponent, newCoefficient);
            }
        }
        return tmp;
    }

    // A ^ Num
    public Polynomial pow(int exp) {
        Polynomial tmp = new Polynomial();
        if (exp == 0) {
            tmp.addTerm(0, BigInteger.ONE);
        } else {
            tmp.terms.putAll(this.terms);
            for (int i = 1; i < exp; i++) {
                tmp = tmp.multi(this);
            }
        }
        return tmp;
    }

    @Override
    public String toString() {
        if (terms.isEmpty()) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        Map<Integer, BigInteger> tmpMap = new HashMap<>(terms);
        int firstExponent = -1;

        for (int exp : tmpMap.keySet()) {
            if (tmpMap.get(exp).signum() > 0) {
                firstExponent = exp;
                break;
            }
        }

        if (firstExponent != -1) {
            appendTerm(firstExponent, tmpMap.get(firstExponent), sb, true);
            tmpMap.remove(firstExponent);
        }

        // 排序
        TreeMap<Integer, BigInteger> sortedMap = new TreeMap<>((a, b) -> b.compareTo(a));
        sortedMap.putAll(tmpMap);

        for (Map.Entry<Integer, BigInteger> e : sortedMap.entrySet()) {
            appendTerm(e.getKey(), e.getValue(), sb, false);
        }

        String result = sb.toString();
        return result.isEmpty() ? "0" : result;
    }

    //加项构建String
    private void appendTerm(int exp, BigInteger coef, StringBuilder sb, Boolean isFirst) {
        if (coef.signum() > 0 && !isFirst) {
            sb.append("+");
        } else if (coef.signum() < 0) {
            sb.append("-");
        }
        BigInteger nowCoef = coef.abs();

        if (exp == 0) {
            if (!nowCoef.equals(BigInteger.ZERO)) {
                sb.append(nowCoef.toString());
            }
        } else {
            if (!nowCoef.equals(BigInteger.ONE)) {
                sb.append(nowCoef.toString()).append("*");
            }
            sb.append("x");
            if (exp != 1) {
                sb.append("^").append(exp);
            }
        }

    }
}
