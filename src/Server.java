import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

public class Server {

    protected final Map<Integer, String> peerIDIPMap;

    Server(Map<Integer, String> peerIDIPMap) {
        this.peerIDIPMap = peerIDIPMap;
    }

    public void processMessage(Message m) throws MalformedURLException {
        switch(m.getMessageType()) {
            case Constants.BUY:
                processBuy(m);
            case Constants.SELL:
                processSell(m);
        }

    }

    private void processBuy(Message m) throws MalformedURLException {
        m.setMessageType(Constants.SERVER_ACK);

        System.out.println("req item" + m.getRequestedItem());

        int itemCount = 5; // access file and get item count

        if(itemCount > 0) {
            m.setAvailable(true);
        } else {
            m.setAvailable(false);
        }
        sendMessage(leaderId, m);
    }

    private void processSell(Message m) throws MalformedURLException {
        m.setMessageType(Constants.SERVER_ACK);
        
        System.out.println("Sending stock ack to trader");
        sendMessage(leaderId, m);
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
}
