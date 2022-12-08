import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Node {
    private Peer peer;

    int peerID;
    String peerIP;
    private static final String BUYER = "buyer";
    private static final String SELLER = "seller";
    private static final String BuyerAndSeller = "buyerAndSeller";

    private BlockingQueue<Message> messageQueue;

    public Node(int peerID, String peerType, String peerIP, List<Integer> neighborPeerIDs, Map<Integer,String> peerIPMap, String item, List<Integer> leaderList) throws InterruptedException {
        this.peerID = peerID;
        this.peerIP = peerIP;
        this.messageQueue = new LinkedBlockingQueue<>();

        generatePeer(peerID, peerType, peerIP, neighborPeerIDs, peerIPMap, item, leaderList);
        new RMIServerThread(peerIP).start();
        new checkQueueMessagesThread().start();
    }

    private void generatePeer(int peerID, String peerType, String peerIP, List<Integer> neighborPeerIDs, Map<Integer, String> peerIPMap, String item, List<Integer> leaderList) throws InterruptedException {
        switch (peerType) {
            case BUYER:
                peer = new Buyer(peerID, peerType, peerIP, neighborPeerIDs, peerIPMap, item, leaderList); // is peerType needed here?
                break;
            case SELLER:
                peer = new Seller(peerID, peerType, peerIP, neighborPeerIDs, peerIPMap, item, leaderList);
                break;
            case BuyerAndSeller:
                peer = new BuyerAndSeller(peerID, peerType, peerIP, neighborPeerIDs, peerIPMap, item);
                break;
            default:
                peer = new Leader(peerID, peerType, peerIP, neighborPeerIDs, peerIPMap, leaderList);
                break;
        }
    }
    
    public class checkQueueMessagesThread extends Thread {
        public void run(){
            while(true){
                checkMessageQueue();
            }
        }
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
            return peer.sendStatus();
        }
    }

    private void checkMessageQueue() {
        try{
            if(messageQueue.size() >= 1) {
                peer.processMessage(Objects.requireNonNull(messageQueue.poll()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
