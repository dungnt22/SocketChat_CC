import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class userList_v3 extends JPanel implements UserStatusListener {
    private JPanel title;
    private JLabel userListLabel;
    private JList listOfUser;
    public JPanel ListOfUserPanel;

    private ChatClient client;

    private DefaultListModel model;

    public userList_v3(ChatClient client) {
        this.client = client;
        this.client.addListener(this);
        model = new DefaultListModel();
        listOfUser.setModel(model);

        listOfUser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    String login = (String) listOfUser.getSelectedValue();
                    messageWindow screenChat = new messageWindow(client, login);

                    JFrame f = new JFrame("Chat with: " + login);
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f.setSize(400, 450);
                    f.setContentPane(screenChat.messageWindowPanel);
                    f.setVisible(true);
                }
            }
        });
    }


    @Override
    public void online(String login) {
        model.addElement(login);
    }

    @Override
    public void offline(String login) {
        model.removeElement(login);
    }
}
