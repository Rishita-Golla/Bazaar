import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Buyer extends Peer{

    protected final Map<Integer, String> peerIDIPMap;
    private String buyerItem;
    protected List<Integer> leaderIdsList;
    Random random = new Random();

    public Buyer(int peerID, String peerType, String peerIP, List<Integer> neighborPeerIDs, Map<Integer, String> peerIPMap, String item) {
        super(peerID, peerType, peerIP, neighborPeerIDs, peerIPMap);
        this.peerIDIPMap = peerIPMap;
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
        m.setMessageType(Constants.BUY);
        m.setRequestedItem(buyerItem);
        m.setPeerID(this.peerID);
        // int leaderID = random.nextInt(leaderIdsList.size());
        //m.setLeaderID(leaderID);

        //System.out.println("Starting new lookup for item: " + buyerItem + " with trader: " + leaderID);
        // sendMessage(leaderID, m);
        System.out.println("Starting new lookup for item: " + buyerItem + " with trader: " + 3);
        sendMessage(3, m);
    }

    @Override
    void processLeaderAck(Message m) {
        if(m.isAvailable())
            System.out.println("Received acknowledgement from trader, Shipped: " + m.getRequestedItem());
        else
            System.out.println("Received acknowledgement from trader, requested item: " + m.getRequestedItem()+" not available");
    }
    @Override
    void receiveLeaderUpdate(Message m) {
        // leaderIdsList = m.getLeaderIDsList();
        int leaderId = m.getLeaderID();
        System.out.println("Received leader update, new leader Ids are " + leaderId);
       // System.out.println("Received leader update, new leader Ids are " + leaderIdsList);
    }

    void processBuy(Message m) {
        // no implementation as buyer doesn't receive buy message
    }


    protected void processSell(Message m) {}

    void processServerAck(Message m) {
        // no implementation as buyer doesn't communicate with server
    }
}
