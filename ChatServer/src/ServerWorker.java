import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class ServerWorker extends Thread {
    private Server server;
    private Socket clientSocket;
    private String login = null;
    private OutputStream outputStream;
    private HashSet<String> topicSet = new HashSet<>();

    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    public String getLogin() {
        return login;
    }


    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] token = StringUtils.split(line);
            if (token.length > 0) {
                String cmd = token[0];
                //System.out.println(cmd);
//                if ("quit".equalsIgnoreCase(cmd)) {
//                    handleLogOff();
//                    break;
//                }
                if ("login".equalsIgnoreCase(cmd)) {
                    System.out.println("successful");
                    handleLogin(outputStream, token);
                } else if ("message".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = StringUtils.split(line, null, 3);
                    handleMessage(tokensMsg);
//                } else if ("join".equalsIgnoreCase(cmd)) {
//                    handleJoin(token);
//                } else if ("leave".equalsIgnoreCase(cmd)) {
//                    handleLeave(token);
                } else {
                    String msg = "unknown " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }
        clientSocket.close();
    }

    private void handleLeave(String[] token) {
        if (token.length > 1) {
            String topic = token[1];
            topicSet.remove(topic);
        }
    }

    private boolean isMemberOfTopic(String topic) {
        return topicSet.contains(topic);
    }

    private void handleJoin(String[] token) {
        if (token.length > 1) {
            String topic = token[1];
            topicSet.add(topic);
        }
    }

    private void handleMessage(String[] tokensMsg) throws IOException {
        String sendTo = tokensMsg[1];
        String body = tokensMsg[2];

        boolean isTopic = sendTo.charAt(0) == '#';

        List<ServerWorker> workers = server.getWorkerList();
        for (ServerWorker worker : workers) {
            if (isTopic) {
                if (worker.isMemberOfTopic(sendTo)) {
                    String msg = "message " + sendTo + ": " + login + " " + body + "\n";
                    worker.send(msg);
                }
            } else {
                if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                    System.out.println(login);
                    String msg = "message " + login + ": " + body + "\n";
                    worker.send(msg);
                }
            }
        }
    }

    private void handleLogOff() throws IOException {
        server.removeWorker(this);
        List<ServerWorker> workers = server.getWorkerList();
        String msg = "offline " + login + "\n";
        for (ServerWorker worker : workers) {
            if (!login.equals(worker.getLogin())) {
                worker.send(msg);
            }
        }
        clientSocket.close();
    }

    private void handleLogin(OutputStream outputStream, String[] token) throws IOException {
        if (token.length == 3) {
            String login = token[1];
            String password = token[2];
            String msg;
            if (login.equals("guest") && password.equals("guest") || login.equals("dung") && password.equals("dung")
                || login.equals("thu") && password.equals("thu")) {
                msg = "ok login\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User logged in successfully: " + login);

                List<ServerWorker> workers = server.getWorkerList();

                // send current user all other online logins
                for (ServerWorker worker : workers) {
                    if (worker.getLogin() != null) {
                        if (!login.equals(worker.getLogin())) {
                            String message = "online " + worker.getLogin() + "\n";
                            send(message);
                        }
                    }
                }

                // send other online users current user's status;
                String onlMsg = "online " + login + "\n";
                for (ServerWorker worker : workers) {
                    if (!login.equals(worker.getLogin())) {
                        worker.send(onlMsg);
                    }
                }
            } else {
                msg = "error login\n";
                outputStream.write(msg.getBytes());
                System.err.println("Login failed for " + login);
            }
        }
    }

    private void send(String onlMsg) throws IOException {
        if (login != null) {
            outputStream.write(onlMsg.getBytes());
        }
    }
}

//https://www.youtube.com/watch?v=aFj1ma8x8g0&list=PLdmXYkPMWIgCocLY-B4SvpQshQWC7Nc0C&index=11