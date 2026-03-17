import java.util.*;

// ------------------ VIDEO DATA ------------------
class Video {
    String id;
    String content;

    public Video(String id, String content) {
        this.id = id;
        this.content = content;
    }
}

// ------------------ LRU CACHE ------------------
class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    public LRUCache(int capacity) {
        super(capacity, 0.75f, true); // access-order
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}

// ------------------ MAIN CACHE SYSTEM ------------------
public class CacheSystem {

    private LRUCache<String, Video> L1 = new LRUCache<>(10000);
    private LRUCache<String, Video> L2 = new LRUCache<>(100000);
    private Map<String, Video> L3 = new HashMap<>();

    private Map<String, Integer> accessCount = new HashMap<>();

    // Stats
    private int L1Hits = 0, L2Hits = 0, L3Hits = 0;
    private int totalRequests = 0;

    // ------------------ GET VIDEO ------------------
    public Video getVideo(String id) {
        totalRequests++;

        // -------- L1 --------
        if (L1.containsKey(id)) {
            L1Hits++;
            System.out.println("L1 Cache HIT (0.5ms)");
            return L1.get(id);
        }

        System.out.println("L1 Cache MISS (0.5ms)");

        // -------- L2 --------
        if (L2.containsKey(id)) {
            L2Hits++;
            System.out.println("L2 Cache HIT (5ms)");

            Video v = L2.get(id);
            promoteToL1(id, v);
            return v;
        }

        System.out.println("L2 Cache MISS (5ms)");

        // -------- L3 --------
        if (L3.containsKey(id)) {
            L3Hits++;
            System.out.println("L3 Database HIT (150ms)");

            Video v = L3.get(id);
            promoteToL2(id, v);
            return v;
        }

        System.out.println("Video not found!");
        return null;
    }

    // ------------------ PROMOTION ------------------
    private void promoteToL1(String id, Video v) {
        L1.put(id, v);
    }

    private void promoteToL2(String id, Video v) {
        L2.put(id, v);
    }

    // ------------------ UPDATE ACCESS COUNT ------------------
    private void updateAccess(String id) {
        accessCount.put(id, accessCount.getOrDefault(id, 0) + 1);
    }

    // ------------------ ADD VIDEO (DATABASE LOAD) ------------------
    public void addToDatabase(Video v) {
        L3.put(v.id, v);
    }

    // ------------------ INVALIDATE ------------------
    public void invalidate(String id) {
        L1.remove(id);
        L2.remove(id);
        L3.remove(id);
        accessCount.remove(id);

        System.out.println("Cache invalidated for: " + id);
    }

    // ------------------ STATISTICS ------------------
    public void getStatistics() {
        double l1Rate = (totalRequests == 0) ? 0 : (L1Hits * 100.0 / totalRequests);
        double l2Rate = (totalRequests == 0) ? 0 : (L2Hits * 100.0 / totalRequests);
        double l3Rate = (totalRequests == 0) ? 0 : (L3Hits * 100.0 / totalRequests);

        System.out.printf("L1 Hit Rate: %.2f%%\n", l1Rate);
        System.out.printf("L2 Hit Rate: %.2f%%\n", l2Rate);
        System.out.printf("L3 Hit Rate: %.2f%%\n", l3Rate);

        double overall = ((L1Hits + L2Hits + L3Hits) * 100.0 / totalRequests);
        System.out.printf("Overall Hit Rate: %.2f%%\n", overall);
    }

    // ------------------ MAIN ------------------
    public static void main(String[] args) {

        CacheSystem cache = new CacheSystem();

        // Load DB
        cache.addToDatabase(new Video("video_123", "Movie A"));
        cache.addToDatabase(new Video("video_999", "Movie B"));

        System.out.println("Request 1:");
        cache.getVideo("video_123");

        System.out.println("\nRequest 2:");
        cache.getVideo("video_123");

        System.out.println("\nRequest 3:");
        cache.getVideo("video_999");

        System.out.println("\nStatistics:");
        cache.getStatistics();
    }
}
