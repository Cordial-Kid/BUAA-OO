import java.util.ArrayList;
import java.math.BigInteger;

public class Expression implements Factor {
    private ArrayList<Term> terms;
    private int exp = 1;    //表达式作为底数
    private int sign = 1;                     //表达式作为第一项

    public Expression() {
        terms = new ArrayList<>();
    }

    public void addTerm(Term term) {
        this.terms.add(term);
    }

    @Override
    public Polynomial toPoly() {
        Polynomial tmp = new Polynomial(1, BigInteger.ZERO);
        for (Term term : terms) {
            tmp = tmp.add(term.toPoly());
        }
        tmp = tmp.pow(exp);       // 指数可能带正号
        if (sign == -1) {
            tmp = tmp.multi(new Polynomial(0, BigInteger.valueOf(-1)));
        }
        return tmp;
    }

    @Override
    public void setExp(int b) {
        this.exp = b;
    }

    @Override
    public void setSign(int i) {
        this.sign = this.sign * i;
    }
}
