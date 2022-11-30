import java.math.BigInteger;
public class RSA_Cripta{
    public BigInteger crypt(BigInteger d, BigInteger n, String msg) throws Exception{
        BigInteger sms;

        sms = new BigInteger(msg.getBytes());
        sms = sms.modPow(d, n);
        return sms;
    }
    public BigInteger crypt(BigInteger d, BigInteger n, BigInteger msg) throws Exception{
        msg = msg.modPow(d, n);
        return msg;
    }
}
