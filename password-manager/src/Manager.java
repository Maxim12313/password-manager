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
import java.util.*;

public class Manager{
    private EncryptionKey encryptionKey;
    private HMACKey hmacKey;
    private final String root = "data/";
    public LinkedList<String> registeredDomains = new LinkedList<>();


    //register
    Manager(String password,boolean registering) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, InvalidKeySpecException {
        if (registering) register(password);
        else login(password);
    }

    private void register(String password) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        File directory = new File(root);
        directory.mkdir();
        String path = root+"control.vault";
        File myObj = new File(path);

        String secret = MyKeyGenerator.generateRandomCharacterSequence();
        byte[] keySalt = MyKeyGenerator.generateSalt();

        byte[] keyData = MyKeyGenerator.generateHash(password+secret,keySalt);
        encryptionKey = new EncryptionKey(partial(keyData,0,32));
        hmacKey = new HMACKey(partial(keyData,32,32));

        IvParameterSpec IV = generateIV();
        byte[] test = encryptionKey.encrypt(MyKeyGenerator.generateRandomCharacterSequence().getBytes(),IV);

        byte[] MACData = combine(new byte[][]{"control".getBytes(),secret.getBytes(),keySalt,IV.getIV(),test});
        byte[] MAC = hmacKey.computeMAC(MACData);

        byte[][] writeData = new byte[][]{secret.getBytes(),keySalt,IV.getIV(),test,MAC};

        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(path));
        ReadWrite.writeData(out,writeData);
        out.close();


        String name = root+"/entries";
        File file = new File(name);
        file.mkdir();
    }

    private void login(String password) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        String path = root+"control.vault";

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(path));
        File file = new File(path);
        if (!file.exists()){
            throw new RuntimeException("ERROR: NOT REGISTERED");
        }

        byte[][] data = ReadWrite.readData(in,5,(int)file.length());
        in.close();

        String secret = new String(data[0]);
        byte[] keySalt = data[1];
        IvParameterSpec IV = new IvParameterSpec(data[2]);
        byte[] test = data[3];
        byte[] discoveredMAC = data[4];

        byte[] keyData = MyKeyGenerator.generateHash(password+secret,keySalt);
        encryptionKey = new EncryptionKey(partial(keyData,0,32));
        hmacKey = new HMACKey(partial(keyData,32,32));

        byte[] MACData = combine(new byte[][]{"control".getBytes(),secret.getBytes(),keySalt,IV.getIV(),test});
        byte[] computeMAC = hmacKey.computeMAC(MACData);

        if (!Arrays.equals(discoveredMAC,computeMAC)){
            throw new RuntimeException("ERROR: INCORRECT MAC");
        }

        try {
            byte[] decryptedTest = encryptionKey.decrypt(test,IV);
        } catch (NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException("ERROR: INCORRECT PASSWORD");
        }
        compileDomainSet();
    }

    public static byte[] partial(byte[] source,int start,int length){
        byte[] partial = new byte[length];
        System.arraycopy(source,start,partial,0,length);
        return partial;
    }

    private IvParameterSpec generateIV(){
        int length = 16;
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return new IvParameterSpec(bytes);
    }

    public void createNewEntry(byte[] domain, byte[] username, byte[] password) throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
        IvParameterSpec IV = generateIV();

        byte[] domainHash = hmacKey.computeMAC(domain);
        String fileName = filter(Base64.getEncoder().encodeToString(domainHash))+".vault";

        int length = 256-32;
        byte[] paddedPassword = new byte[length];

        System.arraycopy(password,0,paddedPassword,0,password.length);
        System.out.println("padded: "+Arrays.toString(paddedPassword));
        System.out.println("non: "+Arrays.toString(password));

        byte[] encryptedDomain = encryptionKey.encrypt(domain,IV);
        byte[] encryptedUsername = encryptionKey.encrypt(username,IV);
        byte[] encryptedPassword = encryptionKey.encrypt(paddedPassword,IV);
//        byte[] encryptedPassword = encryptionKey.encrypt(password,IV);

        System.out.println("encrypted: "+Arrays.toString(encryptedPassword));
        System.out.println("length: "+encryptedPassword.length);

        byte[] HMACData = combine(new byte[][]{fileName.getBytes(),encryptedDomain,encryptedUsername,encryptedPassword,IV.getIV()});
        byte[] MAC = hmacKey.computeMAC(HMACData);

        byte[][] writeData = new byte[][]{encryptedDomain,encryptedUsername,encryptedPassword,IV.getIV(),MAC}; //mac includes .vault file name extension

        String path = root+"/entries/"+fileName;
        File file = new File(path);
        System.out.println(file.exists());
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(path));
        ReadWrite.writeData(out,writeData);
        out.close();
        registeredDomains.add(new String(domain));
    }


    public void compileDomainSet() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, IOException, InvalidKeySpecException, InvalidKeyException {
        File folder = new File(root+"/entries");
        File[] files = folder.listFiles();
        for (File file:files){
            byte[][] data = readEntryByFileName(file.getName());
            String domain = new String(data[0]);
            registeredDomains.add(domain);
        }
    }

    public byte[][] readEntryByDomain(byte[] domain) throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidKeySpecException {
        byte[] domainHash = hmacKey.computeMAC(domain);
        String fileName = filter(Base64.getEncoder().encodeToString(domainHash))+".vault";
        return readEntryByFileName(fileName);
    }
    public byte[][] readEntryByFileName(String fileName) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, InvalidKeySpecException {

        String path = root+"/entries/"+fileName;
        File file = new File(path);
        if (!file.exists()){
            throw new RuntimeException("ERROR: UNRECOGNIZED DOMAIN");
        }

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(path));
        byte[][] data = ReadWrite.readData(in,5,(int)file.length());
        in.close();

        System.out.println("REACHED");
        byte[] encryptedDomain = data[0];
        byte[] encryptedUsername = data[1];
        byte[] encryptedPassword = data[2];
        IvParameterSpec IV = new IvParameterSpec(data[3]);
        byte[] discoveredMAC = data[4];


        byte[] HMACData = combine(new byte[][]{fileName.getBytes(),encryptedDomain,encryptedUsername,encryptedPassword,IV.getIV()});
        byte[] computedMAC = hmacKey.computeMAC(HMACData);

        if (!Arrays.equals(discoveredMAC,computedMAC)) {
            System.out.println("BAD MAC");
        }

        byte[] domain = encryptionKey.decrypt(encryptedDomain,IV);
        byte[] username = encryptionKey.decrypt(encryptedUsername,IV);
//        byte[] password = encryptionKey.decrypt(encryptedPassword,IV);
        byte[] paddedPassword = encryptionKey.decrypt(encryptedPassword,IV);

        System.out.println("REACHED");
        System.out.println(Arrays.toString(paddedPassword));

        int i=0;
        while (paddedPassword[i]!=(byte)0){
            i++;
        }

        System.out.println(i);
        byte[] password = new byte[i];
        System.arraycopy(paddedPassword,0,password,0,i);

        System.out.println("padded: "+Arrays.toString(paddedPassword));
        System.out.println("non: "+Arrays.toString(password));

        return new byte[][]{domain,username,password};
    }

    private String filter(String in){
        return in.replace('/','h');
    }

    public static byte[] combine(byte[][] inputs){
        int length = 0;
        for (byte[] input:inputs){
            length+=input.length;
        }

        byte[] all = new byte[length];

        int start = 0;
        for (byte[] input:inputs){
            System.arraycopy(input,0,all,start,input.length);
            start+=input.length;
        }
        return all;
    }
}
