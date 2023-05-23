import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class PasswordServer {
    private ServerSocket serverSocket;
    private Manager manager = null;

    public PasswordServer(Manager manager, int port) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, IOException, InvalidKeySpecException, InvalidKeyException {
        this.manager = manager;
        start(port);
    }

    private void start(int port) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        serverSocket = new ServerSocket(port);
        while (true){
            new PasswordServerHandler(serverSocket.accept()).start();
        }
    }

    //'v' = view
    //'c' = create
    //'.' = end
    //'a' = authentication check
    private class PasswordServerHandler extends Thread{
        private Socket clientSocket;
        private BufferedOutputStream out;
        private BufferedInputStream in;


        PasswordServerHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;

        }

        public void run() { //run is executed on default by thread

            try {
                out = new BufferedOutputStream(clientSocket.getOutputStream());
                in = new BufferedInputStream(clientSocket.getInputStream());

                while (in.available()!=-1) { //what should signal different calls also available is innacurate
                    byte type = (byte)in.read();
                    byte r = (byte)'r';
                    byte c = (byte)'c';
                    byte s = (byte)'s';
                    if (type !=r & type!=c && type!=s){
                        out.write("ERROR: NO RECOGNIZED TYPE".getBytes());
                        continue;
                    }
                    if (type==r){
                        byte[][] data = ReadWrite.readData(in,1);
                        byte[][] send = manager.readEntryByDomain(data[0]);
                        out.write(r);
                        ReadWrite.writeData(out,send);
                    }
                    if (type==c){
                        byte[][] data = ReadWrite.readData(in,3);
                        manager.createNewEntry(data[0],data[1],data[2]);
                    }
                }
                in.close();
                out.close();
                clientSocket.close();

            } catch (IOException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                     IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException |
                     InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        }
    }



//    public static void main(String[] args) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
//        int port = 50501;
//        PasswordServer server=new PasswordServer();
//        server.start(port);
//    }

}