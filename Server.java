import java.io.*;
import java.net.*;
import java.util.*;

class ClassSchedule implements Serializable {
    String className; // Added class name
    String date;
    String time;
    String room;

    public ClassSchedule(String className, String date, String time, String room) {
        this.className = className;
        this.date = date;
        this.time = time;
        this.room = room;
    }

    @Override
    public String toString() {
        return className + " on " + date + " " + time + " in Room " + room;
    }
}

public class Server {
    private static final int PORT = 1234;
    private static List<ClassSchedule> schedules = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        loadSchedules();
        Socket link = null;
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started. Listening on Port " + PORT);
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        handleClient(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace(); // Or any other logging mechanism
                    } finally {
                        try {
                            clientSocket.close(); // Ensure the socket is closed after handling
                        } catch (IOException e) {
                            e.printStackTrace(); // Log if closing the socket fails
                        }
                    }
                }).start();
            } catch (IOException e) {
                e.printStackTrace(); // Log exception related to accepting a new connection
                // Optionally, break or return from the loop if the server socket fails
            }
        }
    }
    public static class IncorrectActionException extends Exception {
        public IncorrectActionException(String message) {
            super(message);
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                try {
                    System.out.println("Received from client: " + inputLine);
                    String[] commands = inputLine.split(",");
                    switch (commands[0].toLowerCase()) {
                        case "add":
                            addClass(new ClassSchedule(commands[1], commands[2], commands[3], commands[4]), out);
                            break;
                        case "remove":
                            removeClass(commands[1], out);
                            break;
                        case "display":
                            displaySchedule(commands[1], out);
                            break;
                        case "stop":
                            saveSchedules(); // Save schedules to file when the server stops

                            break; // Added break for consistency
                        default:
                            throw new Server.IncorrectActionException("Invalid command" + commands[0]);
                    }
                } catch (IncorrectActionException e) {
                    out.println(e.getMessage());
                    out.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle IOException separately
        }
    }

    private static void addClass(ClassSchedule newSchedule, PrintWriter out) {
        boolean isClash = schedules.stream().anyMatch(schedule ->
                schedule.date.equals(newSchedule.date) &&
                        schedule.time.equals(newSchedule.time));
            if (isClash) {
                out.println("Error: There is already a class at this time.");
                out.flush(); // Ensure the data is sent immediately
                return;
            }

        schedules.add(newSchedule);
        out.println("Class added successfully: " + newSchedule);
        out.flush(); // Ensure the data is sent immediately
    }

    private static void removeClass(String className, PrintWriter out) {
        boolean found = schedules.removeIf(schedule -> schedule.className.equals(className));
        if (found) {
            out.println("Class removed: " + className);
        } else {
            out.println("Error: Class not found.");
        }
        out.flush(); // Ensure the data is sent immediately
    }

    private static void displaySchedule(String className, PrintWriter out) {
        StringBuilder scheduleStr = new StringBuilder();
        schedules.stream().filter(schedule -> schedule.className.equals(className)).forEach(schedule -> scheduleStr.append(schedule).append("\n"));
        if (scheduleStr.length() > 0) {
            out.println("Schedule for " + className + ":\n" + scheduleStr);
        } else {
            out.println("No schedule found for " + className);
        }
        out.flush(); // Ensure the data is sent immediately
    }

    private static final String CSV_FILE = "schedules.csv"; // File to save schedules in CSV format

    private static void saveSchedules() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
            for (ClassSchedule schedule : schedules) {
                writer.println(schedule.className + "," + schedule.date + "," + schedule.time + "," + schedule.room);
            }
            System.out.println("Schedules saved to CSV file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadSchedules() {
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    schedules.add(new ClassSchedule(parts[0], parts[1], parts[2], parts[3]));
                } else {
                    System.out.println("Invalid line in CSV file: " + line);
                }
            }
            System.out.println("Schedules loaded from CSV file.");
        } catch (IOException e) {
            System.out.println("No existing CSV schedules file found. Starting with an empty schedule list.");
        }
    }
}
