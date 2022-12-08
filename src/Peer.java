import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;

public abstract class Peer {
    protected final String type;
    protected final String ip;
    protected int peerID;
    protected final List<Integer> neighborPeerID;

    protected final Map<Integer, String> peerIDIPMap;

    private int counter = 0;

    public Peer(int peerID, String peerType, String ip, List<Integer> neighborPeerID, Map<Integer, String> peerIDIPMap) {
        this.type = peerType;
        this.peerID = peerID;
        this.ip = ip;
        this.neighborPeerID = neighborPeerID;
        this.peerIDIPMap = peerIDIPMap;
/*        if(peerID == 2)
            sendLeaderElectionMsg(new Message(), peerID);*/
    }

    public void processMessage(Message m) throws MalformedURLException {
        switch (m.getMessageType()) {
            case Constants.BUY:
                counter++;
                processBuy(m);
                break;
            case Constants.SELL:
                counter++;
                processSell(m);
            case Constants.SERVER_ACK:
                processServerAck(m);
                break;
            case Constants.TRADER_ACK:
                processLeaderAck(m);
                break;
            case Constants.LEADER_UPDATE:
                receiveLeaderUpdate(m);
                break;
        }
    }

    public void sendMessage(int receiverID, Message m) throws MalformedURLException {
        URL url = new URL(peerIDIPMap.get(receiverID));
        try {
            Registry registry = LocateRegistry.getRegistry(url.getHost(), url.getPort());
            RemoteInterface remoteInterface = (RemoteInterface) registry.lookup("RemoteInterface");
            remoteInterface.send(m);
        } catch (RemoteException | NotBoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public String sendStatus(int fellowLeader) throws MalformedURLException {
        System.out.println("checking status of "+fellowLeader);
        URL url = new URL(peerIDIPMap.get(fellowLeader));
        try {
            Registry registry = LocateRegistry.getRegistry(url.getHost(), url.getPort());
            RemoteInterface remoteInterface = (RemoteInterface) registry.lookup("RemoteInterface");
            return remoteInterface.leaderStatus();
        } catch (RemoteException | NotBoundException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public String sendStatus() {
        if(counter > 2 && peerID == 3) {
            System.exit(500);
            return "Not OK";
        }
        else
            return "OK";
    }

    abstract void processBuy(Message m) throws MalformedURLException;
    protected abstract void processSell(Message m) throws MalformedURLException;
    abstract void processServerAck(Message m) throws MalformedURLException;
    abstract void processLeaderAck(Message m);
    abstract void receiveLeaderUpdate(Message m);

}
