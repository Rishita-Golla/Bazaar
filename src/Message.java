import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class Message implements Serializable {

    private String requestedItem; // buyer requested item
    private String stockedItem;
    private int stockItemCount;
    private int peerID;
    private int leaderID;
    private String messageType;
    private boolean available;
    private HashMap<String, Integer> cacheResponse;
    private int sellerID;
    List<Integer> leaderIdsList;

    public void setCacheResponse(HashMap<String, Integer> cacheResponse) {
        this.cacheResponse = cacheResponse;
    }

    public HashMap<String, Integer> getCacheResponse() {
        return cacheResponse;
    }

    public int getStockItemCount() {
        return stockItemCount;
    }

    public void setStockItemCount(int stockItemCount) {
        this.stockItemCount = stockItemCount;
    }

    public String getStockedItem() {
        return stockedItem;
    }

    public void setStockedItem(String stockedItem) {
        this.stockedItem = stockedItem;
    }

    public int getPeerID() {
        return peerID;
    }

    public void setPeerID(int peerID) {
        this.peerID = peerID;
    }

    public List<Integer> getLeaderIdsList() {
        return leaderIdsList;
    }

    public void setLeaderIdsList(List<Integer> leaderIdsList) {
        this.leaderIdsList = leaderIdsList;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getRequestedItem() {
        return requestedItem;
    }

    public void setRequestedItem(String requestedItem) {
        this.requestedItem = requestedItem;
    }

    public int getLeaderID() {
        return leaderID;
    }

    public void setLeaderID(int leaderID) {
        this.leaderID = leaderID;
    }

    public int getSellerID() {
        return sellerID;
    }

    public void setSellerID(int sellerID) {
        this.sellerID = sellerID;
    }

}

