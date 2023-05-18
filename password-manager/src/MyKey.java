import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class MyKey {
    private final SecretKey masterKey;
    MyKey(SecretKey key){
        masterKey = key;
    }

    public String encrypt(String input, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String algorithm = "AES/CBC/PKCS5Padding";
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, masterKey, iv);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }


    //IvParameter spec changes with each usage. Keep in string to freeze
    public String decrypt(String input, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
//        IvParameterSpec iv = new IvParameterSpec(ivString.getBytes());
        String algorithm = "AES/CBC/PKCS5Padding";
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE,masterKey,iv);
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(input));
        return new String(plainText);
    }

    private String computeMAC(String input) throws NoSuchAlgorithmException, InvalidKeyException {

        String algorithm = "HmacSHA256";
        Mac mac = Mac.getInstance(algorithm);
        mac.init(masterKey);
        byte[] output = mac.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(output); //timing based equality, search up function
    }

    public String computeControlMAC(String secret, String salt,String readableDomainSalt) throws NoSuchAlgorithmException, InvalidKeyException {
        String all = secret+salt+readableDomainSalt;
        String end = computeMAC(all);
        return end;
    }

    public String computeEntryMAC(String domain, String username, String password) throws NoSuchAlgorithmException, InvalidKeyException {
        return computeMAC(domain+username+password);
    }

    public String computeDomainListMAC(String allDomains) throws NoSuchAlgorithmException, InvalidKeyException {
        return computeMAC(allDomains);
    }

}
