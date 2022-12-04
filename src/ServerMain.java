import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ServerMain {
    public static void main(String[] args) throws IOException {
        // input args such as peerID/URLs, server process IP
        String configFileAddress = args[0];
        String databaseServerIP = null;
        List<Integer> neighborPeerIDs = new ArrayList<>();
        Map<Integer, String> peerIDIPMap = new HashMap<>();

        FileReader file = new FileReader(configFileAddress);
        BufferedReader br = new BufferedReader(file);
        String line;

        while((line = br.readLine()) != null) {
            String[] config = line.split(" ");
            databaseServerIP = config[0];
            List<Integer> inputNeighbourPeerIDs = Arrays.stream(config[1].split(",")).map(Integer::parseInt).collect(Collectors.toList());
            String[] inputNeighbourPeerIPs = config[2].split(",");

            for(int i=0; i<inputNeighbourPeerIDs.size(); i++){
                neighborPeerIDs.add(inputNeighbourPeerIDs.get(i));
                peerIDIPMap.put(inputNeighbourPeerIDs.get(i), inputNeighbourPeerIPs[i]);
            }
        }
        br.close();
        file.close();

        DatabaseServer server = new DatabaseServer(databaseServerIP, peerIDIPMap);
    }
}
