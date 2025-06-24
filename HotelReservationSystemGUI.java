import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class HotelReservationSystemGUI extends JFrame {

    static class Room {
        int number;
        String type;
        boolean available;

        Room(int number, String type) {
            this.number = number;
            this.type = type;
            this.available = true;
        }
    }

    static class Booking {
        String name;
        int roomNumber;
        String roomType;
        double amount;

        Booking(String name, int roomNumber, String roomType, double amount) {
            this.name = name;
            this.roomNumber = roomNumber;
            this.roomType = roomType;
            this.amount = amount;
        }

        @Override
        public String toString() {
            return name + "," + roomNumber + "," + roomType + "," + amount;
        }
    }

    ArrayList<Room> rooms = new ArrayList<>();
    ArrayList<Booking> bookings = new ArrayList<>();
    DefaultTableModel bookingTableModel;

    public HotelReservationSystemGUI() {
        setTitle("Hotel Reservation System");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top: Input Panel
        JPanel inputPanel = new JPanel();
        JTextField nameField = new JTextField(10);
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Standard", "Deluxe", "Suite"});
        JButton bookBtn = new JButton("Book Room");
        JButton cancelBtn = new JButton("Cancel Reservation");

        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Room Type:"));
        inputPanel.add(typeBox);
        inputPanel.add(bookBtn);
        inputPanel.add(cancelBtn);
        add(inputPanel, BorderLayout.NORTH);

        // Center: Booking Table
        bookingTableModel = new DefaultTableModel(new Object[]{"Name", "Room", "Type", "Amount"}, 0);
        JTable bookingTable = new JTable(bookingTableModel);
        add(new JScrollPane(bookingTable), BorderLayout.CENTER);

        // Load data
        loadRooms();
        loadBookings();

        // Book Button
        bookBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String type = (String) typeBox.getSelectedItem();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a name.");
                return;
            }

            Room available = null;
            for (Room r : rooms) {
                if (r.type.equalsIgnoreCase(type) && r.available) {
                    available = r;
                    break;
                }
            }

            if (available == null) {
                JOptionPane.showMessageDialog(this, "No available rooms in " + type + " category.");
                return;
            }

            double amount = switch (type.toLowerCase()) {
                case "standard" -> 1000;
                case "deluxe" -> 2000;
                case "suite" -> 3000;
                default -> 0;
            };

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Payment Amount: ₹" + amount + "\nProceed?", "Payment Confirmation",
                    JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) return;

            available.available = false;
            Booking b = new Booking(name, available.number, type, amount);
            bookings.add(b);
            bookingTableModel.addRow(new Object[]{b.name, b.roomNumber, b.roomType, b.amount});
            nameField.setText("");
            saveBookings();
        });

        // Cancel Button
        cancelBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Enter name to cancel:");
            if (name == null || name.trim().isEmpty()) return;

            Booking toRemove = null;
            for (Booking b : bookings) {
                if (b.name.equalsIgnoreCase(name.trim())) {
                    toRemove = b;
                    break;
                }
            }

            if (toRemove == null) {
                JOptionPane.showMessageDialog(this, "Booking not found.");
                return;
            }

            for (Room r : rooms) {
                if (r.number == toRemove.roomNumber) {
                    r.available = true;
                    break;
                }
            }

            bookings.remove(toRemove);
            refreshTable();
            saveBookings();
            JOptionPane.showMessageDialog(this, "Booking for " + toRemove.name + " canceled.");
        });

        setVisible(true);
    }

    void refreshTable() {
        bookingTableModel.setRowCount(0);
        for (Booking b : bookings) {
            bookingTableModel.addRow(new Object[]{b.name, b.roomNumber, b.roomType, b.amount});
        }
    }

    void loadRooms() {
        for (int i = 101; i <= 105; i++) rooms.add(new Room(i, "Standard"));
        for (int i = 201; i <= 203; i++) rooms.add(new Room(i, "Deluxe"));
        for (int i = 301; i <= 302; i++) rooms.add(new Room(i, "Suite"));
    }

    void loadBookings() {
        File file = new File("bookings.txt");
        if (!file.exists()) return;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String[] data = scanner.nextLine().split(",");
                Booking b = new Booking(data[0], Integer.parseInt(data[1]), data[2], Double.parseDouble(data[3]));
                bookings.add(b);
                for (Room r : rooms) {
                    if (r.number == b.roomNumber) r.available = false;
                }
                bookingTableModel.addRow(new Object[]{b.name, b.roomNumber, b.roomType, b.amount});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading bookings: " + e.getMessage());
        }
    }

    void saveBookings() {
        try (PrintWriter out = new PrintWriter("bookings.txt")) {
            for (Booking b : bookings) {
                out.println(b);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving bookings.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HotelReservationSystemGUI::new);
    }
}