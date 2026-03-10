import java.util.*;
import java.util.concurrent.*;

class UsernameSystem {


    private Set<String> usernames = ConcurrentHashMap.newKeySet();


    private ConcurrentHashMap<String, Integer> popularity = new ConcurrentHashMap<>();

    public boolean isAvailable(String username) {
        popularity.merge(username, 1, Integer::sum);
        return !usernames.contains(username);
    }

    public void registerUsername(String username) {
        usernames.add(username);
    }

    public List<String> suggestUsernames(String username) {
        List<String> suggestions = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            String suggestion = username + i;
            if (!usernames.contains(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        suggestions.add(username + "_official");
        suggestions.add(username + "_123");

        return suggestions;
    }

    public Map<String, Integer> getPopularity() {
        return popularity;
    }
}
public class SocialMediaUsernameAvailabilityChecker {
    public static void main(String[] args) {

        UsernameSystem system = new UsernameSystem();

        system.registerUsername("harshith");
        system.registerUsername("sai");

        ExecutorService executor = Executors.newFixedThreadPool(20);

        String[] testUsers = {"harshith", "sai", "john", "alex", "harshith"};

        for (String user : testUsers) {
            executor.submit(() -> {
                if (system.isAvailable(user)) {
                    System.out.println(user + " is available");
                } else {
                    System.out.println(user + " is taken");

                    List<String> suggestions = system.suggestUsernames(user);
                    System.out.println("Suggestions: " + suggestions);
                }
            });
        }
        executor.shutdown();
    }
}

