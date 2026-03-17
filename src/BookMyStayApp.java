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

// INVENTORY
class RoomInventory {

    private HashMap<String, Integer> inventory;

    RoomInventory() {
        inventory = new HashMap<>();
        inventory.put("Single", 2);
        inventory.put("Double", 1);
        inventory.put("Suite", 1);
    }

    int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }

    void reduceAvailability(String type) {
        inventory.put(type, inventory.get(type) - 1);
    }
}

// RESERVATION
class Reservation {
    String guestName;
    String roomType;

    Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }
}

// QUEUE (UC5)
class BookingQueue {

    Queue<Reservation> queue = new LinkedList<>();

    void addRequest(Reservation r) {
        queue.add(r);
        System.out.println("Request Added: " + r.guestName + " -> " + r.roomType);
    }

    Reservation getNextRequest() {
        return queue.poll(); // FIFO
    }

    boolean isEmpty() {
        return queue.isEmpty();
    }
}

// ✅ NEW: BOOKING SERVICE (UC6 CORE)
class BookingService {

    private RoomInventory inventory;

    // Track allocated room IDs
    private Set<String> allocatedRooms;

    // Map room type → room IDs
    private HashMap<String, Set<String>> roomMap;

    int counter = 1;

    BookingService(RoomInventory inventory) {
        this.inventory = inventory;
        allocatedRooms = new HashSet<>();
        roomMap = new HashMap<>();
    }

    void processBookings(BookingQueue queue) {

        while (!queue.isEmpty()) {

            Reservation r = queue.getNextRequest();
            String type = r.roomType;

            System.out.println("\nProcessing: " + r.guestName + " -> " + type);

            if (inventory.getAvailability(type) > 0) {

                // Generate unique room ID
                String roomId = type + "-" + counter++;

                // Ensure uniqueness
                if (!allocatedRooms.contains(roomId)) {

                    allocatedRooms.add(roomId);

                    // Map type → IDs
                    roomMap.putIfAbsent(type, new HashSet<>());
                    roomMap.get(type).add(roomId);

                    // Reduce inventory
                    inventory.reduceAvailability(type);

                    System.out.println("Booking Confirmed!");
                    System.out.println("Room ID: " + roomId);

                }

            } else {
                System.out.println("Booking Failed (No Rooms Available)");
            }
        }
    }

    void displayAllocations() {
        System.out.println("\nAllocated Rooms:");
        for (String type : roomMap.keySet()) {
            System.out.println(type + " -> " + roomMap.get(type));
        }
    }
}

// MAIN CLASS
public class BookMyStayApp {
    public static void main(String[] args) {

        System.out.println("Book My Stay App v6.0");

        RoomInventory inventory = new RoomInventory();

        BookingQueue queue = new BookingQueue();

        // Add requests
        queue.addRequest(new Reservation("Ujjwal", "Single"));
        queue.addRequest(new Reservation("Rahul", "Single"));
        queue.addRequest(new Reservation("Amit", "Single")); // should fail
        queue.addRequest(new Reservation("Neha", "Double"));

        // Process bookings
        BookingService service = new BookingService(inventory);
        service.processBookings(queue);

        // Show allocation
        service.displayAllocations();
    }
}