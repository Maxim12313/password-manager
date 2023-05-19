import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class Manager {
    private MyKey masterKey;
    private String hashingKeyString;
    private final Base64.Encoder encoder = Base64.getEncoder();
    private final Base64.Decoder decoder = Base64.getDecoder();
    private byte[] domainHashingSalt;
    private final String root = "data/";


    //register
    Manager(String password,boolean registering) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, InvalidKeySpecException {
        if (registering) register(password);
        else login(password);
    }

    private void register(String password) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        String secret = MyKeyGenerator.generateSecretKey();
        byte[] keySalt = MyKeyGenerator.generateSalt();

        hashingKeyString = password+secret;
        masterKey = new MyKey(MyKeyGenerator.generateMasterKey(hashingKeyString,keySalt));

        File directory = new File(root);
        directory.mkdir();


        String path = root+"control.vault";
        File myObj = new File(path);

        BufferedWriter myWriter = new BufferedWriter(new FileWriter(path));
        myWriter.write(secret); //26 characters

        String readableSalt = encoder.encodeToString(keySalt); //converts to base64 bytes
        myWriter.write(readableSalt); //24 characters

        domainHashingSalt = MyKeyGenerator.generateSalt();
        String readableDomainSalt = encoder.encodeToString(domainHashingSalt);
        myWriter.write(readableDomainSalt); //24 characters

        String mac = masterKey.computeControlMAC(secret,readableSalt,readableDomainSalt); //is this more vulnerable? Does this matter?
        myWriter.write(mac); //44 characters

        System.out.println("secret: "+secret+"    salt: "+readableSalt+"    domainSalt: "+readableDomainSalt+"       MAC: "+mac);
        myWriter.close();


        String name = root+"/entries";
        File file = new File(name);
        file.mkdir();
    }

    private void login(String password) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        String path = root+"control.vault";
        BufferedReader scanner = new BufferedReader(new FileReader(path));

        String all = scanner.readLine();

        int secretLength = 26;
        int saltLength = 24;
        int domainSaltLength = 24;
        int MACLength = 44;

        if (all.length()!=secretLength+saltLength+MACLength+domainSaltLength){
            System.out.println("tampered");
        }

        String secret = all.substring(0,secretLength);
        int a = secretLength+saltLength;
        String readableSalt = all.substring(secretLength,a);
        int b = a+domainSaltLength;
        String readableDomainSalt = all.substring(a,b);
        String discoveredMac = all.substring(b);


        byte[] salt = decoder.decode(readableSalt.getBytes()); //returns normal bytes
        domainHashingSalt = decoder.decode(readableDomainSalt.getBytes());

        hashingKeyString = password+secret;
        masterKey = new MyKey(MyKeyGenerator.generateMasterKey(hashingKeyString,salt));

        String computedMac = masterKey.computeControlMAC(secret,readableSalt,readableDomainSalt);
        checkMAC(discoveredMac,computedMac);


        System.out.println("secret: "+secret+"    salt: "+readableSalt+"    computedMAC: "+computedMac+"    domainSalt: "+readableDomainSalt);
    }

    private boolean checkMAC(String discoveredMac,String computedMac){;
        if (discoveredMac.equals(computedMac)){
            System.out.println("correct mac");
            return true;
        }
        else{
            System.out.println("wrong mac");
            return false;
        }
    }

    private IvParameterSpec generateIV(){
        int length = 16;
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return new IvParameterSpec(bytes);
    }


    public String readEntry(String domain) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, InvalidKeySpecException {
        String domainHash = encoder.encodeToString(MyKeyGenerator.generateHash(domain+hashingKeyString,domainHashingSalt));
        domainHash = filter(domainHash);
        String path = root+"/entries/"+domainHash;
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String[] data = reader.readLine().split(" ");
        IvParameterSpec iv = new IvParameterSpec(decoder.decode(data[0]));
        String username = masterKey.decrypt(data[1],iv);
        String password = masterKey.decrypt(data[2],iv);
        String discoveredMAC = data[3];
        String computedMAC = masterKey.computeEntryMAC(domain,username,password);

        System.out.println("domain: "+domain+"    username: "+username+"     password: "+password);
        if (discoveredMAC.equals(computedMAC)){
            return username.length()+" "+password.length()+" "+username+password;
        }
        else{
            return "ERROR: TAMPERED FILE";
        }
    }

    private String filter(String in){
        return in.replace('/','h');
    }
    public void createNewEntry(String domain, String username, String password) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, InvalidKeySpecException {
        IvParameterSpec iv = generateIV();
        String encryptedPassword = masterKey.encrypt(password,iv);
        String encryptedUsername = masterKey.encrypt(username,iv);

        String domainHash = encoder.encodeToString(MyKeyGenerator.generateHash(domain+hashingKeyString,domainHashingSalt)); //should I use a different number of hashing iterations. More frequent process
        domainHash = filter(domainHash);

        System.out.println("Domain write: "+domainHash);

        String mac = masterKey.computeEntryMAC(domain,username,password);

        String path = root+"entries/"+domainHash;
        System.out.println(path);

        File file = new File(path);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(encoder.encodeToString(iv.getIV()));
        writer.write(" ");
        writer.write(encryptedUsername);
        writer.write(" ");
        writer.write(encryptedPassword);
        writer.write(" ");
        writer.write(mac);
        writer.close();

        System.out.println("domain: "+domain+"    username: "+username+"     password: "+password);
        System.out.println("domain: "+domainHash+"    username: "+encryptedUsername+"    password: "+encryptedPassword);



    }


    private void test() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String message = "hello there my friends";
        System.out.println(message);
        IvParameterSpec iv = generateIV();

        System.out.println(encoder.encodeToString(iv.getIV()));
        String encrypted = masterKey.encrypt(message,iv);
        String decrypted = masterKey.decrypt(encrypted,iv);

        System.out.println(encrypted);
        System.out.println(decrypted);
    }

}
