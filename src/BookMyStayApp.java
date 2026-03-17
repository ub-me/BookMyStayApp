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
        return inventory.getOrDefault(type, -1);
    }

    void reduceAvailability(String type) throws InvalidBookingException {
        int current = inventory.get(type);

        if (current <= 0) {
            throw new InvalidBookingException("No rooms available for " + type);
        }

        inventory.put(type, current - 1);
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

// BOOKING HISTORY
class BookingHistory {
    private List<Reservation> history = new ArrayList<>();

    void add(Reservation r) {
        history.add(r);
    }

    List<Reservation> getAllBookings() {
        return history;
    }
}

// ✅ VALIDATOR CLASS
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

            try {
                // ✅ VALIDATION
                BookingValidator.validate(r, inventory);

                String roomId = r.roomType + "-" + counter++;

                if (!allocatedRooms.contains(roomId)) {

                    allocatedRooms.add(roomId);
                    inventory.reduceAvailability(r.roomType);

                    r.roomId = roomId;
                    confirmedBookings.put(roomId, r);
                    history.add(r);

                    System.out.println("Confirmed: " + r.guestName + " -> " + roomId);
                }

            } catch (InvalidBookingException e) {
                // ✅ GRACEFUL ERROR
                System.out.println("Booking Failed: " + e.getMessage());
            }
        }
    }
}

// REPORT SERVICE
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

        System.out.println("Book My Stay App v9.0");

        RoomInventory inventory = new RoomInventory();
        BookingQueue queue = new BookingQueue();
        BookingHistory history = new BookingHistory();

        // ✅ TEST CASES (VALID + INVALID)
        queue.addRequest(new Reservation("Ujjwal", "Single"));   // valid
        queue.addRequest(new Reservation("", "Double"));         // invalid name
        queue.addRequest(new Reservation("Rahul", "Luxury"));    // invalid type
        queue.addRequest(new Reservation("Amit", "Suite"));      // valid
        queue.addRequest(new Reservation("Ravi", "Suite"));      // no availability

        BookingService service = new BookingService(inventory, history);
        service.processBookings(queue);

        BookingReportService report = new BookingReportService();
        report.generateReport(history.getAllBookings());
    }
}