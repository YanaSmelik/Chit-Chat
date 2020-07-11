import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server  extends Thread{


    private final int serverPort;

    private ArrayList<ServerWorker> workrsList = new ArrayList<>();

    public Server( int serverPort) {
        this.serverPort = serverPort;
    }

    public List<ServerWorker> getWorkersList(){
        return workrsList;
    }

    public void removeWorker(ServerWorker serverWorker){
        workrsList.remove(serverWorker);
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ServerWorker serverWorker = new ServerWorker(this, clientSocket);
                workrsList.add(serverWorker);
                serverWorker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
