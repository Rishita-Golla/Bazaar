import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {

        String configFileAddress = args[0];
        int peerID = 0;
        String peerType = null;
        String peerIP = null;
        String item = null;
        List<Integer> neighborPeerIDs = new ArrayList<>();
        Map<Integer, String> peerIPMap = new HashMap<>();

        FileReader file = new FileReader(configFileAddress);
        BufferedReader br = new BufferedReader(file);
        String line;

        while((line = br.readLine()) != null) {
            String[] config = line.split(" ");

            peerID = Integer.parseInt(config[0]);
            peerType = config[1];
            peerIP = config[2];
            List<Integer> inputNeighbourPeerIDs = Arrays.stream(config[3].split(",")).map(Integer::parseInt).collect(Collectors.toList());
            String[] inputNeighbourPeerIPs = config[4].split(",");
            item = config[5];

            for(int i=0; i<inputNeighbourPeerIDs.size(); i++){
                neighborPeerIDs.add(inputNeighbourPeerIDs.get(i));
                peerIPMap.put(inputNeighbourPeerIDs.get(i), inputNeighbourPeerIPs[i]);
            }
        }
        br.close();
        file.close();

        // send database server ID and IP
        System.out.println("PeerID " + peerID);
        System.out.println("NodeType = " + peerType);
        System.out.println("myIP = " + peerIP);
        System.out.println("neighborPeerIDs:" + neighborPeerIDs);
        System.out.println("peerIPMap:" + peerIPMap);

        Node node = new Node(peerID, peerType, peerIP, neighborPeerIDs, peerIPMap, item);
        System.out.println("Node " + peerID + " initialized and running");
    }
}
