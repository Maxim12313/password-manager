import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class MyKeyGenerator {


    public static String generateSecretKey(){
        String upper = "QWERTYUIOPASDFGHJKLZXCVBNM";
        String lower = "qwertyuiopasdfghjklzxcvbnm";
        String numbers = "1234567890";
        String special = "!@#$%^&*()";
        String possibilities = upper+lower+special+numbers;

        int length = 26;
        StringBuilder secret = new StringBuilder();
        SecureRandom random = new SecureRandom(); //new randoms for new seeds each time
        for (int i=0;i<length;i++){
            int randomIndex = random.nextInt(possibilities.length()); //[0,possibilities.length-1]
            secret.append(possibilities.charAt(randomIndex));
        }
        return secret.toString();
    }

    public static byte[] generateSalt(){
        int length = 16;
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
//        System.out.println(Arrays.toString(salt));
        return bytes;
    }

    //string->bytes (magic here) ->secret key->base64 hash->AES secret key
    public static SecretKey generateMasterKey(String input, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] hash = generateHash(input,salt);
        String cipher = "AES";
        return new SecretKeySpec(hash,cipher);
    }

    public static byte[] generateHash(String input, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        int iterations = 100_000;
        int keyLength = 256;

        KeySpec spec = new PBEKeySpec(input.toCharArray(), salt, iterations, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return factory.generateSecret(spec).getEncoded();
    }
}
