import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class client {

    private JFrame frame;
    private JComboBox<String> actionComboBox;
    private JTextField classNameField;
    private JTextField dateField;
    private JTextField timeField;
    private JTextField roomField;
    private JTextArea outputArea;
    private PrintWriter serverOutput;
    private BufferedReader serverInput;
    private Socket socket;

    // Server connection details
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 1234;

    public client() {
        initializeGUI();
        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            serverOutput = new PrintWriter(socket.getOutputStream(), true);
            serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputArea.setText("Connected to server.\n");
        } catch (Exception e) {
            outputArea.setText("Failed to connect to the server.\n");
            e.printStackTrace();
        }
    }

    private void sendCommandToServer(String command) {
        try {
            serverOutput.println(command);
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = serverInput.readLine()) != null && !responseLine.isEmpty()) {
                response.append(responseLine).append("\n");
            }
            outputArea.setText(response.toString());
        } catch (Exception e) {
            outputArea.setText("Error sending command to server.\n");
            e.printStackTrace();
        }
    }

    private void initializeGUI() {
        frame = new JFrame("Class Scheduler Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        // Center align the components within a panel that uses FlowLayout
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        // Panel that holds the action selection components
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));

        JLabel actionLabel = new JLabel("Action:");
        actionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionPanel.add(actionLabel);

        actionComboBox = new JComboBox<>(new String[]{"Add Class", "Remove Class", "Display Schedule"});
        actionComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, actionComboBox.getPreferredSize().height));
        actionComboBox.addActionListener(this::actionSelected);
        actionPanel.add(actionComboBox);

        JLabel classNameLabel = new JLabel("Class Name:");
        classNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionPanel.add(classNameLabel);

        classNameField = new JTextField(10);
        classNameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        classNameField.setMaximumSize(classNameField.getPreferredSize());
        actionPanel.add(classNameField);

        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionPanel.add(dateLabel);

        dateField = new JTextField(10);
        dateField.setAlignmentX(Component.CENTER_ALIGNMENT);
        dateField.setMaximumSize(dateField.getPreferredSize());
        actionPanel.add(dateField);

        JLabel timeLabel = new JLabel("Time:");
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionPanel.add(timeLabel);

        timeField = new JTextField(10);
        timeField.setAlignmentX(Component.CENTER_ALIGNMENT);
        timeField.setMaximumSize(timeField.getPreferredSize());
        actionPanel.add(timeField);

        JLabel roomLabel = new JLabel("Room:");
        roomLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionPanel.add(roomLabel);

        roomField = new JTextField(10);
        roomField.setAlignmentX(Component.CENTER_ALIGNMENT);
        roomField.setMaximumSize(roomField.getPreferredSize());
        actionPanel.add(roomField);

        JButton executeButton = new JButton("Execute");
        executeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        executeButton.addActionListener(this::executeAction);
        actionPanel.add(executeButton);

        JButton terminateButton = new JButton("Terminate");
        terminateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        terminateButton.addActionListener(e -> terminateApplication());
        actionPanel.add(terminateButton);

        centerPanel.add(actionPanel);
        frame.add(centerPanel);

        outputArea = new JTextArea(10, 30);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        frame.add(scrollPane);

        frame.pack();
        frame.setLocationRelativeTo(null); // Center the frame on the screen
        frame.setVisible(true);

        // Initial setup for action selection
        actionSelected(null);
    }

    private void terminateApplication() {
        // Close socket and other resources here before exiting
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Exit the application
        System.exit(0);
    }
    private void actionSelected(ActionEvent e) {
        String selectedAction = (String) actionComboBox.getSelectedItem();
        boolean isAddClass = "Add Class".equals(selectedAction);

        // Show or hide fields based on the selected action
        dateField.setVisible(isAddClass);
        timeField.setVisible(isAddClass);
        roomField.setVisible(isAddClass);

        // Repack the frame to adjust to the layout changes
        frame.pack();
    }



    private void executeAction(ActionEvent e) {
        new Thread(() -> {
            String selectedAction = (String) actionComboBox.getSelectedItem();
            String className = classNameField.getText();
            String command = "";

            switch (selectedAction) {
                case "Add Class":
                    String date = dateField.getText();
                    String time = timeField.getText();
                    String room = roomField.getText();
                    command = "add," + className + "," + date + "," + time + "," + room;
                    break;
                case "Remove Class":
                    command = "remove," + className;
                    break;
                case "Display Schedule":
                    command = "display," + className;
                    break;
            }
            sendCommandToServer(command);
        }).start();
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(client::new);
    }
}
