import java.math.BigInteger;

public interface Factor {
    Polynomial toPoly();

    void setSign(int i);

    void setExp(BigInteger b);

}
