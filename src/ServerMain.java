import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ServerMain {
    public static void main(String[] args) throws IOException {
        // input args such as peerID/URLs, server process IP
        String databaseServerIP = args[0];
        String configFileAddress = args[1];

        Map<Integer, String> peerIDIPMap = new HashMap<>();

        FileReader file = new FileReader(configFileAddress);
        BufferedReader br = new BufferedReader(file);
        String line;

        while((line = br.readLine()) != null) {
            String[] config = line.split("=");
            peerIDIPMap.put(Integer.parseInt(config[0]),config[1]);
        }
        br.close();
        file.close();

        DatabaseServer server = new DatabaseServer(databaseServerIP, peerIDIPMap);
    }
}
