import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Server {

    protected final Map<Integer, String> peerIDIPMap;
    private List<Integer> leaderIDList = new ArrayList<>();

    public static HashMap<String, Integer> inventory = new HashMap<>();

    Server(Map<Integer, String> peerIDIPMap) {
        // create a new file to store all item/count info
        this.peerIDIPMap = peerIDIPMap;
        leaderIDList.add(3);
        leaderIDList.add(5);
        System.out.println(inventory);
        System.out.println(leaderIDList);
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
                break;
            default:
                System.out.println("Cannot process this request type");
        }

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

        System.out.println("req item: " + m.getRequestedItem());

        //int itemCount = 5; // access file and get item count
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

        // send item and count to inc cache?
        // access file and update item count - stock
        inventory.put(m.getStockedItem(), m.getStockItemCount());
        writeDataToFile();
        System.out.println("Status of inventory after stocking"+ inventory);
        System.out.println("Sending stock ack to trader");
        sendMessage(m.getLeaderID(),messageFromServer);

//        for(int leader:  leaderIDList)
//        {
//            sendMessage(leader, messageFromServer);
//        }
        //sendMessage(3, m); // set boolean ack. When warehouse can't stock?
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
                System.out.println(line);
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
