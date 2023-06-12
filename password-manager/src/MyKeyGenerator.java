import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class MyKeyGenerator {


    public static String generateRandomCharacterSequence(){
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

    public static String generateHash(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(input.getBytes());
        return Base64.getEncoder().encodeToString(encodedHash);
    }


    public static byte[] generatePasswordHash(String input, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        int iterations = 100_000;
        int keyLength = 512; //first half 256 encryption aes, second half 256 hmac
        KeySpec spec = new PBEKeySpec(input.toCharArray(), salt, iterations, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        return factory.generateSecret(spec).getEncoded();
    }
}
