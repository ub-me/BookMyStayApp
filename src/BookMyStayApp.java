import java.util.*;

// ✅ CUSTOM EXCEPTION
class InvalidBookingException extends Exception {
    InvalidBookingException(String message) {
        super(message);
    }
}

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

// ✅ THREAD-SAFE INVENTORY
class RoomInventory {
    private HashMap<String, Integer> inventory;

    RoomInventory() {
        inventory = new HashMap<>();
        inventory.put("Single", 2);
        inventory.put("Double", 1);
        inventory.put("Suite", 1);
    }

    synchronized int getAvailability(String type) {
        return inventory.getOrDefault(type, -1);
    }

    synchronized void reduceAvailability(String type) throws InvalidBookingException {
        int current = inventory.get(type);

        if (current <= 0) {
            throw new InvalidBookingException("No rooms available for " + type);
        }

        inventory.put(type, current - 1);
    }

    synchronized void increaseAvailability(String type) {
        inventory.put(type, inventory.get(type) + 1);
    }

    boolean isValidRoomType(String type) {
        return inventory.containsKey(type);
    }
}

// RESERVATION
class Reservation {
    String guestName;
    String roomType;
    String roomId;
    boolean isCancelled = false;

    Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }
}

// ✅ THREAD-SAFE QUEUE
class BookingQueue {
    private Queue<Reservation> queue = new LinkedList<>();

    synchronized void addRequest(Reservation r) {
        queue.add(r);
    }

    synchronized Reservation getNextRequest() {
        return queue.poll();
    }

    synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
}

// HISTORY
class BookingHistory {
    private List<Reservation> history = new ArrayList<>();

    synchronized void add(Reservation r) {
        history.add(r);
    }

    List<Reservation> getAllBookings() {
        return history;
    }
}

// VALIDATOR
class BookingValidator {

    static void validate(Reservation r, RoomInventory inventory)
            throws InvalidBookingException {

        if (r.guestName == null || r.guestName.isEmpty()) {
            throw new InvalidBookingException("Guest name cannot be empty");
        }

        if (!inventory.isValidRoomType(r.roomType)) {
            throw new InvalidBookingException("Invalid room type: " + r.roomType);
        }

        if (inventory.getAvailability(r.roomType) <= 0) {
            throw new InvalidBookingException("Room not available: " + r.roomType);
        }
    }
}

// ✅ THREAD-SAFE BOOKING SERVICE
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

    // ✅ synchronized critical section
    synchronized void processSingleBooking(Reservation r) {

        try {
            BookingValidator.validate(r, inventory);

            String roomId = r.roomType + "-" + counter++;

            allocatedRooms.add(roomId);
            inventory.reduceAvailability(r.roomType);

            r.roomId = roomId;
            confirmedBookings.put(roomId, r);
            history.add(r);

            System.out.println(Thread.currentThread().getName() +
                    " CONFIRMED -> " + r.guestName + " : " + roomId);

        } catch (InvalidBookingException e) {
            System.out.println(Thread.currentThread().getName() +
                    " FAILED -> " + e.getMessage());
        }
    }

    HashMap<String, Reservation> getConfirmedBookings() {
        return confirmedBookings;
    }
}

// ✅ THREAD WORKER
class BookingProcessor extends Thread {

    private BookingQueue queue;
    private BookingService service;

    BookingProcessor(String name, BookingQueue queue, BookingService service) {
        super(name);
        this.queue = queue;
        this.service = service;
    }

    public void run() {

        while (true) {

            Reservation r;

            synchronized (queue) {
                if (queue.isEmpty()) break;
                r = queue.getNextRequest();
            }

            if (r != null) {
                service.processSingleBooking(r);
            }
        }
    }
}

// CANCELLATION SERVICE
class CancellationService {

    private RoomInventory inventory;
    private HashMap<String, Reservation> bookings;
    private Stack<String> rollbackStack = new Stack<>();

    CancellationService(RoomInventory inventory,
                        HashMap<String, Reservation> bookings) {
        this.inventory = inventory;
        this.bookings = bookings;
    }

    void cancelBooking(String roomId) {

        if (!bookings.containsKey(roomId)) {
            System.out.println("Cancel Failed: Not found");
            return;
        }

        Reservation r = bookings.get(roomId);

        if (r.isCancelled) {
            System.out.println("Already cancelled");
            return;
        }

        rollbackStack.push(roomId);
        inventory.increaseAvailability(r.roomType);
        r.isCancelled = true;

        System.out.println("Cancelled -> " + roomId);
    }
}

// REPORT
class BookingReportService {

    void generateReport(List<Reservation> history) {

        System.out.println("\n--- REPORT ---");

        for (Reservation r : history) {
            System.out.println(r.guestName + " | " + r.roomType + " | " + r.roomId);
        }

        System.out.println("Total: " + history.size());
    }
}

// MAIN
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("Book My Stay App v11.0 🚀");

        RoomInventory inventory = new RoomInventory();
        BookingQueue queue = new BookingQueue();
        BookingHistory history = new BookingHistory();

        // MULTIPLE REQUESTS
        queue.addRequest(new Reservation("Ujjwal", "Single"));
        queue.addRequest(new Reservation("Rahul", "Single"));
        queue.addRequest(new Reservation("Amit", "Double"));
        queue.addRequest(new Reservation("Ravi", "Suite"));

        BookingService service = new BookingService(inventory, history);

        // MULTIPLE THREADS
        BookingProcessor t1 = new BookingProcessor("Thread-1", queue, service);
        BookingProcessor t2 = new BookingProcessor("Thread-2", queue, service);

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (Exception e) {}

        // REPORT
        BookingReportService report = new BookingReportService();
        report.generateReport(history.getAllBookings());
    }
}