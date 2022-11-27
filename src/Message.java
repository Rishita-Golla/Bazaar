import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {

    private String requestedItem; // buyer requested item
    private String lookUpId;
    private int hopCount;
    private List<Integer> path;
    private int sellerID; //set seller ID - during transaction
    private int buyerID;
    private int leaderID;
    private String messageType;

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public int getBuyerID() { return buyerID; }

    public void setBuyerID(int buyerID) { this.buyerID = buyerID; }

    public int getSellerID() {
        return sellerID;
    }

    public void setSellerID(int sellerID) {
        this.sellerID = sellerID;
    }

    public void Message(int hopCount) {
        this.hopCount = hopCount;
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

    public void setLookUpId(String lookUpId) {
        this.lookUpId = lookUpId;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    public List<Integer> getPath() {
        return path;
    }

    public void setPath(List<Integer> path) {
        this.path = path;
    }

    public void addLastInPath(int ID) {
        this.path.add(ID);
    }

    public int removeLastNodeInPath() {
        int lastNode = path.get(path.size()-1);
        path.remove(path.get(path.size()-1));
        return lastNode;
    }
}

