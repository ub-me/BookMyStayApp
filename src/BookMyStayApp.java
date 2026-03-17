import java.io.*;
import java.util.*;

// ================= EXCEPTION =================
class InvalidBookingException extends Exception {
    InvalidBookingException(String msg) {
        super(msg);
    }
}

// ================= ROOM =================
abstract class Room implements Serializable {
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

// ================= INVENTORY =================
class RoomInventory implements Serializable {
    HashMap<String, Integer> inventory = new HashMap<>();

    RoomInventory() {
        inventory.put("Single", 2);
        inventory.put("Double", 1);
        inventory.put("Suite", 1);
    }

    synchronized int getAvailability(String type) {
        return inventory.getOrDefault(type, -1);
    }

    synchronized void reduce(String type) throws InvalidBookingException {
        int val = inventory.get(type);
        if (val <= 0) throw new InvalidBookingException("No rooms available for " + type);
        inventory.put(type, val - 1);
    }

    synchronized void increase(String type) {
        inventory.put(type, inventory.get(type) + 1);
    }

    boolean isValid(String type) {
        return inventory.containsKey(type);
    }
}

// ================= RESERVATION =================
class Reservation implements Serializable {
    String guestName;
    String roomType;
    String roomId;
    boolean cancelled = false;

    Reservation(String g, String r) {
        guestName = g;
        roomType = r;
    }
}

// ================= QUEUE =================
class BookingQueue {
    Queue<Reservation> queue = new LinkedList<>();

    synchronized void add(Reservation r) {
        queue.add(r);
    }

    synchronized Reservation poll() {
        return queue.poll();
    }

    synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
}

// ================= HISTORY =================
class BookingHistory implements Serializable {
    List<Reservation> list = new ArrayList<>();

    synchronized void add(Reservation r) {
        list.add(r);
    }

    List<Reservation> getAll() {
        return list;
    }
}

// ================= VALIDATOR =================
class Validator {
    static void validate(Reservation r, RoomInventory inv) throws InvalidBookingException {
        if (r.guestName == null || r.guestName.trim().isEmpty())
            throw new InvalidBookingException("Invalid guest name");

        if (!inv.isValid(r.roomType))
            throw new InvalidBookingException("Invalid room type");

        if (inv.getAvailability(r.roomType) <= 0)
            throw new InvalidBookingException("Room not available");
    }
}

// ================= BOOKING SERVICE =================
class BookingService {
    RoomInventory inventory;
    BookingHistory history;

    Set<String> allocatedRooms = new HashSet<>();
    HashMap<String, Reservation> confirmedBookings = new HashMap<>();

    int counter = 1;

    BookingService(RoomInventory inv, BookingHistory hist) {
        inventory = inv;
        history = hist;
    }

    synchronized void processBooking(Reservation r) {
        try {
            Validator.validate(r, inventory);

            String roomId = r.roomType + "-" + counter++;
            inventory.reduce(r.roomType);

            r.roomId = roomId;

            allocatedRooms.add(roomId);
            confirmedBookings.put(roomId, r);
            history.add(r);

            System.out.println(Thread.currentThread().getName() +
                    " SUCCESS -> " + r.guestName + " booked " + roomId);

        } catch (Exception e) {
            System.out.println(Thread.currentThread().getName() +
                    " FAILED -> " + e.getMessage());
        }
    }
}

// ================= THREAD WORKER =================
class BookingWorker extends Thread {
    BookingQueue queue;
    BookingService service;

    BookingWorker(String name, BookingQueue q, BookingService s) {
        super(name);
        queue = q;
        service = s;
    }

    public void run() {
        while (true) {
            Reservation r;

            synchronized (queue) {
                if (queue.isEmpty()) break;
                r = queue.poll();
            }

            if (r != null) {
                service.processBooking(r);
            }
        }
    }
}

// ================= CANCELLATION =================
class CancellationService {
    RoomInventory inventory;
    HashMap<String, Reservation> bookings;
    Stack<String> cancelStack = new Stack<>();

    CancellationService(RoomInventory inv, HashMap<String, Reservation> b) {
        inventory = inv;
        bookings = b;
    }

    void cancelBooking(String roomId) {
        if (!bookings.containsKey(roomId)) {
            System.out.println("Cancellation Failed: Not found");
            return;
        }

        Reservation r = bookings.get(roomId);

        if (r.cancelled) {
            System.out.println("Already cancelled");
            return;
        }

        cancelStack.push(roomId);
        inventory.increase(r.roomType);
        r.cancelled = true;

        System.out.println("Cancelled -> " + roomId);
    }
}

// ================= REPORT =================
class ReportService {
    void generate(List<Reservation> list) {
        System.out.println("\n===== BOOKING REPORT =====");
        for (Reservation r : list) {
            System.out.println(r.guestName + " | " + r.roomType + " | " + r.roomId +
                    (r.cancelled ? " (Cancelled)" : ""));
        }
    }
}

// ================= PERSISTENCE =================
class SystemState implements Serializable {
    List<Reservation> bookings;
    HashMap<String, Integer> inventory;

    SystemState(List<Reservation> b, HashMap<String, Integer> i) {
        bookings = b;
        inventory = i;
    }
}

class PersistenceService {
    private static final String FILE_NAME = "hotel_data.ser";

    static void save(SystemState state) {
        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {

            out.writeObject(state);
            System.out.println("Data Saved Successfully");

        } catch (Exception e) {
            System.out.println("Error Saving Data");
        }
    }

    static SystemState load() {
        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(FILE_NAME))) {

            SystemState state = (SystemState) in.readObject();
            System.out.println("Data Loaded Successfully");
            return state;

        } catch (Exception e) {
            System.out.println("No Previous Data Found (Fresh Start)");
            return null;
        }
    }
}

// ================= MAIN =================
public class BookMyStayApp {
    public static void main(String[] args) throws Exception {

        System.out.println("===== SYSTEM START =====");

        // LOAD PREVIOUS STATE
        SystemState state = PersistenceService.load();

        RoomInventory inventory = new RoomInventory();
        BookingHistory history = new BookingHistory();

        if (state != null) {
            history.list = state.bookings;
            inventory.inventory = state.inventory;
        }

        // CREATE QUEUE
        BookingQueue queue = new BookingQueue();

        queue.add(new Reservation("Ujjwal", "Single"));
        queue.add(new Reservation("Rahul", "Single"));
        queue.add(new Reservation("Amit", "Double"));
        queue.add(new Reservation("Neha", "Suite"));

        // PROCESS BOOKINGS (MULTI-THREAD)
        BookingService service = new BookingService(inventory, history);

        BookingWorker t1 = new BookingWorker("Thread-1", queue, service);
        BookingWorker t2 = new BookingWorker("Thread-2", queue, service);

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        // CANCEL ONE BOOKING
        CancellationService cancelService =
                new CancellationService(inventory, service.confirmedBookings);

        for (String id : service.confirmedBookings.keySet()) {
            cancelService.cancelBooking(id);
            break;
        }

        // REPORT
        ReportService report = new ReportService();
        report.generate(history.getAll());

        // SAVE STATE
        PersistenceService.save(
                new SystemState(history.getAll(), inventory.inventory)
        );

        System.out.println("===== SYSTEM END =====");
    }
}