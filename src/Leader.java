import java.net.MalformedURLException;;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Leader extends Peer{

    private int peerID;
    private List<Integer> leaderIDList;
    List<Integer> neighborPeerIDs;
    protected final Map<Integer, String> peerIDIPMap;
    // trader's local cache for item count lookup
    private Cache cache;
    protected int serverID;
    SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");

    public Leader(int peerID, String peerType, String peerIP, List<Integer> neighborPeerIDs, Map<Integer, String> peerIPMap, List<Integer> leaderIdsList) throws InterruptedException {
        super(peerID, peerType, peerIP, neighborPeerIDs, peerIPMap);
        this.leaderIDList = leaderIdsList;
        this.peerIDIPMap = peerIPMap;
        this.peerID = peerID;
        this.neighborPeerIDs = neighborPeerIDs;
        cache = new Cache();
        this.serverID = 4; //testing
        Thread.sleep(8000);
        // update serverID
        updateCache();
        new BroadcastThread().start();
//
//        Thread thread = new Thread(new BroadcastThread());
//        thread.start();
//
//        Timer timer = new Timer();
//        TimeOutTask timeOutTask = new TimeOutTask(thread, timer);
//        timer.schedule(timeOutTask, 300000);
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
    private void updateCache() throws InterruptedException {
        cache.put("salt", 5);
       // Thread.sleep(5000);
    }

    public class BroadcastThread extends Thread {
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
                      Thread.sleep(2000);
                  } catch (MalformedURLException e) {
                      throw new RuntimeException(e);
                  } catch (InterruptedException e) {
                      throw new RuntimeException(e);
                  }
              }

              if(status == null)
              {
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
