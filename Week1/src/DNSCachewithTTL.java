import java.util.*;

class DNSCache {

    class CacheEntry {
        String ip;
        long expiryTime;

        CacheEntry(String ip, long ttlSeconds) {
            this.ip = ip;
            this.expiryTime = System.currentTimeMillis() + ttlSeconds * 1000;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private final int capacity;
    private int hits = 0;
    private int misses = 0;

    private LinkedHashMap<String, CacheEntry> cache;

    public DNSCache(int capacity) {
        this.capacity = capacity;

        cache = new LinkedHashMap<String, CacheEntry>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
                return size() > DNSCache.this.capacity;
            }
        };
    }

    // Resolve domain
    public synchronized String resolve(String domain) {

        CacheEntry entry = cache.get(domain);

        if (entry != null) {
            if (!entry.isExpired()) {
                hits++;
                return entry.ip;
            } else {
                cache.remove(domain);
            }
        }

        misses++;

        String ip = queryUpstreamDNS(domain);

        cache.put(domain, new CacheEntry(ip, 10)); // TTL = 10 seconds

        return ip;
    }

    // Simulate upstream DNS lookup
    private String queryUpstreamDNS(String domain) {

        try {
            Thread.sleep(100); // simulate network delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "192.168.1." + new Random().nextInt(255);
    }

    // Remove expired entries
    public synchronized void cleanup() {

        Iterator<Map.Entry<String, CacheEntry>> it = cache.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, CacheEntry> entry = it.next();
            if (entry.getValue().isExpired()) {
                it.remove();
            }
        }
    }

    // Report hit/miss ratio
    public void reportStats() {

        int total = hits + misses;

        double hitRatio = total == 0 ? 0 : (double) hits / total;

        System.out.println("Cache Hits: " + hits);
        System.out.println("Cache Misses: " + misses);
        System.out.println("Hit Ratio: " + hitRatio);
    }
}
public class DNSCachewithTTL {
    public static void main(String[] args) throws Exception {

        DNSCache dnsCache = new DNSCache(3);

        System.out.println(dnsCache.resolve("google.com"));
        System.out.println(dnsCache.resolve("openai.com"));
        System.out.println(dnsCache.resolve("google.com"));
        System.out.println(dnsCache.resolve("github.com"));
        System.out.println(dnsCache.resolve("stackoverflow.com"));

        dnsCache.cleanup();

        dnsCache.reportStats();
    }
}

