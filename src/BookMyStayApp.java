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

// Concrete Classes
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

// MAIN CLASS (ONLY PUBLIC CLASS)
public class BookMyStayApp {
    public static void main(String[] args) {

        System.out.println("Book My Stay App v2.0");

        // Create Room Objects
        Room single = new SingleRoom();
        Room doub = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Static Availability (IMPORTANT PART)
        int singleAvailable = 5;
        int doubleAvailable = 3;
        int suiteAvailable = 2;

        // Display
        System.out.println("\nSingle Room:");
        single.displayRoom();
        System.out.println("Available: " + singleAvailable);

        System.out.println("\nDouble Room:");
        doub.displayRoom();
        System.out.println("Available: " + doubleAvailable);

        System.out.println("\nSuite Room:");
        suite.displayRoom();
        System.out.println("Available: " + suiteAvailable);
    }
}