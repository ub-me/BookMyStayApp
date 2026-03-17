import java.util.HashMap;

// ROOM DOMAIN
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

// INVENTORY (UC3)
class RoomInventory {

    private HashMap<String, Integer> inventory;

    RoomInventory() {
        inventory = new HashMap<>();
        inventory.put("Single", 5);
        inventory.put("Double", 3);
        inventory.put("Suite", 0); // set 0 to test filtering
    }

    int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }

    void displayInventory() {
        System.out.println("\nInventory:");
        for (String key : inventory.keySet()) {
            System.out.println(key + " → " + inventory.get(key));
        }
    }
}

// ✅ NEW: SEARCH SERVICE (READ-ONLY)
class RoomSearch {

    void searchAvailableRooms(RoomInventory inventory,
                              Room single,
                              Room doub,
                              Room suite) {

        System.out.println("\nAvailable Rooms:\n");

        if (inventory.getAvailability("Single") > 0) {
            System.out.println("Single Room:");
            single.displayRoom();
            System.out.println("Available: " + inventory.getAvailability("Single") + "\n");
        }

        if (inventory.getAvailability("Double") > 0) {
            System.out.println("Double Room:");
            doub.displayRoom();
            System.out.println("Available: " + inventory.getAvailability("Double") + "\n");
        }

        if (inventory.getAvailability("Suite") > 0) {
            System.out.println("Suite Room:");
            suite.displayRoom();
            System.out.println("Available: " + inventory.getAvailability("Suite") + "\n");
        }
    }
}

// MAIN CLASS
public class BookMyStayApp {
    public static void main(String[] args) {

        System.out.println("Book My Stay App v4.0");

        // Room objects
        Room single = new SingleRoom();
        Room doub = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Inventory
        RoomInventory inventory = new RoomInventory();

        // Search (READ-ONLY)
        RoomSearch search = new RoomSearch();
        search.searchAvailableRooms(inventory, single, doub, suite);

        // Show inventory again (unchanged)
        inventory.displayInventory();
    }
}