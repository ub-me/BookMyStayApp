import java.util.*;

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
        inventory.put("Suite", 2);
    }

    int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }
}

// SEARCH (UC4 - READ ONLY)
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

// ✅ NEW: RESERVATION (BOOKING REQUEST)
class Reservation {
    String guestName;
    String roomType;

    Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    void display() {
        System.out.println(guestName + " requested " + roomType + " room");
    }
}

// ✅ NEW: BOOKING QUEUE (FIFO)
class BookingQueue {

    Queue<Reservation> queue;

    BookingQueue() {
        queue = new LinkedList<>();
    }

    // Add request
    void addRequest(Reservation r) {
        queue.add(r);
        System.out.println("Request added: ");
        r.display();
    }

    // Display queue
    void showQueue() {
        System.out.println("\nBooking Requests in Queue (FIFO Order):");
        for (Reservation r : queue) {
            r.display();
        }
    }
}

// MAIN CLASS
public class BookMyStayApp {
    public static void main(String[] args) {

        System.out.println("Book My Stay App v5.0");

        // Rooms
        Room single = new SingleRoom();
        Room doub = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Inventory
        RoomInventory inventory = new RoomInventory();

        // Search
        RoomSearch search = new RoomSearch();
        search.searchAvailableRooms(inventory, single, doub, suite);

        // ✅ Booking Requests (Queue)
        BookingQueue bookingQueue = new BookingQueue();

        bookingQueue.addRequest(new Reservation("Ujjwal", "Single"));
        bookingQueue.addRequest(new Reservation("Rahul", "Double"));
        bookingQueue.addRequest(new Reservation("Amit", "Suite"));

        // Show queue (FIFO)
        bookingQueue.showQueue();
    }
}