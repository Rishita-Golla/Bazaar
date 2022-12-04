import java.util.HashMap;
import java.util.Map;

public class Cache {
    public Map<String, Integer> cache = new HashMap<>();

    public Cache(){};

    public boolean check(String item) {
        return cache.containsKey(item);
    }
    public int get(String item) {
        return cache.get(item);
    }

    public void put(String item, int count) {
        cache.put(item, count);
    }

    public void remove(String item) {
        cache.remove(item);
    }
}
