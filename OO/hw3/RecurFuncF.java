import java.math.BigInteger;

public class RecurFuncF {
    private static final RecurFuncF f = new RecurFuncF();
    private Polynomial f0;
    private Polynomial f1;
    private BigInteger num1;
    private BigInteger num2;
    private Factor arg1;
    private Factor arg2;
    private Factor expr;

    private RecurFuncF() {
    }

    public static RecurFuncF getInstance() {
        return f;
    }

    public void setF0(Polynomial f0) {
        this.f0 = f0;
    }

    public void setF1(Polynomial f1) {
        this.f1 = f1;
    }

    public void setNum1(BigInteger num1) {
        this.num1 = num1;
    }

    public void setNum2(BigInteger num2) {
        this.num2 = num2;
    }

    public void setArg1(Factor arg1) {
        this.arg1 = arg1;
    }

    public void setArg2(Factor arg2) {
        this.arg2 = arg2;
    }

    public void setExpr(Factor expr) {
        this.expr = expr;
    }

    public Polynomial getF0() {
        return this.f0;
    }

    public Polynomial getF1() {
        return this.f1;
    }

    public BigInteger getNum1() {
        return this.num1;
    }

    public BigInteger getNum2() {
        return this.num2;
    }

    public Factor getArg1() {
        return arg1;
    }

    public Factor getArg2() {
        return arg2;
    }

    public Factor getExpr() {
        return expr;
    }
}
