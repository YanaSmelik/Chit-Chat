import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ChatClient {

    private final String server;
    private final int serverPort;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferIn;

    //register multiple user listeners
    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();

    public ChatClient(String server, int serverPort) {
        this.server = server;
        this.serverPort = serverPort;

    }

    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost", 8818);

        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String user) {
                System.out.println("ONLINE: " + user);
            }

            @Override
            public void offline(String user) {
                System.out.println("OFFLINE: " + user);
            }
        });

        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String sender, String messageBody) {
                System.out.println("You got a message from " + sender + ": " + messageBody);
            }
        });

        if (!client.connect()) {
            System.err.println("Connect failed");
        } else {
            System.out.println("Connect successful");
            if (client.login("guest", "guest")) {
                System.out.println("Login successful");

                client.message("jim", "Hello world");
            } else {
                System.err.println("Login failed");
            }

            //client.logout();
        }
    }

    public void message(String receiver, String messageBody) throws IOException {
        String command = "message " + receiver + " " + messageBody + "\n";
        serverOut.write(command.getBytes());
    }

    public void logout() throws IOException {
        String command = "logout\n";
        serverOut.write(command.getBytes());
    }

    public boolean connect() throws IOException {
        socket = new Socket(server, serverPort);
        System.out.println("Client port is " + socket.getLocalPort());
        serverOut = socket.getOutputStream();
        serverIn = socket.getInputStream();
        bufferIn = new BufferedReader(new InputStreamReader(serverIn));
        return true;
    }

    public boolean login(String user, String password) throws IOException {
        String command = "login " + user + " " + password + "\n";
        serverOut.write(command.getBytes());
        String serverResponse = bufferIn.readLine();
        System.out.println("Server response: " + serverResponse);

        if ("Welcome!".equalsIgnoreCase(serverResponse)) {
            startMessageReader();
            return true;
        } else {
            return false;
        }
    }

    private void startMessageReader() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    readMessageLoop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    private void readMessageLoop() throws IOException {
        try {
            String line;
            while ((line = bufferIn.readLine()) != null) {
                String[] tokens = line.split(" ");
                if (tokens != null && tokens.length > 0) {
                    String command = tokens[0];
                    if ("online".equalsIgnoreCase(command)) {
                        handleOnline(tokens);
                    } else if ("offline".equalsIgnoreCase(command)) {
                        handleOffline(tokens);
                    } else if ("message".equalsIgnoreCase(command)) {
                        handleMessage(tokens);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            socket.close();
        }

    }

    private void handleMessage(String[] tokens) {
        String sender = tokens[1];
        String[] messageArray = Arrays.copyOfRange(tokens, 2, tokens.length);

        StringBuffer messageBody = new StringBuffer();
        for (int i = 0; i < messageArray.length; i++) {
            messageBody.append(messageArray[i] + " ");
        }

        for (MessageListener listener : messageListeners) {
            listener.onMessage(sender, messageBody.toString());
        }
    }

    private void handleOnline(String[] tokens) {
        String user = tokens[1];
        for (UserStatusListener listener : userStatusListeners) {
            listener.online(user);
        }
    }

    private void handleOffline(String[] tokens) {
        String user = tokens[1];
        for (UserStatusListener listener : userStatusListeners) {
            listener.offline(user);
        }
    }

    public void addUserStatusListener(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }

    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }
}
