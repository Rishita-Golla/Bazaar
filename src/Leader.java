import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Leader extends Peer{

    private final int peerID;
    private List<Integer> leaderIDList;
    List<Integer> neighborPeerIDs;
    protected final Map<Integer, String> peerIDIPMap;
    // trader's local cache for item count lookup
    private Cache cache;
    protected int serverID;
    private int processedRequests = 0;
    SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
    Date date = new Date(System.currentTimeMillis());

    public Leader(int peerID, String peerType, String peerIP, List<Integer> neighborPeerIDs, Map<Integer, String> peerIPMap, List<Integer> leaderIdsList) throws InterruptedException {
        super(peerID, peerType, peerIP, neighborPeerIDs, peerIPMap);
        this.leaderIDList = leaderIdsList;
        this.peerIDIPMap = peerIPMap;
        this.peerID = peerID;
        this.neighborPeerIDs = neighborPeerIDs;
        cache = new Cache();
        this.serverID = 4; //testing
        Thread.sleep(8000);
        new UpdateCacheThread().start();
    }

    private class UpdateCacheThread extends Thread {
        public void run() {

            while(true) {
                if(processedRequests >= Constants.PROCESSED_REQUESTS_THRESHOLD) {
                    Message m = new Message();
                    m.setLeaderID(peerID);
                    m.setMessageType(Constants.CACHE_UPDATE);
                    System.out.println(formatter.format(date)+" Refreshing cache, processed requests count is: " + processedRequests);
                    try {
                        sendMessage(serverID, m);
                    } catch (MalformedURLException e) {
                        System.out.println(e.getMessage());
                    }
                    processedRequests = 0;
                }
            }
        }
    }

    private class BroadcastThread extends Thread {
        // Thread to broadcast elected leader
        public void run() {
        }
    }

    @Override
    void processBuy(Message m) throws MalformedURLException {
        String item = m.getRequestedItem();
        processedRequests++;

        if(cache.check(item)) {
            // forward buy request to server
            System.out.println(formatter.format(date)+" Forwarding buy message to warehouse");
            sendMessage(serverID, m);
        } else {
            System.out.println(formatter.format(date)+" Checked cache, item not available in inventory");
            sendReply(m.getPeerID(), item, false);
        }
    }

    @Override
    protected void processSell(Message m) throws MalformedURLException {
        // forward seller's message to server
        processedRequests++;
        System.out.println(formatter.format(date)+" Forwarding seller " + m.getPeerID() + " stock message to warehouse");
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
        System.out.println(formatter.format(date)+" Cache before receiving server ack: " + cache.getEntries());

        // leader forwards server's message to buyer and seller
        boolean isBuyerRequest = m.getRequestedItem() != null;
        if(isBuyerRequest) {
            String requestedItem = m.getRequestedItem();
            if(m.isAvailable()) {
                int count = cache.get(requestedItem);
                if(count == 0) {
                    cache.remove(requestedItem);
                    System.out.println(formatter.format(date)+" Removing " + requestedItem + " from cache");
                } else {
                    cache.put(requestedItem, --count);
                }
            }

            sendReply(m.getPeerID(), requestedItem, m.isAvailable());
        }
        else {
            String stockedItem = m.getStockedItem();
            System.out.println(formatter.format(date)+" Stocked sellerID: " + m.getPeerID() + " items in warehouse");
            cache.put(stockedItem, cache.get(stockedItem) + m.getStockItemCount());
        }

        System.out.println(formatter.format(date)+" Updated cache after server ack: " + cache.getEntries());
    }

    void processLeaderAck(Message m) {}

    void receiveLeaderUpdate(Message m) {}

    @Override
    protected void receiveCacheUpdate(Message m) {
        cache = new Cache(m.getCacheResponse());
        System.out.println(formatter.format(date)+" Updated cache " + cache.getEntries());
    }
}
