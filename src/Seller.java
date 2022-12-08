import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Seller extends Peer{

    private String sellerItem;
    protected List<Integer> leaderIdsList;

    private int selectedLeader;
    Random random = new Random();

    public Seller(int peerID, String peerType, String peerIP, List<Integer> neighborPeerIDs, Map<Integer, String> peerIPMap, String item, List<Integer> leaderIdsList) {
        super(peerID, peerType, peerIP, neighborPeerIDs, peerIPMap);
        this.sellerItem = item;
        this.leaderIdsList = leaderIdsList;
        this.selectedLeader = leaderIdsList.get(random.nextInt(leaderIdsList.size()));
        new StockGoodsThread().start();
    }
    
    public class StockGoodsThread extends Thread {

        public void run() {

            while(true) {
                try {
                    // stock goods every 5s
                    Thread.sleep(10000);
                    stockGoods();
                } catch (InterruptedException | MalformedURLException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private void stockGoods() throws MalformedURLException {
        Message m = new Message();
        m.setMessageType(Constants.SELL);
        m.setStockedItem(sellerItem);
        m.setStockItemCount(Constants.SELLER_STOCK_COUNT);
        m.setPeerID(this.peerID);

        System.out.println("The leader chosen by seller is:"+ this.selectedLeader);
        sendMessage(this.selectedLeader, m);
        //sendMessage(3, m);
    }

    @Override
    void receiveLeaderUpdate(Message m) {
        this.selectedLeader = m.getLeaderID();
        System.out.println("Received leader update, new leader Id is " + this.selectedLeader );
    }

    void processBuy(Message m) {
        // no implementation as seller doesn't receive buy message
    }

    protected void processSell(Message m) {}

    void processLeaderAck(Message m) {}

    void processServerAck(Message m) {}
}
