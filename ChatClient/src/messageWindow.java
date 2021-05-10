import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class messageWindow extends JPanel implements MessageListener {
    public JPanel messageWindowPanel;
    private JList screenChat;
    private JTextField inputMessage;
    private JButton btn_send;

    private ChatClient client;
    private DefaultListModel model;
    private String login;

    public messageWindow(ChatClient client, String login) {
        this.client = client;
        this.login = login;
        model = new DefaultListModel();

        client.addMessage(this);
        screenChat.setModel(model);

        inputMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String text = inputMessage.getText();
                    client.message(login, text);
                    model.addElement("You: " + text);
                    inputMessage.setText("");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        btn_send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String text = inputMessage.getText();
                    client.message(login, text);
                    model.addElement("You: " + text);
                    inputMessage.setText("");
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onMessage(String fromLogin, String body) {
        String line = fromLogin + " " + body;
        model.addElement(line);
    }
}
