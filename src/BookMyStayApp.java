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

    // ✅ NEW: RESTORE INVENTORY
    void increaseAvailability(String type) {
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

// HISTORY
class BookingHistory {
    private List<Reservation> history = new ArrayList<>();

    void add(Reservation r) {
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
                BookingValidator.validate(r, inventory);

                String roomId = r.roomType + "-" + counter++;

                allocatedRooms.add(roomId);
                inventory.reduceAvailability(r.roomType);

                r.roomId = roomId;
                confirmedBookings.put(roomId, r);
                history.add(r);

                System.out.println("Confirmed: " + r.guestName + " -> " + roomId);

            } catch (InvalidBookingException e) {
                System.out.println("Failed: " + e.getMessage());
            }
        }
    }

    HashMap<String, Reservation> getConfirmedBookings() {
        return confirmedBookings;
    }
}

// ✅ NEW: CANCELLATION SERVICE
class CancellationService {

    private RoomInventory inventory;
    private HashMap<String, Reservation> bookings;

    // ✅ STACK for rollback
    private Stack<String> rollbackStack = new Stack<>();

    CancellationService(RoomInventory inventory,
                        HashMap<String, Reservation> bookings) {
        this.inventory = inventory;
        this.bookings = bookings;
    }

    void cancelBooking(String roomId) {

        if (!bookings.containsKey(roomId)) {
            System.out.println("Cancellation Failed: Booking not found");
            return;
        }

        Reservation r = bookings.get(roomId);

        if (r.isCancelled) {
            System.out.println("Cancellation Failed: Already cancelled");
            return;
        }

        // ✅ LIFO tracking
        rollbackStack.push(roomId);

        // ✅ rollback steps
        inventory.increaseAvailability(r.roomType);
        r.isCancelled = true;

        System.out.println("Cancelled: " + roomId);
    }

    void showRollbackStack() {
        System.out.println("Rollback Stack: " + rollbackStack);
    }
}

// MAIN
public class BookMyStayApp {
    public static void main(String[] args) {

        System.out.println("Book My Stay App v10.0");

        RoomInventory inventory = new RoomInventory();
        BookingQueue queue = new BookingQueue();
        BookingHistory history = new BookingHistory();

        // BOOKINGS
        queue.addRequest(new Reservation("Ujjwal", "Single"));
        queue.addRequest(new Reservation("Rahul", "Double"));

        BookingService service = new BookingService(inventory, history);
        service.processBookings(queue);

        HashMap<String, Reservation> bookings = service.getConfirmedBookings();

        // ✅ CANCELLATION
        CancellationService cancelService =
                new CancellationService(inventory, bookings);

        // Cancel one booking
        for (String roomId : bookings.keySet()) {
            cancelService.cancelBooking(roomId);
            break;
        }

        // Invalid cancellation
        cancelService.cancelBooking("Invalid-101");

        cancelService.showRollbackStack();
    }
}