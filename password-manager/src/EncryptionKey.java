import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class EncryptionKey {
    private final SecretKey key;
    EncryptionKey(byte[] keyData){
        String cipher = "AES";
        key = new SecretKeySpec(keyData,cipher);
    }

    public byte[] encrypt(byte[] input, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String algorithm = "AES/CBC/PKCS5Padding";
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        return cipher.doFinal(input);
    }


    //IvParameter spec changes with each usage. Keep in string to freeze
    public byte[] decrypt(byte[] input, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    //IvParameterSpec iv = new IvParameterSpec(ivString.getBytes());
        String algorithm = "AES/CBC/PKCS5Padding";
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key,iv);
        return cipher.doFinal(input);
    }
}
