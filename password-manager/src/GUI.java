import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class GUI {

    public static void main(String[] args) {
        new GUI();
    }

    JFrame myJFrame = new JFrame("Password Manager");
    JPanel myPanel = new JPanel();
    Manager manager;
    PasswordClient client;
    int port = 50501;
    String ip = "127.0.0.1";

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

            if (domain.length()==0 || username.length()==0 || password.length()==0) {
                System.out.println("empty fields");
                return;
            }

            try {
                String callData = "c" + domain.length() + " " + username.length() + " " +password.length() + " " + domain + username + password;
                String response = client.sendMessage(callData);
                System.out.println("create response: "+response);

            } catch (IOException ex) {
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
            String domain = this.getText();
            try {
                String callData = "v"+domain;
                String response = client.sendMessage(callData);
                System.out.println("search response: "+response);
                String[] responseData = response.split(" ");

                int usernameLength = Integer.parseInt(responseData[0]);
                int passwordLength = Integer.parseInt(responseData[1]); //I can take this out

                int headingCharacters = responseData[0].length()+responseData[1].length()+2;
                String info = response.substring(headingCharacters);

                String username = info.substring(0,usernameLength);
                String password = info.substring(usernameLength);

                System.out.println("username: "+username+"     password: "+password);

            } catch (IOException ex) {
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

            try {
                client = new PasswordClient();
                client.startConnection(ip, port);
                String registerCall = "r"+password;
                String response = client.sendMessage(registerCall);

                if (response.equals("authenticated")){
                    System.out.println("yes");
                    authenticatedPage();
                }
                else{
                    System.out.println("no");
                    System.out.println(response);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        }
    }

    class SignInText extends JTextField implements ActionListener {


        SignInText() {
            super();
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("pressed");
            String password = this.getText();
            try {
                client = new PasswordClient();
                client.startConnection(ip, port);
                String loginCall = "l" + password;
                String loginResponse = client.sendMessage(loginCall);

                if (loginResponse.equals("authenticated")) {
                    System.out.println("yes");
                    authenticatedPage();
                } else {
                    System.out.println("no");
                    System.out.println(loginResponse);
                }

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
