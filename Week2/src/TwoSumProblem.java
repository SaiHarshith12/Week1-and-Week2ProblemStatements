import java.util.*;

class Transaction {
    int id;
    int amount;
    String merchant;
    String account;
    long timestamp;

    public Transaction(int id, int amount, String merchant, String account, long timestamp) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        this.timestamp = timestamp;
    }
}

public class TwoSumProblem {

    // ------------------ TWO SUM ------------------
    public static List<int[]> findTwoSum(List<Transaction> txs, int target) {
        Map<Integer, List<Transaction>> map = new HashMap<>();
        List<int[]> result = new ArrayList<>();

        for (Transaction t : txs) {
            int complement = target - t.amount;

            if (map.containsKey(complement)) {
                for (Transaction prev : map.get(complement)) {
                    result.add(new int[]{prev.id, t.id});
                }
            }

            map.computeIfAbsent(t.amount, k -> new ArrayList<>()).add(t);
        }
        return result;
    }

    // ------------------ TWO SUM WITH 1-HOUR WINDOW ------------------
    public static List<int[]> findTwoSumWithWindow(List<Transaction> txs, int target) {
        txs.sort(Comparator.comparingLong(t -> t.timestamp));

        Map<Integer, List<Transaction>> map = new HashMap<>();
        List<int[]> result = new ArrayList<>();

        int left = 0;

        for (int right = 0; right < txs.size(); right++) {
            Transaction curr = txs.get(right);

            // Remove old transactions (>1 hour)
            while (curr.timestamp - txs.get(left).timestamp > 3600) {
                Transaction old = txs.get(left);

                List<Transaction> list = map.get(old.amount);
                if (list != null) {
                    list.remove(old);
                    if (list.isEmpty()) {
                        map.remove(old.amount);
                    }
                }
                left++;
            }

            int complement = target - curr.amount;

            if (map.containsKey(complement)) {
                for (Transaction t : map.get(complement)) {
                    result.add(new int[]{t.id, curr.id});
                }
            }

            map.computeIfAbsent(curr.amount, k -> new ArrayList<>()).add(curr);
        }

        return result;
    }

    // ------------------ K SUM ------------------
    public static List<List<Integer>> findKSum(List<Transaction> txs, int k, int target) {
        List<List<Integer>> result = new ArrayList<>();
        txs.sort(Comparator.comparingInt(t -> t.amount));

        kSumHelper(txs, k, target, 0, new ArrayList<>(), result);
        return result;
    }

    private static void kSumHelper(List<Transaction> txs, int k, int target, int start,
                                   List<Integer> current, List<List<Integer>> result) {

        if (k == 2) {
            int left = start, right = txs.size() - 1;

            while (left < right) {
                int sum = txs.get(left).amount + txs.get(right).amount;

                if (sum == target) {
                    List<Integer> temp = new ArrayList<>(current);
                    temp.add(txs.get(left).id);
                    temp.add(txs.get(right).id);
                    result.add(temp);

                    left++;
                    right--;
                } else if (sum < target) {
                    left++;
                } else {
                    right--;
                }
            }
            return;
        }

        for (int i = start; i < txs.size(); i++) {

            // Skip duplicates
            if (i > start && txs.get(i).amount == txs.get(i - 1).amount) continue;

            // Pruning
            if (txs.get(i).amount > target && txs.get(i).amount >= 0) break;

            current.add(txs.get(i).id);
            kSumHelper(txs, k - 1, target - txs.get(i).amount, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    // ------------------ DUPLICATE DETECTION ------------------
    public static List<Map<String, Object>> detectDuplicates(List<Transaction> txs) {
        Map<String, Set<String>> map = new HashMap<>();

        for (Transaction t : txs) {
            String key = t.amount + "|" + t.merchant;
            map.computeIfAbsent(key, k -> new HashSet<>()).add(t.account);
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (String key : map.keySet()) {
            if (map.get(key).size() > 1) {
                String[] parts = key.split("\\|");

                Map<String, Object> obj = new HashMap<>();
                obj.put("amount", Integer.parseInt(parts[0]));
                obj.put("merchant", parts[1]);
                obj.put("accounts", map.get(key));

                result.add(obj);
            }
        }

        return result;
    }

    // ------------------ MAIN ------------------
    public static void main(String[] args) {

        List<Transaction> txs = List.of(
                new Transaction(1, 500, "Store A", "acc1", 1000),
                new Transaction(2, 300, "Store B", "acc2", 1100),
                new Transaction(3, 200, "Store C", "acc3", 1200),
                new Transaction(4, 500, "Store A", "acc2", 1300)
        );

        System.out.println("Two Sum:");
        for (int[] pair : findTwoSum(txs, 500)) {
            System.out.println(Arrays.toString(pair));
        }

        System.out.println("\nTwo Sum (1-hour window):");
        for (int[] pair : findTwoSumWithWindow(new ArrayList<>(txs), 500)) {
            System.out.println(Arrays.toString(pair));
        }

        System.out.println("\nK Sum (k=3, target=1000):");
        for (List<Integer> combo : findKSum(new ArrayList<>(txs), 3, 1000)) {
            System.out.println(combo);
        }

        System.out.println("\nDuplicate Detection:");
        for (Map<String, Object> dup : detectDuplicates(txs)) {
            System.out.println(dup);
        }
    }
}


