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
                processBuy(m);
                break;
            case Constants.ACK: // change
                processAck(m);
                break;
            case Constants.LEADER_BC:
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

    abstract void processBuy(Message m) throws MalformedURLException;
    abstract void processAck(Message m);
    abstract void receiveLeaderUpdate(Message m);

    // public void sendLeaderElectionMsg(Message message, int nodeID) throws MalformedURLException, InterruptedException {

}
