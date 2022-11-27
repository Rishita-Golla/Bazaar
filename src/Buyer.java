import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Buyer extends Peer{

    protected final Map<Integer, String> peerIDIPMap;
    private String buyerItem;
    private final int buyerID;
    private int leaderID;

    public Buyer(int peerID, String peerType, String peerIP, List<Integer> neighborPeerIDs, Map<Integer, String> peerIPMap, String item) {
        super(peerID, peerType, peerIP, neighborPeerIDs, peerIPMap);
        this.peerIDIPMap = peerIPMap;
        this.buyerID = peerID;
        this.buyerItem = item;
        new LookUpThread().start();
    }

    private class LookUpThread extends Thread {
        public void run() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            while(true){
                try {
                    Thread.sleep(5000);
                    startLookUpWithTrader();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            }
        }
    }

    private void startLookUpWithTrader() throws MalformedURLException {
        Message m = new Message();
        String lookupId = UUID.randomUUID().toString();
        m.setLookUpId(lookupId);
        m.setMessageType(Constants.BUY);
        m.setRequestedItem(buyerItem);
        m.setBuyerID(buyerID);

        System.out.println("Starting new lookup for item: " + buyerItem);

        sendMessage(leaderID, m);
    }

    @Override
    void processBuy(Message m) {
    }

    @Override
    void processAck(Message m) {
        System.out.println("Received acknowledgement from trader, bought " + m.getRequestedItem() + " from seller " + m.getSellerID());
    }

    @Override
    void receiveLeaderUpdate(Message m) {
        leaderID = m.getLeaderID();
        System.out.println("Received leader update, new leader Id is " + leaderID);
    }
}
