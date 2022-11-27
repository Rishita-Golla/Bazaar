import java.util.List;
import java.util.Map;

public class Seller extends Peer{

    private String sellerItem;
    private int leaderID;

    public Seller(int peerID, String peerType, String peerIP, List<Integer> neighborPeerIDs, Map<Integer, String> peerIPMap, String item) {
        super(peerID, peerType, peerIP, neighborPeerIDs, peerIPMap);
        this.sellerItem = item;
    }

    @Override
    void processBuy(Message m) {

    }

    @Override
    void processAck(Message m) {
        System.out.println("Sold requested item " + m.getRequestedItem() + " to buyer " + m.getBuyerID());
    }

    @Override
    void receiveLeaderUpdate(Message m) {
        leaderID = m.getLeaderID();
        System.out.println("Received leader update, new leader Id is " + leaderID);
    }
}
