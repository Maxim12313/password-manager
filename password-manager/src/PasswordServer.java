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
    private Manager manager;

    public PasswordServer(Manager manager) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        this.manager = manager;
    }

    public void start(int port) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        serverSocket = new ServerSocket(port);
        while (true){
            new PasswordServerHandler(serverSocket.accept()).start();
        }
    }

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

                while (true) { //what should signal different calls also available is innacurate
                    int read = in.read();
                    if (read==-1){
                        continue;
                    }
                    char protocol = (char)read;
                    System.out.println("protocol: "+protocol);

                    if (protocol=='c'){
                        CreateEntryHandler handler = new CreateEntryHandler(in,out,manager);
                        handler.serverReadWrite();
                    }
                    else if (protocol=='r'){
                        ReadEntryHandler handler = new ReadEntryHandler(in,out,manager);
                        handler.serverReadWrite();
                    }
                    else if (protocol=='l'){
                        DomainListHandler handler = new DomainListHandler(in,out,manager);
                        handler.serverReadWrite();
                    }
                    else if (protocol=='s'){
                        out.write("CONNECTION CLOSED".getBytes());
                        break;
                    }
                    else{
                        System.out.println("UNRECOGNIZED");
                    }
                }
                System.out.println("closed");
                in.close();
                out.close();
                clientSocket.close();

            } catch (IOException IllegalBlockSizeException){
                System.out.println("ILLEGAL BLOCK SIZE EXCEPTION");
            } catch (InvalidAlgorithmParameterException e) {
                throw new RuntimeException(e);
            } catch (NoSuchPaddingException e) {
                throw new RuntimeException(e);
            } catch (IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (BadPaddingException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeySpecException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        }
    }
}