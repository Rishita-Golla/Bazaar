import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Buyer extends Peer{

    protected final Map<Integer, String> peerIDIPMap;
    private String buyerItem;
    protected List<Integer> leaderIdsList;
    SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
    Date date = new Date(System.currentTimeMillis());
    Random random = new Random();

    public Buyer(int peerID, String peerType, String peerIP, List<Integer> neighborPeerIDs, Map<Integer, String> peerIPMap, String item, List<Integer> leaderIdsList) {
        super(peerID, peerType, peerIP, neighborPeerIDs, peerIPMap);
        this.peerIDIPMap = peerIPMap;
        this.buyerItem = item;
        this.leaderIdsList = leaderIdsList;
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
                    Thread.sleep(6000);
                    startLookUpWithTrader();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            }
        }
    }

    private void startLookUpWithTrader() throws MalformedURLException {
        int selectedLeader = leaderIdsList.get(random.nextInt(leaderIdsList.size()));

        Message m = new Message();
        m.setMessageType(Constants.BUY);
        m.setRequestedItem(buyerItem);
        m.setPeerID(this.peerID);
        m.setLeaderID(selectedLeader);

        System.out.println(formatter.format(date)+" Starting new lookup for item: " + buyerItem + " with trader: " + selectedLeader);
        sendMessage(selectedLeader, m);
    }

    @Override
    void processLeaderAck(Message m) {
        if(m.isAvailable())
            System.out.println(formatter.format(date)+" Received acknowledgement from trader, Shipped: " + m.getRequestedItem());
        else
            System.out.println(formatter.format(date)+" Received acknowledgement from trader, requested item: " + m.getRequestedItem()+" not available");
    }

    @Override
    void receiveLeaderUpdate(Message m) {
        System.out.println(formatter.format(date)+" Received leader update, new leader Ids are " + m.getLeaderID());
    }

    @Override
    protected void receiveCacheUpdate(Message m) {
    }

    void processBuy(Message m) {
        // no implementation as buyer doesn't receive buy message
    }


    protected void processSell(Message m) {}

    void processServerAck(Message m) {
        // no implementation as buyer doesn't communicate with server
    }
}
