import java.util.*;

public class AutoComplete {

    // Trie Node
    class TrieNode {
        HashMap<Character, TrieNode> children = new HashMap<>();
        List<String> queries = new ArrayList<>();
    }

    private TrieNode root = new TrieNode();

    // query -> frequency
    private HashMap<String, Integer> frequency = new HashMap<>();

    // Insert query into trie
    private void insertQuery(String query) {

        TrieNode node = root;

        for (char c : query.toCharArray()) {

            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);

            if (!node.queries.contains(query))
                node.queries.add(query);
        }
    }

    // Update search frequency
    public void updateFrequency(String query) {

        frequency.put(query, frequency.getOrDefault(query, 0) + 1);

        insertQuery(query);

        System.out.println(query + " → Frequency: " + frequency.get(query));
    }

    // Search suggestions
    public List<String> search(String prefix) {

        TrieNode node = root;

        for (char c : prefix.toCharArray()) {

            if (!node.children.containsKey(c))
                return new ArrayList<>();

            node = node.children.get(c);
        }

        // Min heap for top results
        PriorityQueue<String> pq =
                new PriorityQueue<>((a, b) -> frequency.get(a) - frequency.get(b));

        for (String q : node.queries) {

            pq.add(q);

            if (pq.size() > 10)
                pq.poll();
        }

        List<String> result = new ArrayList<>();

        while (!pq.isEmpty())
            result.add(pq.poll());

        Collections.reverse(result);

        return result;
    }

    public static void main(String[] args) {

        AutoComplete system =
                new AutoComplete();

        system.updateFrequency("java tutorial");
        system.updateFrequency("javascript");
        system.updateFrequency("java download");
        system.updateFrequency("java tutorial");
        system.updateFrequency("java 21 features");

        List<String> suggestions = system.search("jav");

        System.out.println("\nSuggestions:");

        int rank = 1;
        for (String s : suggestions) {
            System.out.println(rank + ". " + s +
                    " (" + system.frequency.get(s) + " searches)");
            rank++;
        }
    }
}
