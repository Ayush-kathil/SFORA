/**
 * Main application runner for SFORA.
 * Keeps things unbelievably simple.
 */
public class Main {
    public static void main(String[] args) {
        // Just launch the UI. The UI handles the rest of the flow.
        ConsoleUI app = new ConsoleUI();
        app.start();
    }
}
