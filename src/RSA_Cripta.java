import java.math.BigInteger;
import java.util.Base64;
public class RSA_Cripta{
    public BigInteger crypt(BigInteger d, BigInteger n, String msg) throws Exception{
        BigInteger sms;
        sms = new BigInteger(Base64.getEncoder().encodeToString(msg.getBytes()).getBytes());
        sms = sms.modPow(d, n);
        return sms;
    }
    public BigInteger crypt(BigInteger d, BigInteger n, BigInteger msg) throws Exception{
        msg = msg.modPow(d, n);
        return msg;
    }
}
