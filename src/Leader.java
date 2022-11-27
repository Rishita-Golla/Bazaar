import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Leader extends Peer{

    private int leaderID;
    List<Integer> neighborPeerIDs;
    private HashMap<Integer, HashMap<String, Integer>> sellerItemMap;
    SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");

    public Leader(int peerID, String peerType, String peerIP, List<Integer> neighborPeerIDs, Map<Integer, String> peerIPMap) {
        super(peerID, peerType, peerIP, neighborPeerIDs, peerIPMap);
        this.leaderID = peerID;
        this.neighborPeerIDs = neighborPeerIDs;
        this.sellerItemMap = new HashMap<>();
        new BroadcastThread().start();
        readDataFromFile();
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

    // Update seller, products info from the text file each time a new leader is elected
    public void readDataFromFile() {
        BufferedReader br;
        try {
            String outputPath = "src/sellerInfo.txt";
            File file = new File(outputPath);
            br = new BufferedReader(new FileReader(file));
            String line;
            int sellerID = 0;

            HashMap<String,Integer> map = new HashMap<>();
            while ((line = br.readLine()) != null) {
                if(line.equals("*")) {
                    HashMap mapCopy = (HashMap) map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    sellerItemMap.put(sellerID, mapCopy);
                    map.clear();
                }else{
                    String[] sellerIDInfo = line.split(":");
                    sellerID = Integer.parseInt(sellerIDInfo[0]);
                    String[] sellerInfo = sellerIDInfo[1].trim().split(",");
                    String item = sellerInfo[0];
                    int itemCount = Integer.parseInt(sellerInfo[1]);
                    if(sellerID != leaderID) {
                        map.put(item, itemCount);
                    }
                }
            }
            br.close();
            System.out.println("Sellers registered with leader successfully:  " + sellerItemMap);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    void processBuy(Message m) throws MalformedURLException {
        String requestedItem = m.getRequestedItem();
        boolean foundSeller = false;
        int sellerID = 0;

        for(Map.Entry<Integer, HashMap<String, Integer>> entry : sellerItemMap.entrySet()) {
            for(Map.Entry<String, Integer> itemAndCountMap : entry.getValue().entrySet())
                if(itemAndCountMap.getKey().equals(requestedItem) && itemAndCountMap.getValue() > 0) {
                    foundSeller = true;
                    sellerID = entry.getKey();
                    break;
                }
        }

        if(foundSeller) {
            updateItemCount(sellerID, requestedItem);
            sendTransactionAck(m.getBuyerID(), sellerID, requestedItem);
        }
    }

    // update seller, product info after selling an item
    private void updateItemCount(int sellerID, String item) {
        HashMap<String, Integer> map = sellerItemMap.get(sellerID);
        int count = map.get(item);
        map.put(item, --count);
        if(count == 0){
            sellerItemMap.remove(sellerID);
        }
        System.out.println("Seller and Item map: "+ sellerItemMap);
    }

    private void sendTransactionAck(int buyerID, int sellerID, String requestedItem) throws MalformedURLException {
        Message m = new Message();
        m.setMessageType(Constants.ACK);
        m.setRequestedItem(requestedItem);
        m.setBuyerID(buyerID);
        m.setSellerID(sellerID);

        sendMessage(buyerID, m);
        sendMessage(sellerID, m);
    }


    @Override
    void processAck(Message m) {

    }

    @Override
    void receiveLeaderUpdate(Message m) {
        return;
    }
}
