import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class Product {
    String productId;
    int stock;
    Queue<String> waitingList;

    public Product(String productId, int stock) {
        this.productId = productId;
        this.stock = stock;
        this.waitingList = new LinkedList<>();
    }
}
class InventorySystem {

    private ConcurrentHashMap<String, Product> inventory = new ConcurrentHashMap<>();

    // Add new product
    public void addProduct(String productId, int stock) {
        inventory.put(productId, new Product(productId, stock));
    }

    // Check stock availability (O1)
    public boolean checkAvailability(String productId) {
        Product p = inventory.get(productId);
        return p != null && p.stock > 0;
    }

    // Purchase request
    public synchronized void purchase(String productId, String user) {

        Product p = inventory.get(productId);

        if (p == null) {
            System.out.println("Product not found");
            return;
        }

        if (p.stock > 0) {
            p.stock--;
            System.out.println(user + " purchased " + productId);
        }
        else {
            p.waitingList.add(user);
            System.out.println(user + " added to waiting list for " + productId);
        }
    }

    // Restock product
    public synchronized void restock(String productId, int quantity) {

        Product p = inventory.get(productId);

        if (p == null)
            return;

        p.stock += quantity;

        while (p.stock > 0 && !p.waitingList.isEmpty()) {
            String user = p.waitingList.poll();
            p.stock--;
            System.out.println("Product allocated to waiting user: " + user);
        }
    }

    // Display stock
    public void showStock(String productId) {
        Product p = inventory.get(productId);
        if (p != null)
            System.out.println("Stock of " + productId + ": " + p.stock);
    }
}
public class EcommerceFlashSaleInventoryManager {
    public static void main(String[] args) {

        InventorySystem system = new InventorySystem();

        system.addProduct("Laptop", 2);

        system.purchase("Laptop", "User1");
        system.purchase("Laptop", "User2");
        system.purchase("Laptop", "User3"); // waiting list

        system.showStock("Laptop");

        system.restock("Laptop", 2);

        system.showStock("Laptop");
    }
}

