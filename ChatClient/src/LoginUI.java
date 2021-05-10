import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LoginUI extends JFrame {
    private JTextField userName;
    private JPanel icon;
    private JLabel login;
    private JButton btn_login;
    private JTextField passwordField;
    private JPanel panel1;

    public static JFrame frame = new JFrame("Login");

    private ChatClient client;

    public LoginUI() {
        this.client = new ChatClient("localhost", 1234);
        client.connect();



        btn_login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });
   }

    private void doLogin() {
        String login_ = userName.getText();
        String password = passwordField.getText();

        try {
            if (client.login(login_, password)) {
                userList_v3 userListPane = new userList_v3(client);

                JFrame frame_ = new JFrame("User List");
                frame_.setSize(300, 450);
                frame_.getContentPane().add(userListPane.ListOfUserPanel, BorderLayout.CENTER);
                frame_.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame_.pack();

                frame_.setVisible(true);

                frame.setVisible(false);
                //System.out.println("hello");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid login/password.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        LoginUI loginUI = new LoginUI();

        frame.setContentPane(loginUI.panel1);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.pack();

        frame.setVisible(true);

    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
