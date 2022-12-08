import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Server {

    protected final Map<Integer, String> peerIDIPMap;
    private List<Integer> leaderIDList = new ArrayList<>();
    public HashMap<String, Integer> inventory = new HashMap<>();

    SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
    Date date = new Date(System.currentTimeMillis());

    Server(Map<Integer, String> peerIDIPMap) {
        // create a new file to store all item/count info
        this.peerIDIPMap = peerIDIPMap;
        leaderIDList.add(3);
        // leaderIDList.add(5);
        System.out.println(formatter.format(date)+" Inventory at start:" + inventory);
        System.out.println(formatter.format(date)+" Leader IDs" + leaderIDList);
    }

    public void processMessage(Message m) throws MalformedURLException {
        switch(m.getMessageType()) { // add Leader_BS case
            case Constants.BUY:
                processBuy(m);
                break;
            case Constants.SELL:
                processSell(m);
                break;
            case Constants.LEADER_UPDATE:
                receiveLeaderUpdate(m);
            case Constants.CACHE_UPDATE:
                sendCacheUpdate(m);
                break;
            default:
                System.out.println("Cannot process this request type");
        }
    }

    private void sendCacheUpdate(Message m) throws MalformedURLException {
        System.out.println(formatter.format(date)+" Sending cache update to leader: " + m.getLeaderID());
        Message cacheUpdate = new Message();
        cacheUpdate.setMessageType(Constants.CACHE_UPDATE);
        cacheUpdate.setCacheResponse(new HashMap<>(inventory));
        sendMessage(m.getLeaderID(), cacheUpdate);
    }

    public void receiveLeaderUpdate(Message m) {
        this.leaderIDList.clear();
        leaderIDList.add(m.getLeaderID());
        System.out.println("A leader went down. Updated leader list is: "+leaderIDList);
    }

    private void processBuy(Message m) throws MalformedURLException {

        Message messageFromServer = new Message();
        messageFromServer.setPeerID(m.getPeerID());
        messageFromServer.setRequestedItem(m.getRequestedItem());
        messageFromServer.setMessageType(Constants.SERVER_ACK);

        System.out.println(formatter.format(date)+" Requested item: " + m.getRequestedItem());
        inventory.clear();
        readDataFromFile();

        if(inventory.containsKey(m.getRequestedItem())){
            int stockCount = inventory.get(m.getRequestedItem());
            if(stockCount > 0) {
                messageFromServer.setAvailable(true);
                stockCount--;
                if(stockCount == 0) {
                    inventory.remove(m.getRequestedItem());
                }else{
                    inventory.put(m.getRequestedItem(),stockCount);
                }

            }else{
                messageFromServer.setAvailable(false);
            }
        }
        // update file to decrement item count
       // sendMessage(leaderId, reply);
        writeDataToFile();
        for(int leader:  leaderIDList)
        {
            sendMessage(leader, messageFromServer);
        }
    }

    private void processSell(Message m) throws MalformedURLException {

        Message messageFromServer = new Message();
        messageFromServer.setPeerID(m.getPeerID()); //this is the seller who sent the sell request
        messageFromServer.setMessageType(Constants.SERVER_ACK);
        messageFromServer.setStockedItem(m.getStockedItem());
        messageFromServer.setStockItemCount(m.getStockItemCount());

        inventory.put(m.getStockedItem(), inventory.getOrDefault(m.getStockedItem(), 0) + m.getStockItemCount());
        writeDataToFile();
        System.out.println(formatter.format(date)+" Status of inventory after stocking "+ inventory);
        System.out.println(formatter.format(date)+" Sending stock ack to trader");
        sendMessage(m.getLeaderID(),messageFromServer);
    }

    public void sendMessage(int receiverID, Message m) throws MalformedURLException {
        URL url = new URL(peerIDIPMap.get(receiverID));
        try {
            Registry registry = LocateRegistry.getRegistry(url.getHost(), url.getPort());
            RemoteInterface remoteInterface = (RemoteInterface) registry.lookup("RemoteInterface");
            remoteInterface.send(m);
        } catch (RemoteException | NotBoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public void writeDataToFile() {
        // open file
        String outputPath = "src/sellerInfo.txt";
        File file = new File(outputPath);
        BufferedWriter bf = null;
        try {
            // create new BufferedWriter for the output file
            bf = new BufferedWriter(new FileWriter(file));
            for (Map.Entry<String,Integer> entry : inventory.entrySet()) {
                    bf.write(entry.getKey() + ":"+entry.getValue());
                    bf.newLine();
                    bf.flush();
            }
            bf.flush();
            bf.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readDataFromFile() {
        BufferedReader br = null;
        try {
            String outputPath = "src/sellerInfo.txt";
            File file = new File(outputPath);
            br = new BufferedReader(new FileReader(file));
            String line = null;
            inventory.clear();
            while ((line = br.readLine()) != null) {
                // System.out.println(line);
                if(line.equals("null:0")) continue;
                String[] sellerInfo = line.split(":");
                String item = sellerInfo[0];
                int itemCount = Integer.parseInt(sellerInfo[1].trim());
                if(item.equals("null")){
                    continue;
                }else{
                    inventory.put(item, itemCount);
                }

            }
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
