import java.net.MalformedURLException;;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class Leader extends Peer{

    private int leaderID;
    List<Integer> neighborPeerIDs;
    // trader's local cache for item count lookup
    private Cache cache;
    protected int serverID;
    SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");

    public Leader(int peerID, String peerType, String peerIP, List<Integer> neighborPeerIDs, Map<Integer, String> peerIPMap) throws InterruptedException {
        super(peerID, peerType, peerIP, neighborPeerIDs, peerIPMap);
        this.leaderID = peerID;
        this.neighborPeerIDs = neighborPeerIDs;
        cache = new Cache();
        this.serverID = 4; //testing
        Thread.sleep(8000);
        // update serverID
        updateCache();
        new BroadcastThread().start();
    }

    // Implement cache consistency
    private void updateCache() throws InterruptedException {
        cache.put("salt", 5);
       // Thread.sleep(5000);
    }

    public class BroadcastThread extends Thread {
        public void run() {
            Message m = new Message();
            m.setMessageType(Constants.LEADER_BC);
            m.setLeaderID(leaderID);

            // TODO: send leader ID to all peers and try update to async call
            for(int nodeID : neighborPeerIDs) {
                try {
                    sendMessage(nodeID, m);
                } catch (MalformedURLException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    @Override
    void processBuy(Message m) throws MalformedURLException {
        String item = m.getRequestedItem();

        if(cache.check(item)) {
            // forward buy request to server
            sendMessage(serverID, m);
        } else {
            sendReply(m.getPeerID(), item, false);
        }
    }

    @Override
    protected void processSell(Message m) throws MalformedURLException {
        // forward seller's message to server
        System.out.println("Forwarding stock message to warehouse");
        sendMessage(serverID, m);
    }

    // update seller, product info after selling an item
    private void sendReply(int peerID, String requestedItem, boolean isAvailable) throws MalformedURLException {
        Message m = new Message();
        m.setMessageType(Constants.TRADER_ACK);
        m.setRequestedItem(requestedItem);
        m.setAvailable(isAvailable);

        sendMessage(peerID, m);
    }


    @Override
    void processServerAck(Message m) throws MalformedURLException {
        boolean isBuyerRequest = m.getRequestedItem() != null;
        System.out.println("isBuyerRequest: " + isBuyerRequest);

        // leader forwards server's message to buyer
        if(isBuyerRequest)
            sendReply(m.getPeerID(), m.getRequestedItem(), m.isAvailable());
        else
            System.out.println("Stocked sellerID: " + m.getPeerID() + " items in warehouse");
    }

    void processLeaderAck(Message m) {}

    void receiveLeaderUpdate(Message m) {}
}
