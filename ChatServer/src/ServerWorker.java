import java.io.*;
import java.net.Socket;
import java.util.List;

public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private String user = null;
    private OutputStream outputStream;

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
                if ("quit".equalsIgnoreCase(command)) {
                    break;
                } else if ("login".equalsIgnoreCase(command)) {
                    handleLogin(outputStream, tokens);
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
                    if (worker.getUser() != null) {
                        if (!user.equals(worker.getUser())) {
                            String onlineUsersMessage = worker.getUser() + " is online\n";
                            //worker.send(onlineUsersMessage);
                            send(onlineUsersMessage);
                        }
                    }
                }

                // send other online users CURRENT user's status
                String onlineMessage = user + " is online\n";
                for (ServerWorker worker : workers) {
                    if (!user.equals(worker.getUser())) {
                        worker.send(onlineMessage);
                    }
                }
            } else {
                String message = "Login attempt failed. Incorrect username or password";
                outputStream.write(message.getBytes());
            }
        }
    }

    private void send(String message) throws IOException {
        if (user != null) {
            outputStream.write(message.getBytes());
        }
    }

}
