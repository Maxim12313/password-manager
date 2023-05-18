import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

public class GUI {

    public static void main(String[] args) {
        new GUI();
    }

    JFrame myJFrame = new JFrame("Password Manager");
    JPanel myPanel = new JPanel();
    Manager manager;

    GUI(){
        myPanel.setLayout(new BoxLayout(myPanel,BoxLayout.Y_AXIS));
        startPage();

        myJFrame.add(myPanel);

        myJFrame.setSize(500, 500);
        myJFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myJFrame.setLocationRelativeTo(null);
        myJFrame.setVisible(true);
    }

    public void startPage(){
        myPanel.removeAll();

        myPanel.add(new JLabel("Register"));
        myPanel.add(new RegisterText());

        myPanel.add(new JLabel("Login"));
        myPanel.add(new SignInText());
        myPanel.revalidate();
    }

    public void authenticatedPage(){
        myPanel.removeAll();

        myPanel.add(new JLabel("Domain"));
        myPanel.add(new EntryText());
        myPanel.add(new JLabel("Username"));
        myPanel.add(new EntryText());
        myPanel.add(new JLabel("Password"));
        myPanel.add(new EntryText());

        myPanel.add(new JLabel("Search"));
        myPanel.add(new SearchText());
        myPanel.revalidate();
    }

    class EntryText extends JTextField implements ActionListener{

        EntryText(){
            super();
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("pressed");
            String domain = ((EntryText)myPanel.getComponent(1)).getText();
            String username = ((EntryText)myPanel.getComponent(3)).getText();
            String password = ((EntryText)myPanel.getComponent(5)).getText();

            for (String field:new String[]{domain,username,password}){
                if (field.length()==0){
                    System.out.println("empty fields");
                    return;
                }
            }

            try {
                manager.createNewEntry(domain,username,password);
                manager.readEntry(domain);
            } catch (InvalidAlgorithmParameterException ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchPaddingException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalBlockSizeException ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            } catch (BadPaddingException ex) {
                throw new RuntimeException(ex);
            } catch (InvalidKeyException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (InvalidKeySpecException ex) {
                throw new RuntimeException(ex);
            }


        }
    }

    class SearchText extends JTextField implements ActionListener{

        SearchText(){
            super();
            this.addActionListener(this);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                manager.readEntry(getText());
            } catch (InvalidAlgorithmParameterException ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchPaddingException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalBlockSizeException ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            } catch (BadPaddingException ex) {
                throw new RuntimeException(ex);
            } catch (InvalidKeyException ex) {
                throw new RuntimeException(ex);
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (InvalidKeySpecException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    class RegisterText extends JTextField implements ActionListener {


        RegisterText(){
            super();
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("pressed");
            String password = this.getText();
            String secret = MyKeyGenerator.generateSecretKey();
            byte[] salt = MyKeyGenerator.generateSalt();

            try {
                manager = new Manager(password,secret,salt);
                authenticatedPage();


            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            } catch (InvalidKeySpecException ex) {
                throw new RuntimeException(ex);
            } catch (InvalidAlgorithmParameterException ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchPaddingException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalBlockSizeException ex) {
                throw new RuntimeException(ex);
            } catch (BadPaddingException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (InvalidKeyException ex) {
                throw new RuntimeException(ex);
            }

        }
    }

    class SignInText extends JTextField implements ActionListener {


        SignInText(){
            super();
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("pressed");
            String password = this.getText();
            try {
                manager = new Manager(password);
                authenticatedPage();


            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            } catch (InvalidKeySpecException ex) {
                throw new RuntimeException(ex);
            } catch (InvalidAlgorithmParameterException ex) {
                throw new RuntimeException(ex);
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchPaddingException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalBlockSizeException ex) {
                throw new RuntimeException(ex);
            } catch (BadPaddingException ex) {
                throw new RuntimeException(ex);
            } catch (InvalidKeyException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        }
    }

}
