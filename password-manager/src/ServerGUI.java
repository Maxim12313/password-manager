import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class ServerGUI {

    public static void main(String[] args) throws IOException {
        new ServerGUI();
    }

    JFrame myJFrame = new JFrame("Password Manager Server");
    JPanel myPanel = new JPanel();
    int port = 50501;
    String ip = "127.0.0.1";

    ServerGUI() throws IOException {
        myPanel.setLayout(new BoxLayout(myPanel,BoxLayout.Y_AXIS));
        startPage();
        myJFrame.add(myPanel);

        myJFrame.setSize(300, 200);
        myJFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myJFrame.setLocationRelativeTo(null);
        myJFrame.setVisible(true);
    }

    public void startPage(){
        myPanel.removeAll();

        myPanel.add(new JLabel("Register"));
        myPanel.add(new AuthenticationText(true));

        myPanel.add(new JLabel("Login"));
        myPanel.add(new AuthenticationText(false));
        myPanel.revalidate();

    }

    public void authenticatedPage() throws IOException {

        myPanel.removeAll();

//        JFrame frame = new JFrame("SERVER RUNNING");
//        JPanel panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
//        frame.add(panel);

        Icon icon = new ImageIcon("cat.gif");
        JLabel label = new JLabel(icon);
        JLabel caption = new JLabel("Server Running");

//        panel.add(label);
//        panel.add(caption);
//        panel.revalidate();
//
//        frame.setSize(500, 500);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setLocationRelativeTo(null);
//        frame.setVisible(true);


        myPanel.add(label);
        myPanel.add(caption);
        myPanel.revalidate();
    }


    class AuthenticationText extends JTextField implements ActionListener {

        boolean registering;

        AuthenticationText(boolean registering) {
            super();
            this.addActionListener(this);
            this.registering = registering;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("pressed");
            String password = this.getText();
//            try {
//                authenticatedPage();
//            } catch (IOException ex) {
//                throw new RuntimeException(ex);
//            }

            String path = "data/control.vault";
            if (!registering && !new File(path).exists()){
                System.out.println("ERROR: NOT REGISTERED OR INCORRECT PASSWORD");
                return;
            }

            try {
                startServer(password,registering);
            } catch (IOException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                     IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException |
                     InvalidKeySpecException ex) {
                System.out.println("ERROR: INCORRECT PASSWORD");
            }
        }
    }

    public void startServer(String password,boolean registering) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, IOException, InvalidKeySpecException, InvalidKeyException {
        Manager manager = new Manager(password,registering);
        if (manager.errorMessage.length()!=0) {
            System.out.println(manager.errorMessage);
            manager.errorMessage = "";
            return;
        }
        authenticatedPage();
        PasswordServer server=new PasswordServer(manager,port);
    }



}
