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
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;

public class Manager {
    private EncryptionKey encryptionKey;
    private HMACKey hmacKey;
    private final String root = "data/";
    private HashSet<String> entryDomains;
    public String errorMessage = "";


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

        entryDomains = new HashSet<>();
    }

    private void login(String password) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        String path = root+"control.vault";

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(path));
        byte[][] data = ReadWrite.readData(in,5);
        in.close();

        String secret = new String(data[0]);
        byte[] keySalt = data[1];
        IvParameterSpec IV = new IvParameterSpec(data[2]);
        byte[] test = data[3];
        byte[] discoveredMAC = data[4];

        byte[] keyData = MyKeyGenerator.generateHash(password+secret,keySalt);
        encryptionKey = new EncryptionKey(partial(keyData,0,32));
        hmacKey = new HMACKey(partial(keyData,32,32));

        data[4] = new byte[]{};
        byte[] MACData = combine(new byte[][]{"control".getBytes(),secret.getBytes(),keySalt,IV.getIV(),test});
        byte[] computeMAC = hmacKey.computeMAC(MACData);

        if (!Arrays.equals(discoveredMAC,computeMAC)){
            throw new RuntimeException("ERROR: INCCORECT MAC");
        }

        byte[] decryptedTest = encryptionKey.decrypt(test,IV);
        entryDomains = getEntryDomains();
    }

    public static int getBase64ByteLength(int byteLength){
        int bytesToBits = 8;
        int base64ToBits = 6;
        int withoutPadding = (int)Math.ceil((double)byteLength*bytesToBits/base64ToBits);
        return (int)(4*Math.ceil(withoutPadding/(double)4)); //add padding by rounding up to nearest multiple of 4
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

    private HashSet<String> getEntryDomains(){
        String path = root+"/entries";
        File directory = new File(path);

        HashSet<String> entryDomains = new HashSet<>();
        for (File file:directory.listFiles()){
            entryDomains.add(file.getName());
        }
        return entryDomains;
    }

    public void createNewEntry(byte[] domain, byte[] username, byte[] password) throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
        IvParameterSpec IV = generateIV();

        byte[] domainHash = hmacKey.computeMAC(domain);
        String fileName = filter(Base64.getEncoder().encodeToString(domainHash));
        entryDomains.add(fileName);

        byte[] encryptedDomain = encryptionKey.encrypt(domain,IV);
        byte[] encryptedUsername = encryptionKey.encrypt(username,IV);
        byte[] encryptedPassword = encryptionKey.encrypt(password,IV);

        byte[] HMACData = combine(new byte[][]{fileName.getBytes(),encryptedDomain,encryptedUsername,encryptedPassword,IV.getIV()});
        byte[] MAC = hmacKey.computeMAC(HMACData);

        byte[][] writeData = new byte[][]{encryptedDomain,encryptedUsername,encryptedPassword,IV.getIV(),MAC};

        String path = root+"/entries/"+fileName;
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(path));
        ReadWrite.writeData(out,writeData);
        out.close();

    }

    public byte[][] readEntryByDomain(byte[] domain) throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidKeySpecException {
        byte[] domainHash = hmacKey.computeMAC(domain);
        String fileName = filter(Base64.getEncoder().encodeToString(domainHash));
        return readEntryByFileName(fileName);
    }
    public byte[][] readEntryByFileName(String fileName) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, InvalidKeySpecException {

        if (!entryDomains.contains(fileName)) {
            System.out.println("unrecognized domain");
        }

        String path = root+"/entries/"+fileName;
        File file = new File(path);
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(path));
        byte[][] data = ReadWrite.readData(in,5);
        in.close();

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
        byte[] password = encryptionKey.decrypt(encryptedPassword,IV);

        return new byte[][]{domain,username,password};
    }

    private String filter(String in){
        return in.replace('/','h');
    }


    //padding = #, it is outside base64 range of characters
    public static byte[] addPadding(byte[] data,int length){
        byte[] paddedData = new byte[length];
        Arrays.fill(paddedData, (byte) '#');
        System.arraycopy(data,0,paddedData,0,data.length);
        System.out.println("paddedData: "+Arrays.toString(paddedData));
        return paddedData;
    }

    public static byte[] removePadding(byte[] data){
        byte padding = (byte)'#';

        int i = 0;
        while (i<data.length && data[i]!=padding){
            i++;
        }

        return partial(data,0,i);

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
