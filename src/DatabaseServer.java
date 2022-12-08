import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DatabaseServer {
    private BlockingQueue<Message> messageQueue;
    private Server server;

    public DatabaseServer(String databaseIP, Map<Integer, String> peerIDIPMap) {
        this.messageQueue = new LinkedBlockingQueue<>();
        server = new Server(peerIDIPMap);
        new RMIServerThread(databaseIP).start();
        new checkQueueMessagesThread().start();

    }

    public class RMIServerThread extends Thread {
        String url;

        public RMIServerThread(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            try {
                RemoteInterfaceImpl obj = new RemoteInterfaceImpl();
                RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(obj, 0);
                System.setProperty("java.rmi.server.hostname", new URL(this.url).getHost());
                Registry registry = LocateRegistry.createRegistry(new URL(this.url).getPort());
                registry.bind("RemoteInterface", stub);
            } catch (Exception e) {
                throw new RuntimeException("Failed to start the server" + e.getMessage());
            }
            System.out.println("Running server thread");
        }
    }

    public class RemoteInterfaceImpl implements RemoteInterface {
        public RemoteInterfaceImpl() throws RemoteException {
        }

        @Override
        public void send(Message m) throws RemoteException {
            messageQueue.add(m);
        }

        @Override
        public String leaderStatus() throws RemoteException {
            return null;
        }
    }

    public class checkQueueMessagesThread extends Thread {
        public void run(){
            while(true){
                checkMessageQueue();
            }
        }
    }

    private void checkMessageQueue() {
        try{
            if(messageQueue.size() >= 1) {
                // System.out.println("Processing messages on server's side");
                server.processMessage(Objects.requireNonNull(messageQueue.poll()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
