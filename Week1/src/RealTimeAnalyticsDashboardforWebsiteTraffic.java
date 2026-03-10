import java.util.*;
import java.util.concurrent.*;

class PageEvent {
    String page;
    String user;
    String source;

    PageEvent(String page, String user, String source) {
        this.page = page;
        this.user = user;
        this.source = source;
    }
}
class StreamingAnalytics {
    private ConcurrentHashMap<String, Integer> pageVisits = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Integer> sourceCounts = new ConcurrentHashMap<>();

    public void processEvent(PageEvent event) {


        pageVisits.merge(event.page, 1, Integer::sum);

        // update unique visitors
        uniqueVisitors.putIfAbsent(event.page, ConcurrentHashMap.newKeySet());
        uniqueVisitors.get(event.page).add(event.user);

        // update traffic sources
        sourceCounts.merge(event.source, 1, Integer::sum);
    }

    public List<Map.Entry<String,Integer>> getTopPages() {

        PriorityQueue<Map.Entry<String,Integer>> pq =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<String,Integer> entry : pageVisits.entrySet()) {
            pq.offer(entry);
            if (pq.size() > 10)
                pq.poll();
        }

        List<Map.Entry<String,Integer>> result = new ArrayList<>(pq);
        result.sort((a,b) -> b.getValue() - a.getValue());

        return result;
    }
    public void showDashboard() {

        System.out.println("\n===== DASHBOARD =====");

        System.out.println("Top Pages:");
        for (Map.Entry<String,Integer> e : getTopPages()) {
            System.out.println(e.getKey() + " -> " + e.getValue());
        }

        System.out.println("\nUnique Visitors Per Page:");
        for (String page : uniqueVisitors.keySet()) {
            System.out.println(page + " -> " + uniqueVisitors.get(page).size());
        }

        System.out.println("\nTraffic Sources:");
        for (Map.Entry<String,Integer> e : sourceCounts.entrySet()) {
            System.out.println(e.getKey() + " -> " + e.getValue());
        }
    }
}
public class RealTimeAnalyticsDashboardforWebsiteTraffic {

    public static void main(String[] args) {

        StreamingAnalytics analytics = new StreamingAnalytics();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {
            analytics.showDashboard();
        }, 5, 5, TimeUnit.SECONDS);

        String[] pages = {"home", "about", "product", "contact", "pricing"};
        String[] sources = {"Google", "Facebook", "Direct", "Twitter"};

        Random rand = new Random();

        while (true) {

            PageEvent event = new PageEvent(
                    pages[rand.nextInt(pages.length)],
                    "user" + rand.nextInt(20),
                    sources[rand.nextInt(sources.length)]
            );

            analytics.processEvent(event);

            try {
                Thread.sleep(200); // simulate incoming traffic
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

