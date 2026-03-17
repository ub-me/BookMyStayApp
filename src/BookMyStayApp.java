import java.util.HashMap;

// ROOM DOMAIN (same as UC2)
abstract class Room {
    int beds;
    int price;

    Room(int beds, int price) {
        this.beds = beds;
        this.price = price;
    }

    void displayRoom() {
        System.out.println("Beds: " + beds + ", Price: " + price);
    }
}

class SingleRoom extends Room {
    SingleRoom() {
        super(1, 1000);
    }
}

class DoubleRoom extends Room {
    DoubleRoom() {
        super(2, 2000);
    }
}

class SuiteRoom extends Room {
    SuiteRoom() {
        super(3, 5000);
    }
}

// ✅ NEW: INVENTORY CLASS (UC3 CORE)
class RoomInventory {

    private HashMap<String, Integer> inventory;

    // Constructor → initialize availability
    RoomInventory() {
        inventory = new HashMap<>();
        inventory.put("Single", 5);
        inventory.put("Double", 3);
        inventory.put("Suite", 2);
    }

    // Get availability
    int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }

    // Update availability (controlled)
    void updateAvailability(String type, int count) {
        inventory.put(type, count);
    }

    // Display full inventory
    void displayInventory() {
        System.out.println("\nRoom Availability:");
        for (String key : inventory.keySet()) {
            System.out.println(key + " Rooms Available: " + inventory.get(key));
        }
    }
}

// MAIN CLASS
public class BookMyStayApp {
    public static void main(String[] args) {

        System.out.println("Book My Stay App v3.0");

        // Room objects
        Room single = new SingleRoom();
        Room doub = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Inventory (centralized)
        RoomInventory inventory = new RoomInventory();

        // Display room details
        System.out.println("\nSingle Room:");
        single.displayRoom();
        System.out.println("Available: " + inventory.getAvailability("Single"));

        System.out.println("\nDouble Room:");
        doub.displayRoom();
        System.out.println("Available: " + inventory.getAvailability("Double"));

        System.out.println("\nSuite Room:");
        suite.displayRoom();
        System.out.println("Available: " + inventory.getAvailability("Suite"));

        // Show full inventory
        inventory.displayInventory();
    }
}