import java.net.MalformedURLException;;
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

    public Leader(int peerID, String peerType, String peerIP, List<Integer> neighborPeerIDs, Map<Integer, String> peerIPMap, List<Integer> leaderIdsList) throws InterruptedException {
        super(peerID, peerType, peerIP, neighborPeerIDs, peerIPMap);
        this.leaderIDList = leaderIdsList;
        this.peerIDIPMap = peerIPMap;
        this.peerID = peerID;
        this.neighborPeerIDs = neighborPeerIDs;
        cache = new Cache();
        this.serverID = 4; //testing
        new UpdateCacheThread().start();
        Thread.sleep(8000);
        // update serverID
        new BroadcastThread().start();
    }

    class TimeOutTask extends TimerTask {
        private Thread thread;
        private Timer timer;

        public TimeOutTask(Thread thread, Timer timer) {
            this.thread = thread;
            this.timer = timer;
        }

        @Override
        public void run() {
            if(thread != null && thread.isAlive()) {
                thread.interrupt();
                timer.cancel();
            }
        }
    }

    // Implement cache consistency
    private class UpdateCacheThread extends Thread {
        public void run() {
            boolean firstCheck = true;
            while(processedRequests >= Constants.PROCESSED_REQUESTS_THRESHOLD || firstCheck) {
                Message m = new Message();
                m.setLeaderID(peerID);
                m.setMessageType(Constants.CACHE_UPDATE);
                System.out.println("Refreshing cache, processed requests count is: " + processedRequests);
                try {
                    sendMessage(serverID, m);
                } catch (MalformedURLException e) {
                    System.out.println(e.getMessage());
                }
                processedRequests = 0;
                firstCheck = false;
            }
        }
    }

    private class BroadcastThread extends Thread {
        public void run() {
            int index =0;
            while(peerID == leaderIDList.get(index)) {
                index++;
            }
            int fellowLeader = leaderIDList.get(index);
            System.out.println("My fellow leader is:"+fellowLeader);

            String status = "OK";
            if(peerID == 3){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            while(status != null)
              {
                  try {
                      status = sendStatus(fellowLeader);
                      Thread.sleep(5000);
                  } catch (MalformedURLException | InterruptedException e) {
                      throw new RuntimeException(e);
                  }
              }

              System.out.println("My fellow leader is down. Redirecting buy/sell requests to myself at peerID: "+peerID);
              //fellow peer is down
              //send broadcast msg to all to redirect requests to new leader
              Message m = new Message();
              m.setMessageType(Constants.LEADER_UPDATE);
              m.setLeaderID(peerID);

              //send leader ID to the network
              for(int nodeID : peerIDIPMap.keySet()) {
                    try {
                        if(nodeID == fellowLeader)
                            continue;
                        else
                            System.out.println("nodeID:" +nodeID);
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
        processedRequests++;

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
        processedRequests++;
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
        if(isBuyerRequest) {
            String requestedItem = m.getRequestedItem();
            if(m.isAvailable())
                cache.put(requestedItem, cache.get(requestedItem)-1);

            sendReply(m.getPeerID(), requestedItem, m.isAvailable());
        }
        else {
            String stockedItem = m.getStockedItem();
            System.out.println("Stocked sellerID: " + m.getPeerID() + " items in warehouse");
            cache.put(stockedItem, cache.get(stockedItem) + m.getStockItemCount());
        }
    }

    void processLeaderAck(Message m) {}

    void receiveLeaderUpdate(Message m) {}

    @Override
    protected void receiveCacheUpdate(Message m) {
        cache = new Cache(m.getCacheResponse());
        System.out.println("Updated cache " + cache);
    }
}
