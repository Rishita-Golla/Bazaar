import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BuyerAndSeller extends Peer{

    private String buyerItem;
    private int leaderID;

    public BuyerAndSeller(int peerID, String peerType, String peerIP,
                          List<Integer> neighborPeerIDs, Map<Integer, String> peerIPMap, String item) {
        super(peerID, peerType, peerIP, neighborPeerIDs, peerIPMap);
        this.buyerItem = item;
        new LookUpThread().start();
    }

    private class LookUpThread extends Thread {
        public void run() {
            while(true){
                try {
                    Thread.sleep(4000);
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
        m.setRequestedItem(buyerItem);
        sendMessage(leaderID, m);
    }

    @Override
    void processBuy(Message m) {

    }

    @Override
    protected void processSell(Message m) {

    }

    @Override
    void processLeaderAck(Message m) {

    }

    @Override
    void receiveLeaderUpdate(Message m) {

    }

    void processServerAck(Message m) {}

//    @Override
//    void receiveLeaderUpdate(Message m) {
//        leaderID = m.getLeaderID();
//        System.out.println("Received leader update, new leader Id is " + leaderID);
//    }
}
