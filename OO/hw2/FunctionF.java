public class FunctionF {
    private static final FunctionF f = new FunctionF();
    private Polynomial funcPoly = null;

    private FunctionF() {
    }

    public static FunctionF getInstance() {
        return f;
    }

    public void recordF(Polynomial polynomial) {
        funcPoly = polynomial;
    }

    public Polynomial getfPoly() {
        return funcPoly;
    }
}
