import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HMACKey {
   Mac macKey;
    HMACKey(byte[] keyData) throws NoSuchAlgorithmException, InvalidKeyException {
        String algorithm = "HmacSHA256";
        SecretKeySpec key = new SecretKeySpec(keyData,algorithm);
        macKey = Mac.getInstance(algorithm);
        macKey.init(key);
    }

    public byte[] computeMAC(byte[] input) throws NoSuchAlgorithmException, InvalidKeyException { //returns base64 data
        //should I be reinitializing the mac value each time?
        return macKey.doFinal(input);
    }
}
