import java.util.HashMap;
import java.util.Map;

public class Cache {
    public Map<String, Integer> cache;

    public Cache() {
        cache = new HashMap<>();
    };

    public Cache(HashMap<String, Integer> updatedCache) {
        cache = new HashMap<>(updatedCache);
    }

    public boolean check(String item) {
        return cache.containsKey(item);
    }
    public int get(String item) {
        if(!check(item))
            return 0;

        return cache.get(item);
    }

    public void put(String item, int count) {
        cache.put(item, count);
    }

    public void remove(String item) {
        cache.remove(item);
    }
}
