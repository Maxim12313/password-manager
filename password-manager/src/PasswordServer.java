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
    
    public void start(int port) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        serverSocket = new ServerSocket(port);
        while (true){
            new PasswordServerHandler(serverSocket.accept()).start();
        }
    }



    //'v' = view
    //'c' = create
    //'l' = login
    //'r' = register
    //'.' = end
    private class PasswordServerHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private Manager manager = null;

        PasswordServerHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;

        }

        public void run() { //run is executed on default by thread

            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("input: " + inputLine);
                    int type = inputLine.charAt(0);

                    String input = inputLine.substring(1);
                    if (type == 'r' || type == 'l') {

                        if (type == 'r') {
                            manager = new Manager(input, true);
                        }
                        if (type == 'l') {
                            manager = new Manager(input, false);
                        }
                        out.println("authenticated");
                    } else {
                        if (manager == null) {
                            out.println("not authenticated");
                            continue;
                        } else if (type == 'v') {
                            String res = manager.readEntry(input);
                            out.println(res);
                        } else if (type == 'c') {
                            String[] sizes = input.split(" ");
                            int domainLength = Integer.parseInt(sizes[0]);
                            int usernameLength = Integer.parseInt(sizes[1]);
                            int passwordLength = Integer.parseInt(sizes[2]);

                            int headingCharacters = sizes[0].length() + sizes[1].length() + sizes[2].length() + 3;
                            String info = input.substring(headingCharacters);


                            String domain = info.substring(0, domainLength);
                            String username = info.substring(domainLength, domainLength + usernameLength);
                            String password = info.substring(info.length() - passwordLength);

                            manager.createNewEntry(domain, username, password);
                            out.println("created");
                        } else { //first character is E if error
                            out.println("ERROR: NO RECOGNIZED CALL TYPE");
                        }
                    }

                    if (".".equals(inputLine)) {
                        out.println("CONNECTION CLOSED");
                        break;
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



    public static void main(String[] args) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        int port = 50501;
        PasswordServer server=new PasswordServer();
        server.start(port);
    }

}