import java.math.BigInteger;

public class RSA_Decripta{
    public String decrypt(BigInteger e, BigInteger n, BigInteger cript) throws Exception{
        String msg;
        BigInteger decr;
        decr = cript.modPow(e, n);
        msg = new String(decr.toByteArray());
        return msg;
    }
    public BigInteger decrypt_bi(BigInteger e, BigInteger n, BigInteger cript) throws Exception{
        BigInteger decr;
        decr = cript.modPow(e, n);
        return decr;
    }
}
