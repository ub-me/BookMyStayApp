import java.util.*;

// ROOM DOMAIN
abstract class Room {
    int beds;
    int price;

    Room(int beds, int price) {
        this.beds = beds;
        this.price = price;
    }
}

class SingleRoom extends Room {
    SingleRoom() { super(1, 1000); }
}

class DoubleRoom extends Room {
    DoubleRoom() { super(2, 2000); }
}

class SuiteRoom extends Room {
    SuiteRoom() { super(3, 5000); }
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
    }

    Reservation getNextRequest() {
        return queue.poll();
    }

    boolean isEmpty() {
        return queue.isEmpty();
    }
}

// ✅ NEW: BOOKING HISTORY
class BookingHistory {
    private List<Reservation> history = new ArrayList<>();

    void add(Reservation r) {
        history.add(r);
    }

    List<Reservation> getAllBookings() {
        return history;
    }
}

// BOOKING SERVICE
class BookingService {

    private RoomInventory inventory;
    private Set<String> allocatedRooms;
    private HashMap<String, Reservation> confirmedBookings;
    private BookingHistory history;

    int counter = 1;

    BookingService(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
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

                    // ✅ ADD TO HISTORY
                    history.add(r);

                    System.out.println("Confirmed: " + r.guestName + " -> " + roomId);
                }

            } else {
                System.out.println("Failed: " + r.guestName);
            }
        }
    }

    HashMap<String, Reservation> getConfirmedBookings() {
        return confirmedBookings;
    }
}

// SERVICE CLASS
class Service {
    String name;
    int cost;

    Service(String name, int cost) {
        this.name = name;
        this.cost = cost;
    }
}

// ADD-ON SERVICE MANAGER
class AddOnServiceManager {

    private HashMap<String, List<Service>> serviceMap = new HashMap<>();

    void addService(String roomId, Service service) {
        serviceMap.putIfAbsent(roomId, new ArrayList<>());
        serviceMap.get(roomId).add(service);
    }

    int calculateCost(String roomId) {

        List<Service> services = serviceMap.get(roomId);
        int total = 0;

        if (services != null) {
            for (Service s : services) {
                total += s.cost;
            }
        }

        return total;
    }
}

// ✅ NEW: REPORT SERVICE
class BookingReportService {

    void generateReport(List<Reservation> history) {

        System.out.println("\n--- BOOKING REPORT ---");

        for (Reservation r : history) {
            System.out.println("Guest: " + r.guestName +
                    ", Room: " + r.roomType +
                    ", ID: " + r.roomId);
        }

        System.out.println("Total Bookings: " + history.size());
    }
}

// MAIN
public class BookMyStayApp {
    public static void main(String[] args) {

        System.out.println("Book My Stay App v8.0");

        RoomInventory inventory = new RoomInventory();
        BookingQueue queue = new BookingQueue();
        BookingHistory history = new BookingHistory();

        // Requests
        queue.addRequest(new Reservation("Ujjwal", "Single"));
        queue.addRequest(new Reservation("Rahul", "Double"));
        queue.addRequest(new Reservation("Amit", "Suite"));

        // Process
        BookingService service = new BookingService(inventory, history);
        service.processBookings(queue);

        // Add-on services
        AddOnServiceManager manager = new AddOnServiceManager();
        HashMap<String, Reservation> bookings = service.getConfirmedBookings();

        for (String roomId : bookings.keySet()) {
            manager.addService(roomId, new Service("Breakfast", 200));
            manager.addService(roomId, new Service("WiFi", 100));
        }

        // ✅ REPORT GENERATION
        BookingReportService report = new BookingReportService();
        report.generateReport(history.getAllBookings());
    }
}