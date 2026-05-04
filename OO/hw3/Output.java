// 这是屎山，得优化一下

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Output {

    public static String optimizePoly(Polynomial polynomial) {
        Map<Polynomial, String> memo = new HashMap<>();
        return optimizePolyInternel(polynomial, memo);
    }

    public static String optimizePolyInternel(Polynomial polynomial, Map<Polynomial, String> memo) {
        HashMap<Mono, BigInteger> terms = polynomial.getTerms();
        if (terms.isEmpty()) {
            return "0";
        }
        if (memo.containsKey(polynomial)) {
            return memo.get(polynomial);
        }

        StringBuilder sb = new StringBuilder();
        Map<Mono, BigInteger> tmpMap = new HashMap<>(terms);
        Mono firstMono = null;

        for (Mono mono : tmpMap.keySet()) {
            if (tmpMap.get(mono).signum() > 0) {
                firstMono = mono;
                break;
            }
        }

        if (firstMono != null) {
            appendTerm(firstMono, tmpMap.get(firstMono), sb, true, memo);
            tmpMap.remove(firstMono);
        }

        // 排序
        // TreeMap<Mono, BigInteger> sortedMap = new TreeMap<>((a, b) -> b.compareTo(a));
        // sortedMap.putAll(tmpMap);

        for (Map.Entry<Mono, BigInteger> e : tmpMap.entrySet()) {
            appendTerm(e.getKey(), e.getValue(), sb, false, memo);
        }

        String result = sb.toString();
        String finalResult = result.isEmpty() ? "0" : result;
        memo.put(polynomial, finalResult);
        return finalResult;
    }

    private static void appendTerm(Mono mono, BigInteger coef,
                                   StringBuilder sb,
                                   Boolean isFirst,
                                   Map<Polynomial, String> memo) {
        if (coef.signum() > 0 && !isFirst) {
            sb.append("+");
        } else if (coef.signum() < 0) {
            sb.append("-");
        }
        BigInteger nowCoef = coef.abs();

        BigInteger xexp = mono.getXexp();
        BigInteger yexp = mono.getYexp();
        Polynomial eexp = mono.geteExp();

        Boolean hasX = !xexp.equals(BigInteger.ZERO);
        Boolean hasY = !yexp.equals(BigInteger.ZERO);
        Boolean hasE = !isEmpty(eexp);

        Boolean multi = false;

        if (!hasX && !hasY && !hasE) {
            sb.append(nowCoef.toString());
            return;
        }

        if (!nowCoef.equals(BigInteger.ONE)) {
            sb.append(nowCoef.toString());
            multi = true;
        }

        if (hasX) {
            if (multi) {
                sb.append("*");
            }
            sb.append("x");
            if (!xexp.equals(BigInteger.ONE)) {
                sb.append("^").append(xexp);
            }
            multi = true;
        }

        if (hasY) {
            if (multi) {
                sb.append("*");
            }
            sb.append("y");
            if (!yexp.equals(BigInteger.ONE)) {
                sb.append("^").append(yexp);
            }
            multi = true;
        }

        if (hasE) {
            if (multi) {
                sb.append("*");
            }
            sb.append(optimizeExp(eexp, memo));
        }
    }

    // 得到当前的exp的字符串形式
    private static String nowExp(Polynomial arg, Map<Polynomial, String> memo) {
        String inner = optimizePolyInternel(arg, memo);
        if (isFactor(arg)) {
            return "exp(" + inner + ")";
        } else {
            return "exp((" + inner + "))";
        }
    }

    //提公因式
    private static BigInteger getCommonFactor(Polynomial eexp) {
        BigInteger commonFactor = BigInteger.ZERO;
        for (BigInteger i : eexp.getTerms().values()) {
            BigInteger tmp = i.abs();
            commonFactor = commonFactor.equals(BigInteger.ZERO) ? tmp : commonFactor.gcd(tmp);
        }
        return commonFactor;
    }

    // 化简提公因式后的结果
    private static Polynomial divideByCommon(Polynomial eexp, BigInteger k) {
        Polynomial tmp = new Polynomial();
        for (Map.Entry<Mono, BigInteger> entry : eexp.getTerms().entrySet()) {
            tmp.addTerm(entry.getKey(), entry.getValue().divide(k));
        }
        return tmp;
    }

    // 从一个方面优化exp输出
    private static String optimizeExp(Polynomial eexp, Map<Polynomial, String> memo) {
        String best = nowExp(eexp, memo);

        // 提公因式
        BigInteger commonFactor = getCommonFactor(eexp);
        // System.out.println("common factor: " + commonFactor);
        if (commonFactor.compareTo(BigInteger.ONE) > 0) {
            Polynomial newInner = divideByCommon(eexp, commonFactor);
            String result = nowExp(newInner, memo) + "^" + commonFactor;
            if (result.length() < best.length()) {
                best = result;
            }
        }
        return best;
    }

    private static boolean isEmpty(Polynomial eexp) {
        if (eexp == null || eexp.getTerms().isEmpty()) {
            return true;
        }
        return false;
    }

    private static boolean isFactor(Polynomial eexp) {
        if (eexp.getTerms().size() > 1) {
            return false;
        } else {
            for (Map.Entry<Mono, BigInteger> entry : eexp.getTerms().entrySet()) {
                if (!entry.getKey().getXexp().equals(BigInteger.ZERO)
                        && entry.getKey().getYexp().equals(BigInteger.ZERO)
                        && isEmpty(entry.getKey().geteExp())
                        && (entry.getValue().equals(BigInteger.ONE))) {
                    return true;
                } else if (entry.getKey().getXexp().equals(BigInteger.ZERO)
                        && !entry.getKey().getYexp().equals(BigInteger.ZERO)
                        && isEmpty(entry.getKey().geteExp())
                        && (entry.getValue().equals(BigInteger.ONE))) {
                    return true;
                } else if (entry.getKey().getXexp().equals(BigInteger.ZERO)
                        && entry.getKey().getYexp().equals(BigInteger.ZERO)
                        && !isEmpty(entry.getKey().geteExp())
                        && (entry.getValue().equals(BigInteger.ONE))) {
                    return true;
                } else if (entry.getKey().getXexp().equals(BigInteger.ZERO)
                        && entry.getKey().getYexp().equals(BigInteger.ZERO)
                        && isEmpty(entry.getKey().geteExp())) {
                    return true;
                }
            }
        }
        return false;
    }
}