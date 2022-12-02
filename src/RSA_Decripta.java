import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class RSA_Decripta{
    public String decrypt(BigInteger e, BigInteger n, BigInteger cript) throws Exception{
        String msg;
        BigInteger decr;
        decr = cript.modPow(e, n);
        //System.out.println(decr);
        msg = new String(Base64.getDecoder().decode(decr.toByteArray()));
        return msg;
    }
    public BigInteger decrypt_bi(BigInteger e, BigInteger n, BigInteger cript) throws Exception{
        BigInteger decr;
        decr = cript.modPow(e, n);
        return decr;
    }
}
