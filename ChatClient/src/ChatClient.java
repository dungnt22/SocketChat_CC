import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {
    private String serverName;
    private int serverPort;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferedReader;

    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();

    public ChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public void message(String sendTo, String body) throws IOException {
        String cmd = "message " + sendTo + " " + body + "\n";
        serverOut.write(cmd.getBytes());
    }

    private void logoff() {
    }

    public boolean login(String userName, String password) throws IOException {
        String cmd = "login " + userName + " " + password + "\n";
        serverOut.write(cmd.getBytes());

        String  response = bufferedReader.readLine();
        System.out.println("Response from server: " + response);

        if ("ok login".equalsIgnoreCase(response)) {
            startMessageReader();
            return true;
        } else {
            return false;
        }
    }

    private void startMessageReader() {
        Thread t = new Thread() {
            @Override
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }

    private void readMessageLoop() {
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                //System.out.println(line);
                String[] tokens = StringUtils.split(line);

                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    if ("online".equalsIgnoreCase(cmd)) {
                        handleOnline(tokens);
                    } else if ("offline".equalsIgnoreCase(cmd)) {
                        handleOffline(tokens);
                    } else if ("message".equalsIgnoreCase(cmd)) {
                        String[] tokensMsg = StringUtils.split(line, null, 3);
                        handleMessage(tokensMsg);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void handleMessage(String[] tokens) {
        String login = tokens[1];
        String body = tokens[2];
        //System.out.println(body);
        for (MessageListener messageListener : messageListeners) {
            messageListener.onMessage(login, body);
        }
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for (UserStatusListener listener : userStatusListeners) {
            listener.offline(login);
        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for (UserStatusListener listener : userStatusListeners) {
            listener.online(login);
        }
    }

    public boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort);
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedReader = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addListener(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }

    public void removeListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }

    public void addMessage(MessageListener messageListener) {
        messageListeners.add(messageListener);
    }

    public void removeMessage(MessageListener messageListener) {
        messageListeners.remove(messageListener);
    }
}
