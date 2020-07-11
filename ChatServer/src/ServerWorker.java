import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private String user = null;
    private OutputStream outputStream;
    private HashSet<String> channelSet = new HashSet<>();

    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();


        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");
            if (tokens != null && tokens.length > 0) {
                String command = tokens[0];
                if ("quit".equalsIgnoreCase(command) || "logout".equalsIgnoreCase(command)) {
                    handleLogout();
                    break;
                } else if ("login".equalsIgnoreCase(command)) {
                    handleLogin(outputStream, tokens);
                } else if ("message".equalsIgnoreCase(command)) {
                    handleMessage(tokens);
                } else if ("join".equalsIgnoreCase(command)) {
                    handleJoin(tokens);
                } else if ("leave".equalsIgnoreCase(command)) {
                    handleLeave(tokens);
                } else {
                    String message = "unknown " + command + "\n";
                    outputStream.write(message.getBytes());
                }
            }
        }
        clientSocket.close();
    }

    public String getUser() {
        return user;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String username = tokens[1];
            String password = tokens[2];

            if ((username.equals("guest") && password.equals("guest")) || username.equals("jim") && password.equals("123")) {
                String message = "Welcome!\n";
                outputStream.write(message.getBytes());
                this.user = username;
                System.out.println("user logged in successfully: " + user);
                List<ServerWorker> workers = server.getWorkersList();

                // send current user ALL other online loggins
                for (ServerWorker worker : workers) {
                    if (worker.getUser() != null && !user.equals(worker.getUser())) {
                        String onlineUsersMessage = worker.getUser() + " is online\n";
                        worker.send(onlineUsersMessage);
                        send(onlineUsersMessage);
                    }
                }

                // send other online users CURRENT user's status
                String onlineMessage = "online " + user + "\n";
                for (ServerWorker worker : workers) {
                    if (!user.equals(worker.getUser())) {
                        worker.send(onlineMessage);
                    }
                }
            } else {
                String message = "Login attempt failed. Incorrect username or password\n";
                outputStream.write(message.getBytes());
                System.err.println("Login failed for " + username);
            }
        }
    }

    private void handleLogout() throws IOException {
        server.removeWorker(this);
        List<ServerWorker> workers = server.getWorkersList();

        String offlineMessage = "offline " + user + "\n";
        for (ServerWorker worker : workers) {
            if (!user.equals(worker.getUser())) {
                worker.send(offlineMessage);
            }
        }
        clientSocket.close();
    }

    // format: <command> <user> <message body>: message user messageBody
    // format: <command> <channel> <message body> message #channel messageBody
    private void handleMessage(String[] tokens) throws IOException {
        String receiver = tokens[1];

        //TODO make separate classes and channel object
        boolean isChannel = receiver.charAt(0) == '#';

        //TODO change input array to the multiple objects. User, Message...ect.
        String[] messageArray = Arrays.copyOfRange(tokens, 2, tokens.length);
        StringBuffer messageBody = new StringBuffer();
        for (int i = 0; i < messageArray.length; i++) {
            messageBody.append(messageArray[i] + " ");
        }

        List<ServerWorker> workers = server.getWorkersList();
        for (ServerWorker worker : workers) {
            if (isChannel) {
                if (worker.isMemberOfChannel(receiver)) {
                    String message = "message " + receiver + ": " + user + " " + messageBody.toString() + "\n";
                    worker.send(message);
                }
            } else {
                if (receiver.equalsIgnoreCase(worker.getUser())) {
                    String message = "message " + user + " " + messageBody.toString() + "\n";
                    worker.send(message);
                }
            }
        }
    }

    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String channel = tokens[1];
            channelSet.add(channel);
        }
    }

    public boolean isMemberOfChannel(String channel) {
        return channelSet.contains(channel);
    }

    private void handleLeave(String[] tokens) {
        if (tokens.length > 1) {
            String channel = tokens[1];
            channelSet.remove(channel);
        }
    }

    private void send(String message) throws IOException {
        if (user != null) {
            outputStream.write(message.getBytes());
        }
    }

}
