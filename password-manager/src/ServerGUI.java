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
            String password = this.getText();
            try {
                startServer(password,registering);
            } catch (IOException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                     IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | RuntimeException|
                     InvalidKeySpecException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public void startServer(String password,boolean registering) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        Manager manager = null;
        try {
            manager = new Manager(password,registering);
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | IOException |
                 InvalidKeySpecException e) {
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("SERVER RUNNING");
        authenticatedPage();
        PasswordServer server=new PasswordServer(manager);
        server.start(port);
    }



}
