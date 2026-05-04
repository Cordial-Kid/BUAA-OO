// 这是屎山，得优化一下

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Output {
    public static String optimizePoly(Polynomial polynomial) {
        HashMap<Mono, BigInteger> terms = polynomial.getTerms();
        if (terms.isEmpty()) {
            return "0";
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
            appendTerm(firstMono, tmpMap.get(firstMono), sb, true);
            tmpMap.remove(firstMono);
        }

        // 排序
        // TreeMap<Mono, BigInteger> sortedMap = new TreeMap<>((a, b) -> b.compareTo(a));
        // sortedMap.putAll(tmpMap);

        for (Map.Entry<Mono, BigInteger> e : tmpMap.entrySet()) {
            appendTerm(e.getKey(), e.getValue(), sb, false);
        }

        String result = sb.toString();
        return result.isEmpty() ? "0" : result;
    }

    private static void appendTerm(Mono mono, BigInteger coef, StringBuilder sb, Boolean isFirst) {
        if (coef.signum() > 0 && !isFirst) {
            sb.append("+");
        } else if (coef.signum() < 0) {
            sb.append("-");
        }
        BigInteger nowCoef = coef.abs();

        BigInteger xexp = mono.getXexp();
        Polynomial eexp = mono.geteExp();

        if (xexp.equals(BigInteger.ZERO) && !isEmpty(eexp)) {
            if (!nowCoef.equals(BigInteger.ONE)) {
                sb.append(nowCoef.toString()).append("*");
            }
            sb.append(optimizeExp(eexp));
        } else if (xexp.equals(BigInteger.ZERO) && isEmpty(eexp)) {
            if (!nowCoef.equals(BigInteger.ZERO)) {
                sb.append(nowCoef.toString());
            }

        } else if ((!xexp.equals(BigInteger.ZERO)) && isEmpty(eexp)) {
            if (!xexp.equals(BigInteger.ONE)) {
                if (!nowCoef.equals(BigInteger.ONE)) {
                    sb.append(nowCoef.toString()).append("*");
                }
                sb.append("x");
                sb.append("^").append(xexp);
            } else {
                if (!nowCoef.equals(BigInteger.ONE)) {
                    sb.append(nowCoef.toString()).append("*");
                }
                sb.append("x");
            }
        } else {
            if (!xexp.equals(BigInteger.ONE)) {
                if (!nowCoef.equals(BigInteger.ONE)) {
                    sb.append(nowCoef.toString()).append("*");
                }
                sb.append("x");
                sb.append("^").append(xexp);
                sb.append("*");
                sb.append(optimizeExp(eexp));
            } else {
                if (!nowCoef.equals(BigInteger.ONE)) {
                    sb.append(nowCoef.toString()).append("*");
                }
                sb.append("x*");
                sb.append(optimizeExp(eexp));
            }
        }
    }

    // 得到当前的exp的字符串形式
    private static String nowExp(Polynomial arg) {
        String inner = optimizePoly(arg);
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
    private static String optimizeExp(Polynomial eexp) {
        String best = nowExp(eexp);

        // 提公因式
        BigInteger commonFactor = getCommonFactor(eexp);
        // System.out.println("common factor: " + commonFactor);
        if (commonFactor.compareTo(BigInteger.ONE) > 0) {
            Polynomial newInner = divideByCommon(eexp, commonFactor);
            String result = nowExp(newInner) + "^" + commonFactor;
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
                        && isEmpty(entry.getKey().geteExp())
                        && !(entry.getValue().equals(BigInteger.ONE))) {
                    return false;
                } else if (!entry.getKey().getXexp().equals(BigInteger.ZERO)
                        && !isEmpty(entry.getKey().geteExp())
                        && (entry.getValue().equals(BigInteger.ONE))) {
                    return false;
                } else if (entry.getKey().getXexp().equals(BigInteger.ZERO)
                        && !isEmpty(entry.getKey().geteExp())
                        && !(entry.getValue().equals(BigInteger.ONE))) {
                    return false;
                } else if (!entry.getKey().getXexp().equals(BigInteger.ZERO)
                        && !isEmpty(entry.getKey().geteExp())
                        && !(entry.getValue().equals(BigInteger.ONE))) {
                    return false;
                }
            }
        }
        return true;
    }

}