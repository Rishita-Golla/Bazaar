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

        //1 seller http://127.0.0.1:9001 2,3,4 http://127.0.0.1:9002,http://127.0.0.1:9003,http://127.0.0.1:9004 salt
        while((line = br.readLine()) != null) {
            String[] config = line.split(" ");
            peerID = Integer.parseInt(config[0]);
            peerType = config[1];
            peerIP = config[2];
            List<Integer> inputNeighbourPeerIDs = Arrays.stream(config[3].split(",")).map(Integer::parseInt).collect(Collectors.toList());
            item = config[4];

            for(int i=0; i<inputNeighbourPeerIDs.size(); i++){
                neighborPeerIDs.add(inputNeighbourPeerIDs.get(i));
            }
        }

        String commonNetworkConfigInfo = args[1];
        FileReader file1 = new FileReader(commonNetworkConfigInfo);
        BufferedReader br1 = new BufferedReader(file1);
        while((line = br1.readLine()) != null) {
            String[] config = line.split("=");
            peerIPMap.put(Integer.parseInt(config[0]),config[1]);
        }

        br1.close();
        file1.close();
        br.close();
        file.close();

        // send database server ID and IP
        System.out.println("PeerID " + peerID);
        System.out.println("NodeType = " + peerType);
        System.out.println("myIP = " + peerIP);
        System.out.println("neighborPeerIDs:" + neighborPeerIDs);
        System.out.println("peerIPMap:" + peerIPMap);

        List<Integer> leaderList = new ArrayList<>();
        leaderList.add(3);
        leaderList.add(5);

        Node node = new Node(peerID, peerType, peerIP, neighborPeerIDs, peerIPMap, item, leaderList);
        System.out.println("Node " + peerID + " initialized and running");
    }
}
