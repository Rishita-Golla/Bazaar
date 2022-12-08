import java.io.Serializable;
import java.util.HashMap;

public class CacheResponse implements Serializable {

    private HashMap<String, Integer> cacheInfo;
    public HashMap<String, Integer> getCacheInfo() {
        return cacheInfo;
    }
}
