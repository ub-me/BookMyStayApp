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
    String roomId;

    Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }
}

// QUEUE
class BookingQueue {
    Queue<Reservation> queue = new LinkedList<>();

    void addRequest(Reservation r) {
        queue.add(r);
        System.out.println("Request Added: " + r.guestName + " -> " + r.roomType);
    }

    Reservation getNextRequest() {
        return queue.poll();
    }

    boolean isEmpty() {
        return queue.isEmpty();
    }
}

// BOOKING SERVICE
class BookingService {

    private RoomInventory inventory;
    private Set<String> allocatedRooms;
    private HashMap<String, Reservation> confirmedBookings;

    int counter = 1;

    BookingService(RoomInventory inventory) {
        this.inventory = inventory;
        allocatedRooms = new HashSet<>();
        confirmedBookings = new HashMap<>();
    }

    void processBookings(BookingQueue queue) {

        while (!queue.isEmpty()) {

            Reservation r = queue.getNextRequest();
            String type = r.roomType;

            if (inventory.getAvailability(type) > 0) {

                String roomId = type + "-" + counter++;

                if (!allocatedRooms.contains(roomId)) {

                    allocatedRooms.add(roomId);
                    inventory.reduceAvailability(type);

                    r.roomId = roomId;
                    confirmedBookings.put(roomId, r);

                    System.out.println("Booking Confirmed: " + r.guestName + " -> " + roomId);
                }

            } else {
                System.out.println("Booking Failed for " + r.guestName);
            }
        }
    }

    HashMap<String, Reservation> getConfirmedBookings() {
        return confirmedBookings;
    }
}

// ✅ NEW: SERVICE CLASS
class Service {
    String name;
    int cost;

    Service(String name, int cost) {
        this.name = name;
        this.cost = cost;
    }
}

// ✅ NEW: ADD-ON SERVICE MANAGER
class AddOnServiceManager {

    private HashMap<String, List<Service>> serviceMap = new HashMap<>();

    // Add service to reservation
    void addService(String roomId, Service service) {

        serviceMap.putIfAbsent(roomId, new ArrayList<>());
        serviceMap.get(roomId).add(service);

        System.out.println("Added Service: " + service.name + " to " + roomId);
    }

    // Calculate total cost
    void calculateCost(String roomId) {

        List<Service> services = serviceMap.get(roomId);

        if (services == null) {
            System.out.println("No services for " + roomId);
            return;
        }

        int total = 0;
        System.out.println("\nServices for " + roomId + ":");

        for (Service s : services) {
            System.out.println(s.name + " - " + s.cost);
            total += s.cost;
        }

        System.out.println("Total Add-on Cost: " + total);
    }
}

// MAIN
public class BookMyStayApp {
    public static void main(String[] args) {

        System.out.println("Book My Stay App v7.0");

        RoomInventory inventory = new RoomInventory();
        BookingQueue queue = new BookingQueue();

        // Booking requests
        queue.addRequest(new Reservation("Ujjwal", "Single"));
        queue.addRequest(new Reservation("Rahul", "Double"));

        // Process bookings
        BookingService service = new BookingService(inventory);
        service.processBookings(queue);

        // Get confirmed bookings
        HashMap<String, Reservation> bookings = service.getConfirmedBookings();

        // Add-on services
        AddOnServiceManager manager = new AddOnServiceManager();

        for (String roomId : bookings.keySet()) {
            manager.addService(roomId, new Service("Breakfast", 200));
            manager.addService(roomId, new Service("WiFi", 100));

            manager.calculateCost(roomId);
        }
    }
}