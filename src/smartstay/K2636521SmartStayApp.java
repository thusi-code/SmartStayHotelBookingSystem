package smartstay;

import smartstay.model.K2636521BedType;
import smartstay.model.K2636521BookingStatus;
import smartstay.model.K2636521DeluxeRoom;
import smartstay.model.K2636521FamilySuiteRoom;
import smartstay.model.K2636521Guest;
import smartstay.model.K2636521GuestType;
import smartstay.model.K2636521Invoice;
import smartstay.model.K2636521Reservation;
import smartstay.model.K2636521Room;
import smartstay.model.K2636521RoomStatus;
import smartstay.model.K2636521RoomType;
import smartstay.model.K2636521StandardRoom;
import smartstay.model.K2636521SuiteRoom;
import smartstay.service.K2636521BookingService;
import smartstay.service.K2636521InvoiceService;
import smartstay.service.K2636521RoomService;
import smartstay.util.K2636521InputValidator;
import smartstay.model.K2636521InvoiceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.math.BigDecimal;
import java.util.function.Function;

/* Runs the SmartStay Hotel Booking System through a CLI menu. */
public class K2636521SmartStayApp {

    private final Scanner scanner;
    private final K2636521RoomService roomService;
    private final K2636521BookingService bookingService;
    private final K2636521InvoiceService invoiceService;

    private final List<K2636521Guest> guests;
    private final Map<String, String> guestPasswords;

    private K2636521Guest loggedInGuest;

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /* Initializes the system services and account collections. */
    public K2636521SmartStayApp() {
        scanner = new Scanner(System.in);

        roomService = new K2636521RoomService();
        bookingService = new K2636521BookingService(roomService);
        invoiceService = new K2636521InvoiceService();

        guests = new ArrayList<>();
        guestPasswords = new HashMap<>();

        loggedInGuest = null;
    }

    // Starts the application.
    public static void main(String[] args) {
        K2636521SmartStayApp app = new K2636521SmartStayApp();
        app.run();
    }

    // Displays the welcome menu.
    public void run() {
        boolean running = true;

        while (running) {
            System.out.println("\n==================================");
            System.out.println("  SMARTSTAY HOTEL BOOKING SYSTEM");
            System.out.println("==================================");
            System.out.println("1. Admin");
            System.out.println("2. Guest");
            System.out.println("0. Exit");

            int choice = readInteger("Select your role: ");

            switch (choice) {
                case 1:
                    adminLogin();
                    break;

                case 2:
                    guestAccess();
                    break;

                case 0:
                    running = false;
                    System.out.println("Thank you for using SmartStay.");
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }

        scanner.close();
    }

    // Validates the administrator's login credentials.
    private void adminLogin() {
        System.out.println("\n========== ADMIN LOGIN ==========");

        String username = readNonBlankInput("Enter username: ");
        String password = readNonBlankInput("Enter password: ");

        if (ADMIN_USERNAME.equals(username)
                && ADMIN_PASSWORD.equals(password)) {

            System.out.println("Admin login successful.");
            adminMenu();

        } else {
            System.out.println("Invalid admin username or password.");
        }
    }

    // Displays the guest registration and login menu.
    private void guestAccess() {
        boolean running = true;

        while (running) {
            System.out.println("\n========== GUEST ACCESS ==========");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("0. Back");

            int choice = readInteger("Enter your choice: ");

            switch (choice) {
                case 1:
                    guestRegister();
                    break;

                case 2:
                    if (guestLogin()) {
                        guestMenu();
                    }
                    break;

                case 0:
                    running = false;
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // Registers a new guest account.
    private void guestRegister() {
        System.out.println("\n======= GUEST REGISTRATION =======");

        K2636521GuestType guestType = readGuestType();
        String fullName = readNonBlankInput("Enter full name: ");
        String phone = readValidPhone();
        String email = readValidEmail();

        String nic = "";
        String passport = "";

        if (guestType == K2636521GuestType.LOCAL) {

            while (nic.isEmpty() && passport.isEmpty()) {
                nic = readOptionalNIC();
                passport = readOptionalPassport();

                if (nic.isEmpty() && passport.isEmpty()) {
                    System.out.println(
                            "Please provide a valid NIC or passport.");
                }
            }

        } else {
            passport = readRequiredPassport();
        }

        String password = readValidPassword();

        String guestId = generateNextGuestId ();

        K2636521Guest guest = new K2636521Guest(
                guestId,
                guestType.name(),
                fullName,
                phone,
                email,
                nic,
                passport
        );

        guests.add(guest);
        guestPasswords.put(guestId, password);

        System.out.println("\nGuest registration successful.");
        System.out.println("Your Guest ID: " + guestId);
        System.out.println("You can now log in using your Guest ID.");
    }

    // Reads the guest type.
    private K2636521GuestType readGuestType() {
        System.out.println("1. Local");
        System.out.println("2. International");

        while (true) {
            int choice = readInteger("Select guest type: ");

            if (choice == 1 || choice == 2) {
                return K2636521GuestType.values()[choice - 1];
            }

            System.out.println("Invalid guest type. Select 1 or 2.");
        }
    }

    // Reads and validates a unique phone number.
    private String readValidPhone() {
        while (true) {
            String phone = readNonBlankInput("Enter contact number: ");

            if (!K2636521InputValidator.isValidPhoneNumber(phone)) {
                System.out.println("Invalid phone number format.");
                continue;
            }

            if (isDuplicate(phone, K2636521Guest::getContactNumber, true)) {
                System.out.println("This phone number is already registered.");
                continue;
            }

            return phone;
        }
    }

    // Reads and validates a unique email address. 
    private String readValidEmail() {
        while (true) {
            String email = readNonBlankInput("Enter email address: ");

            if (!K2636521InputValidator.isValidEmail(email)) {
                System.out.println("Invalid email format.");
                continue;
            }

            if (isDuplicate(email, K2636521Guest::getEmail, false)) {
                System.out.println("This email is already registered.");
                continue;
            }

            return email;
        }
    }

    // Reads an optional NIC number.
    private String readOptionalNIC() {
        while (true) {
            System.out.print("Enter NIC number (if available): ");
            String nic = scanner.nextLine().trim();

            if (nic.isEmpty()) {
                return "";
            }

            if (!K2636521InputValidator.isValidSriLankanNIC(nic)) {
                System.out.println("Invalid NIC format.");
                continue;
            }

            if (isDuplicate(nic, K2636521Guest::getNicNumber, false)) {
                System.out.println("This NIC is already registered.");
                continue;
            }

            return nic;
        }
    }

    // Reads an optional passport number.
    private String readOptionalPassport() {
        while (true) {
            System.out.print("Enter passport number (if available): ");
            String passport = scanner.nextLine().trim();

            if (passport.isEmpty()) {
                return "";
            }

            if (!K2636521InputValidator.isValidPassportNumber(passport)) {
                System.out.println("Invalid passport format.");
                continue;
            }

            if (isDuplicate(passport, K2636521Guest::getPassportNumber, false)) {
                System.out.println("This passport is already registered.");
                continue;
            }

            return passport;
        }
    }

    // Reads a mandatory passport number.
    private String readRequiredPassport() {
        while (true) {
            String passport = readNonBlankInput("Enter passport number: ");

            if (!K2636521InputValidator.isValidPassportNumber(passport)) {
                System.out.println("Invalid passport format.");
                continue;
            }

            if (isDuplicate(passport, K2636521Guest::getPassportNumber, false)) {
                System.out.println("This passport is already registered.");
                continue;
            }

            return passport;
        }
    }

    // Reads and validates a password.
    private String readValidPassword() {
        while (true) {
            String password = readNonBlankInput(
                    "Create password (minimum 6 characters): ");

            if (password.length() >= 6) {
                return password;
            }

            System.out.println(
                    "Password must contain at least 6 characters.");
        }
    }

    // genarate a unique guest ID based on the highest existing ID.
    private String generateNextGuestId () {
        int highestId = 0;

        for (K2636521Guest guest : guests) {
            String id = guest.getGuestId();

            if (id != null && id.matches("G\\d+")) {
                int number = Integer.parseInt(id.substring(1));
                highestId = Math.max(highestId, number);
            }
        }

        return String.format("G%03d", highestId + 1);
    }

    // Checks whether a value is already registered.
    private boolean isDuplicate(
            String value,
            Function<K2636521Guest, String> extractor,
            boolean isPhone) {

        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        String newValue = isPhone
                ? normalizePhone(value)
                : value.trim();

        for (K2636521Guest guest : guests) {
            String existingValue = extractor.apply(guest);

            if (existingValue == null || existingValue.trim().isEmpty()) {
                continue;
            }

            String comparableValue = isPhone
                    ? normalizePhone(existingValue)
                    : existingValue.trim();

            if (comparableValue.equalsIgnoreCase(newValue)) {
                return true;
            }
        }

        return false;
    }

    // Normalizes a phone number for comparison.
    private String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }

        String digits = phone.replaceAll("[^0-9]", "");

        if (digits.length() == 10 && digits.startsWith("0")) {
            digits = "94" + digits.substring(1);
        }

        return digits;
    }

    // Authenticates a registered guest.
    private boolean guestLogin() {
        System.out.println("\n========== GUEST LOGIN ==========");

        String guestId = readNonBlankInput("Enter guest ID: ");
        String password = readNonBlankInput("Enter password: ");

        K2636521Guest guest = findGuestById(guestId);

        if (guest == null
                || !password.equals(guestPasswords.get(guestId.trim()))) {
            System.out.println("Invalid guest ID or password.");
            return false;
        }

        loggedInGuest = guest;

        System.out.println("Login successful.");
        System.out.println("Welcome, " + loggedInGuest.getFullName() + "!");

        return true;
    }

    // Displays a formatted menu heading.
    private void printMenuHeader(String title) {
        System.out.println("\n========================================");
        System.out.printf("%" + ((40 + title.length()) / 2) + "s%n", title);
        System.out.println("========================================");
    }

    /* Displays the administrator's main menu. */
    private void adminMenu() {
        boolean running = true;

        while (running) {
            printMenuHeader("ADMIN MENU");
            System.out.println("1. Room Management");
            System.out.println("2. Booking Management");
            System.out.println("3. Guest Management");
            System.out.println("4. Invoice Management");
            System.out.println("0. Logout");

            int choice = readInteger("Enter your choice: ");

            switch (choice) {
                case 1:
                    roomManagementMenu();
                    break;
                case 2:
                    bookingManagementMenu();
                    break;
                case 3:
                    guestManagementMenu();
                    break;
                case 4:
                    invoiceManagementMenu();
                    break;
                case 0:
                    running = false;
                    System.out.println("Admin logged out.");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    /* Displays room management options. */
    private void roomManagementMenu() {
        boolean running = true;

        while (running) {
            printMenuHeader("ROOM MANAGEMENT");
            System.out.println("1. Add Room");
            System.out.println("2. View All Rooms");
            System.out.println("3. Update Room Details");
            System.out.println("4. Remove Room");
            System.out.println("5. Update Room Availability Status");
            System.out.println("6. View Room Status by Date");
            System.out.println("0. Back to Admin Menu");

            int choice = readInteger("Enter your choice: ");

            switch (choice) {
                case 1:
                    addRoom();
                    break;
                case 2:
                    displayAllRooms(roomService.getAllRooms());
                    break;
                case 3:
                    updateRoomDetails();
                    break;
                case 4:
                    removeRoom();
                    break;
                case 5:
                    updateRoomStatus();
                    break;
                case 6:
                    viewRoomStatusByDate();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    /* Displays booking management options. */
    private void bookingManagementMenu() {
        boolean running = true;

        while (running) {
            printMenuHeader("BOOKING MANAGEMENT");
            System.out.println("1. View All Bookings");
            System.out.println("2. Search Booking");
            System.out.println("3. Cancel Booking");
            System.out.println("4. Complete Stay and View Invoice");
            System.out.println("0. Back to Admin Menu");

            int choice = readInteger("Enter your choice: ");

            switch (choice) {
                case 1:
                    displayAllBookings();
                    break;
                case 2:
                    searchBookingAsAdmin();
                    break;
                case 3:
                    cancelBookingAsAdmin();
                    break;
                case 4:
                    completeStayAndGenerateInvoice();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    /* Displays guest management options. */
    private void guestManagementMenu() {
        boolean running = true;

        while (running) {
            printMenuHeader("GUEST MANAGEMENT");
            System.out.println("1. View All Guests");
            System.out.println("0. Back to Admin Menu");

            int choice = readInteger("Enter your choice: ");

            switch (choice) {
                case 1:
                    displayAllGuests();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    /* Displays invoice management options. */
    private void invoiceManagementMenu() {
        boolean running = true;

        while (running) {
            printMenuHeader("INVOICE MANAGEMENT");
            System.out.println("1. View All Invoices");
            System.out.println("0. Back to Admin Menu");

            int choice = readInteger("Enter your choice: ");

            switch (choice) {
                case 1:
                    displayAllInvoices();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // Adds a new room with an automatically generated room ID.
    private void addRoom() {
        System.out.println("\n--------------- Add Room ---------------");
        K2636521RoomType roomType = readRoomType();
        K2636521BedType bedType = readBedType();

        int capacity;

        while (true) {
            capacity = readInteger("Enter maximum guest capacity: ");

            if (K2636521InputValidator.isPositive(capacity)) {
                break;
            }

            System.out.println("Capacity must be greater than zero.");
        }

        String roomId = roomService.generateRoomId(roomType);

        K2636521Room room;

        switch (roomType) {
            case STANDARD:
                room = new K2636521StandardRoom(
                        roomId, bedType,
                        K2636521RoomStatus.AVAILABLE, capacity);
                break;

            case DELUXE:
                room = new K2636521DeluxeRoom(
                        roomId, bedType,
                        K2636521RoomStatus.AVAILABLE, capacity);
                break;

            case SUITE:
                room = new K2636521SuiteRoom(
                        roomId, bedType,
                        K2636521RoomStatus.AVAILABLE, capacity);
                break;

            default:
                room = new K2636521FamilySuiteRoom(
                        roomId, bedType,
                        K2636521RoomStatus.AVAILABLE, capacity);
                break;
        }

        if (roomService.addRoom(room)) {
            System.out.println("\n----------------------------------------");
            System.out.println("Room added successfully.");
            System.out.println("Generated Room ID: " + roomId);
            System.out.println("----------------------------------------");
        } else {
            System.out.println("\n----------------------------------------");
            System.out.println("Unable to add room.");
            System.out.println("----------------------------------------");
        }
    }

    /* Updates the bed type and capacity of a room. */
    private void updateRoomDetails() {
        System.out.println("\n---------- Update Room Details ----------");

        String roomId = readNonBlankInput("Enter Room ID: ");

        K2636521Room room = roomService.findRoomById(roomId);

        if (room == null) {
            System.out.println("Room not found.");
            return;
        }

        System.out.println("\nCurrent Room Details:");

        System.out.printf("%-12s %-15s %-12s %-10s %-20s %-12s%n",
                "Room ID", "Room Type", "Bed Type", "Capacity",
                "Room Status", "Daily Rate");

        System.out.println("--------------------------------------------------------------------------------------");

        System.out.printf("%-12s %-15s %-12s %-10d %-20s LKR %-10.2f%n",
                room.getRoomId(), room.getRoomType(), room.getBedType(),
                room.getCapacity(), room.getStatus(), room.getDailyRate());

        System.out.println("\nAvailable Bed Types:");
        System.out.println("1. Single");
        System.out.println("2. Double");
        System.out.println("3. Queen");
        System.out.println("4. King");

        int bedChoice = readInteger("Select new Bed Type: ");

        if (bedChoice < 1 || bedChoice > 4) {
            System.out.println("Invalid bed type. Please select 1-4.");
            return;
        }

        K2636521BedType newBedType =
                K2636521BedType.values()[bedChoice - 1];

        int newCapacity = readInteger("Enter new Room Capacity: ");

        if (!K2636521InputValidator.isPositive(newCapacity)) {
            System.out.println("Capacity must be greater than zero.");
            return;
        }

        if (newCapacity < room.getCapacity()
                && bookingService.hasBookingsForRoom(roomId)) {
            System.out.println(
                    "Cannot reduce capacity because this room has booking history.");
            return;
        }

        boolean updated = roomService.updateRoomDetails(
                roomId, newBedType, newCapacity);

        if (updated) {
            System.out.println("\nRoom details updated successfully.");

            System.out.println("\nUpdated Room Details:");

            System.out.printf("%-12s %-15s %-12s %-10s %-20s %-12s%n",
                    "Room ID", "Room Type", "Bed Type", "Capacity",
                    "Room Status", "Daily Rate");

            System.out.println("--------------------------------------------------------------------------------------");

            System.out.printf("%-12s %-15s %-12s %-10d %-20s LKR %-10.2f%n",
                    room.getRoomId(), room.getRoomType(), room.getBedType(),
                    room.getCapacity(), room.getStatus(), room.getDailyRate());

        } else {
            System.out.println("Unable to update room details.");
        }
    }

    /* Removes a room if it has no booking history. */
    private void removeRoom() {
        System.out.println("\n------------- Remove Room -------------");
        String roomId = readNonBlankInput("Enter Room ID: ");
        K2636521Room room = roomService.findRoomById(roomId);

        if (room == null) {
            System.out.println("Room not found.");
            return;
        }

        if (bookingService.hasBookingsForRoom(roomId)) {
            System.out.println(
                    "Cannot remove this room because it has booking history.");
            return;
        }
        System.out.println("\nRoom Details:");
        System.out.println("Room ID: " + room.getRoomId());
        System.out.println("Room Type: " + room.getRoomType());
        System.out.println("Bed Type: " + room.getBedType());
        System.out.println("Capacity: " + room.getCapacity());

        String confirmation = readNonBlankInput(
                "\nAre you sure you want to remove this room? (Y/N): ");

        if (confirmation.equalsIgnoreCase("Y")) {
            if (roomService.removeRoom(roomId)) {
                System.out.println("Room removed successfully.");
            } else {
                System.out.println("Unable to remove room.");
            }
        } else if (confirmation.equalsIgnoreCase("N")) {
            System.out.println("Room removal cancelled.");
        } else {
            System.out.println("Invalid confirmation. Room not removed.");
        }
    }

    // Displays the authenticated guest's menu.
    private void guestMenu() {
        boolean running = true;

        while (running && loggedInGuest != null) {
            printMenuHeader("ROOM MANAGEMENT");
            System.out.println("Logged in as: "
                    + loggedInGuest.getFullName());
            System.out.println("1. Search Available Rooms");
            System.out.println("2. Create Booking");
            System.out.println("3. View My Bookings");
            System.out.println("4. Search My Booking");
            System.out.println("5. Modify My Booking");
            System.out.println("6. Cancel My Booking");
            System.out.println("7. View My Invoices");
            System.out.println("0. Logout");

            int choice = readInteger("Enter your choice: ");

            switch (choice) {
                case 1:
                    searchAvailableRooms();
                    break;

                case 2:
                    createBooking();
                    break;

                case 3:
                    viewMyBookingsMenu();
                    break;

                case 4:
                    searchMyBooking();
                    break;

                case 5:
                    modifyMyBooking();
                    break;

                case 6:
                    cancelMyBooking();
                    break;

                case 7:
                    displayMyInvoices();
                    break;

                case 0:
                    loggedInGuest = null;
                    running = false;
                    System.out.println("Guest logged out.");
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }

        loggedInGuest = null;
    }

    private static class BookingSearchResult {

        private LocalDateTime checkIn;
        private LocalDateTime checkOut;
        private List<K2636521Room> availableRooms;

        public BookingSearchResult(
                LocalDateTime checkIn,
                LocalDateTime checkOut,
                List<K2636521Room> availableRooms) {

            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.availableRooms = availableRooms;
        }
    }

    // Searches available rooms by room type and selected dates.
    private BookingSearchResult searchAvailableRoomsForSelectedCriteria() {

        K2636521RoomType roomType = readRoomType();

        LocalDateTime checkIn = readCheckInDateTime();
        LocalDateTime checkOut = readCheckOutDateTime(checkIn);

        List<K2636521Room> availableRooms =
                bookingService.getAvailableRooms(
                        checkIn, checkOut, roomType);

        System.out.println("\nRoom Type: " + roomType);
        System.out.println("Check-in: " + checkIn);
        System.out.println("Check-out: " + checkOut);

        if (availableRooms.isEmpty()) {
            System.out.println(
                    "No " + roomType
                    + " rooms are available for the selected dates.");
        } else {
            System.out.println("\nAvailable rooms:");
            displayAvailableRoomsTable(availableRooms);
        }

        return new BookingSearchResult(
            checkIn, checkOut, availableRooms);
    }

    // Searches available rooms by room type and selected dates.
    private void searchAvailableRooms() {

        System.out.println("\n----- Search Available Rooms -----");

        searchAvailableRoomsForSelectedCriteria();
    }

    /* Displays the guest's bookings for a selected check-in date. */
    private void displayMyBookingsByCheckInDate() {
        System.out.println("\n--- View Bookings by Check-in Date ---");

        LocalDate selectedDate;

        while (true) {
            System.out.print("Enter check-in date (yyyy-MM-dd): ");
            String input = scanner.nextLine().trim();

            try {
                selectedDate = LocalDate.parse(input);
                break;
            } catch (DateTimeParseException exception) {
                System.out.println("Invalid date. Use yyyy-MM-dd.");
            }
        }

        boolean found = false;

        for (K2636521Reservation booking : bookingService.getAllBookings()) {

            if (isMyBooking(booking)
                    && booking.getCheckIn().toLocalDate().equals(selectedDate)) {

                displayBookingDetails(booking);
                found = true;
            }
        }

        if (!found) {
            System.out.println(
                    "No bookings found for the selected check-in date.");
        }
    }

    // Reads and returns the selected room type.
    private K2636521RoomType readRoomType() {
        System.out.println("\n============== Room Types ==============");
        System.out.println("1. Standard");
        System.out.println("2. Deluxe");
        System.out.println("3. Suite");
        System.out.println("4. Family Suite");

        while (true) {
            int choice = readInteger("Select room type: ");

            if (choice >= 1 && choice <= 4) {
                return K2636521RoomType.values()[choice - 1];
            }

            System.out.println("Invalid room type. Please select 1-4.");
        }
    }

    // Reads and returns the selected bed type.
    private K2636521BedType readBedType() {
        System.out.println("\n============== Bed Types ==============");
        System.out.println("1. Single");
        System.out.println("2. Double");
        System.out.println("3. Queen");
        System.out.println("4. King");

        while (true) {
            int choice = readInteger("Select bed type: ");

            if (choice >= 1 && choice <= 4) {
                return K2636521BedType.values()[choice - 1];
            }

            System.out.println("Invalid bed type. Please select 1-4.");
        }
    }

    /* Updates the status of a room. */
    private void updateRoomStatus() {
        System.out.println("\n---------- Update Room Status ----------");

        String roomId = readNonBlankInput("Enter room ID: ");

        K2636521Room room = roomService.findRoomById(roomId);

        if (room == null) {
            System.out.println("Room not found.");
            return;
        }

        System.out.println("\nRoom ID: " + room.getRoomId());
        System.out.println("Current Status: " + room.getStatus());

        System.out.println("\n1. Available");
        System.out.println("2. Under Maintenance");

        int choice = readInteger("Select new status: ");

        K2636521RoomStatus status;

        if (choice == 1) {
            status = K2636521RoomStatus.AVAILABLE;
        } else if (choice == 2) {
            status = K2636521RoomStatus.UNDER_MAINTENANCE;
        } else {
            System.out.println("========================================");
            System.out.println("Invalid status. Please select 1 or 2.");
            System.out.println("========================================");
            return;
        }

        if (roomService.updateRoomStatus(roomId, status)) {
            System.out.println("========================================");
            System.out.println("Room status updated successfully.");
            System.out.println("New Status: " + status);
            System.out.println("========================================");
        } else {
            System.out.println("========================================");
            System.out.println("Unable to update room status.");
            System.out.println("========================================");
        }
    }

    // Displays room details.
    private void displayAllRooms(List<K2636521Room> rooms) {
        System.out.println("\n------------- Room Details -------------");
        System.out.println(" ");

        if (rooms.isEmpty()) {
            System.out.println("No rooms found.");
            return;
        }

        System.out.printf("%-12s %-15s %-12s %-10s %-15s %-15s %-12s%n",
                "Room ID", "Room Type", "Bed Type", "Capacity",
                "Daily Rate", "Breakfast", "Status");

        System.out.println("-----------------------------------------------------------------------------------------------");

        for (K2636521Room room : rooms) {
            System.out.printf("%-12s %-15s %-12s %-10d LKR %-10.2f %-15s %-12s%n",
                    room.getRoomId(),
                    room.getRoomType(),
                    room.getBedType(),
                    room.getCapacity(),
                    room.getDailyRate(),
                    room.hasBreakfast() ? "Included" : "Not Included",
                    room.getStatus());
        }

        System.out.println("-----------------------------------------------------------------------------------------------");
    }

    /* Displays room status for a selected date range. */
    private void viewRoomStatusByDate() {
        System.out.println("\n------- View Room Status by Date -------");

        LocalDate startDate = readDate("Enter start date (yyyy-MM-dd): ");
        LocalDate endDate;

        while (true) {
            endDate = readDate("Enter end date (yyyy-MM-dd): ");

            if (!endDate.isBefore(startDate)) {
                break;
            }

            System.out.println("End date cannot be before start date.");
        }

        LocalDateTime rangeStart = startDate.atStartOfDay();
        LocalDateTime rangeEnd = endDate.plusDays(1).atStartOfDay();

        List<K2636521Room> reservedRooms = new ArrayList<>();
        List<K2636521Room> maintenanceRooms = new ArrayList<>();
        List<K2636521Room> availableRooms = new ArrayList<>();

        Map<String, String> roomBookingIds = new HashMap<>();

        for (K2636521Room room : roomService.getAllRooms()) {

            if (room.getStatus() == K2636521RoomStatus.UNDER_MAINTENANCE) {
                maintenanceRooms.add(room);
                continue;
            }

            boolean isReserved = false;

            for (K2636521Reservation booking : bookingService.getAllBookings()) {

                if (booking.getRoom().getRoomId().equalsIgnoreCase(room.getRoomId())
                        && booking.getStatus() == K2636521BookingStatus.CONFIRMED
                        && booking.getCheckIn().isBefore(rangeEnd)
                        && booking.getCheckOut().isAfter(rangeStart)) {

                    reservedRooms.add(room);
                    roomBookingIds.put(room.getRoomId(), booking.getBookingId());
                    isReserved = true;
                    break;
                }
            }

            if (!isReserved) {
                availableRooms.add(room);
            }
        }
        
        System.out.println("\n========== ROOM STATUS REPORT ==========");
        System.out.println("Date Range: " + startDate + " to " + endDate);

        System.out.println("\n============ RESERVED ROOMS ============");
        displayRoomStatusCategory(reservedRooms, "RESERVED", roomBookingIds);

        System.out.println("\n======== UNDER MAINTENANCE ROOMS ========");
        displayRoomStatusCategory(maintenanceRooms, "UNDER MAINTENANCE", roomBookingIds);

        System.out.println("\n============ AVAILABLE ROOMS ============");
        displayRoomStatusCategory(availableRooms, "AVAILABLE", roomBookingIds);
    }

    /* Displays rooms under a selected status category. */
    private void displayRoomStatusCategory(
            List<K2636521Room> rooms,
            String category,
            Map<String, String> roomBookingIds) {

        if (rooms.isEmpty()) {
            System.out.println("No rooms found.");
            return;
        }

        if (category.equals("RESERVED")) {
            System.out.printf("%-12s %-18s %-15s%n",
                    "Room ID", "Room Type", "Booking ID");
        } else {
            System.out.printf("%-12s %-18s%n",
                    "Room ID", "Room Type");
        }

        for (K2636521Room room : rooms) {

            if (category.equals("RESERVED")) {
                System.out.printf("%-12s %-18s %-15s%n",
                        room.getRoomId(),
                        room.getRoomType(),
                        roomBookingIds.get(room.getRoomId()));
            } else {
                System.out.printf("%-12s %-18s%n",
                        room.getRoomId(),
                        room.getRoomType());
            }
        }
    }

    /* Reads a date in yyyy-MM-dd format. */
    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException exception) {
                System.out.println("Invalid date. Use yyyy-MM-dd.");
            }
        }
    }

    // Finds a registered guest using the guest ID.
    private K2636521Guest findGuestById(String guestId) {
        if (!K2636521InputValidator.isNotBlank(guestId)) {
            return null;
        }

        for (K2636521Guest guest : guests) {
            if (guest.getGuestId().equalsIgnoreCase(guestId.trim())) {
                return guest;
            }
        }

        return null;
    }

    // Displays all registered guests.
    private void displayAllGuests() {
        System.out.println("\n------------------------------- Registered Guests -------------------------------");

        if (guests.isEmpty()) {
            System.out.println("No guests registered.");
            return;
        }

        System.out.printf("%-10s %-22s %-16s %-15s %-25s%n",
                "Guest ID", "Full Name", "Guest Type",
                "Contact", "Email");

        System.out.println(
                "---------------------------------------------------------------------------------");

        for (K2636521Guest guest : guests) {
            System.out.printf("%-10s %-22s %-16s %-15s %-25s%n",
                    guest.getGuestId(),
                    guest.getFullName(),
                    guest.getGuestType(),
                    guest.getContactNumber(),
                    guest.getEmail());
            
            System.out.println(
                    "---------------------------------------------------------------------------------");
        }
    }

    // Creates a booking for the currently logged-in guest.
    private void createBooking() {

        if (loggedInGuest == null) {
            System.out.println("Please log in as a guest first.");
            return;
        }

        System.out.println("\n========== CREATE BOOKING ==========");

        // Search available rooms using the common method.
        BookingSearchResult searchResult =
                searchAvailableRoomsForSelectedCriteria();

        if (searchResult.availableRooms.isEmpty()) {
            return;
        }

        LocalDateTime checkIn = searchResult.checkIn;
        LocalDateTime checkOut = searchResult.checkOut;

        List<K2636521Room> availableRooms =
                searchResult.availableRooms;

        // Allow the guest to select an available room.
        K2636521Room selectedRoom = null;

        while (selectedRoom == null) {

            String roomId = readNonBlankInput("Enter Room ID: ");

            for (K2636521Room room : availableRooms) {

                if (room.getRoomId().equalsIgnoreCase(roomId)) {
                    selectedRoom = room;
                    break;
                }
            }

            if (selectedRoom == null) {
                System.out.println(
                        "Invalid Room ID. Select a room from the available list.");
            }
        }

        // Get the number of guests.
        int numberOfGuests;

        while (true) {

            numberOfGuests = readInteger("Enter number of guests: ");

            if (numberOfGuests > 0) {
                break;
            }

            System.out.println(
                    "Number of guests must be at least 1.");
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(
                checkIn.toLocalDate(),
                checkOut.toLocalDate());

        int extraGuests = Math.max(
                0, numberOfGuests - selectedRoom.getCapacity());

        BigDecimal extraCharge = selectedRoom.getExtraGuestCharge()
                .multiply(BigDecimal.valueOf(extraGuests))
                .multiply(BigDecimal.valueOf(nights));

        // Display the booking summary.
        System.out.println("\n========== BOOKING SUMMARY ==========");
        System.out.println("Guest: " + loggedInGuest.getFullName());
        System.out.println("Room ID: " + selectedRoom.getRoomId());
        System.out.println("Room Type: " + selectedRoom.getRoomType());
        System.out.println("Room Capacity: " + selectedRoom.getCapacity());
        System.out.println("Number of Guests: " + numberOfGuests);
        System.out.println("Extra Guests: " + extraGuests);
        System.out.println("Check-in: " + checkIn);
        System.out.println("Check-out: " + checkOut);
        System.out.println("Nights: " + nights);
        System.out.println("Extra Guest Charge: LKR " + extraCharge);
        System.out.println("Advance Payment: LKR 10000.00");

        if (!processAdvancePayment()) {
            System.out.println(
                    "Booking cancelled. Payment was not completed.");
            return;
        }

        // Create the booking. Availability is checked again in BookingService.
        K2636521Reservation reservation =
                bookingService.createBooking(
                        loggedInGuest,
                        selectedRoom.getRoomId(),
                        checkIn,
                        checkOut,
                        numberOfGuests);

        if (reservation == null) {

            System.out.println(
                    "Booking failed. The room may have been booked by "
                    + "another user or another booking rule was not satisfied.");

            System.out.println(
                    "The advance payment must be refunded.");

            System.out.println(
                    "Refund amount: LKR 10000.00 (simulated).");

            return;
        }

        System.out.println("\nBooking created successfully!");
        System.out.println("Booking ID: " + reservation.getBookingId());
        System.out.println("Room Status: " + selectedRoom.getStatus());
        System.out.println("Advance Payment: PAID");

        K2636521Invoice invoice =
                invoiceService.generateInvoice(reservation);

        if (invoice != null) {
            displayInvoice(invoice);
        } else {
            System.out.println(
                    "Booking confirmed, but invoice generation failed.");
        }
    }

    // Displays rooms available for the selected dates.
    private void displayAvailableRoomsTable(
            List<K2636521Room> rooms) {

        System.out.println("\n============================== AVAILABLE ROOMS ==============================");

        if (rooms.isEmpty()) {
            System.out.println("No available rooms found.");
            return;
        }

        System.out.printf(
                "%-10s %-15s %-10s %-10s %-15s %-12s%n",
                "Room ID", "Room Type", "Bed Type",
                "Capacity", "Daily Rate", "Breakfast");

        System.out.println(
                "-----------------------------------------------------------------------------");
        for (K2636521Room room : rooms) {
            System.out.printf(
                    "%-10s %-15s %-10s %-10d %-15s %-12s%n",
                    room.getRoomId(),
                    room.getRoomType(),
                    room.getBedType(),
                    room.getCapacity(),
                    room.getDailyRate(),
                    room.hasBreakfast() ? "Included" : "No");
        }

        System.out.println(
                "-----------------------------------------------------------------------------");
    }

    // Displays all reservations for the administrator.
    private void displayAllBookings() {
        System.out.println("\n------------------ All Bookings ------------------");

        List<K2636521Reservation> bookings =
                bookingService.getAllBookings();

        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }

        for (K2636521Reservation booking : bookings) {
            displayBookingDetails(booking);
        }
    }

    /* Displays booking viewing options for the guest. */
    private void viewMyBookingsMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n-------- View My Bookings --------");
            System.out.println("1. View All My Bookings");
            System.out.println("2. View Bookings by Check-in Date");
            System.out.println("0. Back");

            int choice = readInteger("Enter your choice: ");

            switch (choice) {
                case 1:
                    displayMyBookings();
                    break;

                case 2:
                    displayMyBookingsByCheckInDate();
                    break;

                case 0:
                    running = false;
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
        
    // Displays only the logged-in guest's reservations.
    private void displayMyBookings() {
        System.out.println("\n------------------ My Bookings -------------------");

        boolean found = false;

        for (K2636521Reservation booking
                : bookingService.getAllBookings()) {

            if (isMyBooking(booking)) {
                displayBookingDetails(booking);
                found = true;
            }
        }

        if (!found) {
            System.out.println("You have no bookings.");
        }
    }

    // Checks whether a reservation belongs to the logged-in guest.
    private boolean isMyBooking(K2636521Reservation reservation) {
        return loggedInGuest != null
                && reservation.getGuest().getGuestId()
                .equalsIgnoreCase(loggedInGuest.getGuestId());
    }

    /* Searches bookings using booking ID or guest name. */
    private void searchBookingAsAdmin() {
        System.out.println("\n------------ Search Booking ------------");

        String searchInput = readNonBlankInput(
                "Enter Booking ID or Guest Name: ");

        if (searchInput.matches("(?i)BK\\d+")) {

            K2636521Reservation reservation =
                    bookingService.findBookingById(searchInput);

            if (reservation == null) {
                System.out.println("Booking not found.");
            } else {
                displayBookingDetails(reservation);
            }

        } else {

            List<K2636521Reservation> bookings =
                    bookingService.findBookingsByGuestName(searchInput);

            if (bookings.isEmpty()) {
                System.out.println("No bookings found for this guest name.");
                return;
            }

            System.out.println("\nFound " + bookings.size() + " booking(s).");

            for (K2636521Reservation booking : bookings) {
                displayBookingDetails(booking);
            }
        }
    }

    // Searches only the logged-in guest's reservations.
    private void searchMyBooking() {
        String bookingId = readNonBlankInput("Enter booking ID: ");

        K2636521Reservation reservation = findMyBooking(bookingId);

        if (reservation == null) {
            System.out.println("Your booking was not found.");
            return;
        }

        displayBookingDetails(reservation);
    }
    
    // Finds a booking belonging to the logged-in guest.
    private K2636521Reservation findMyBooking(String bookingId) {
        if (loggedInGuest == null) {
            return null;
        }

        K2636521Reservation reservation =
                bookingService.findBookingById(bookingId);

        if (reservation == null || !isMyBooking(reservation)) {
            return null;
        }

        return reservation;
    }

    // Displays reservation details.
    private void displayBookingDetails(
            K2636521Reservation reservation) {

        System.out.println("\n==================================================");
        System.out.println("                 BOOKING DETAILS");
        System.out.println("==================================================");

        System.out.println("\nBOOKING INFORMATION");
        System.out.println("--------------------------------------------------");
        System.out.printf("%-22s : %s%n", "Booking ID",
                reservation.getBookingId());
        System.out.printf("%-22s : %s%n", "Booking Status",
                reservation.getStatus());

        System.out.println("\nGUEST & ROOM INFORMATION");
        System.out.println("--------------------------------------------------");
        System.out.printf("%-22s : %s%n", "Guest Name",
                reservation.getGuest().getFullName());
        System.out.printf("%-22s : %s%n", "Room ID",
                reservation.getRoom().getRoomId());
        System.out.printf("%-22s : %s%n", "Room Type",
                reservation.getRoom().getRoomType());

        System.out.println("\nSTAY INFORMATION");
        System.out.println("--------------------------------------------------");
        System.out.printf("%-22s : %s%n", "Check-in",
                reservation.getCheckIn());
        System.out.printf("%-22s : %s%n", "Check-out",
                reservation.getCheckOut());
        System.out.printf("%-22s : %d%n", "Number of Nights",
                reservation.getNumberOfNights());
        System.out.printf("%-22s : %d%n", "Number of Guests",
                reservation.getNumberOfGuests());

        System.out.println("\nPAYMENT INFORMATION");
        System.out.println("--------------------------------------------------");
        System.out.printf("%-22s : %s%n", "Advance Payment",
                reservation.isAdvancePaid() ? "Paid" : "Unpaid");
        System.out.printf("%-22s : %s%n", "Advance Refund",
                reservation.isAdvanceRefunded() ? "Refunded" : "Not Refunded");

        System.out.println("==================================================");
    }

    // Cancels any reservation as an administrator.
    private void cancelBookingAsAdmin() {
        String bookingId = readNonBlankInput("Enter booking ID to cancel: ");

        cancelBookingById(bookingId, "ADMIN");
    }

    // Searches available rooms for modifying an existing reservation.
    private BookingSearchResult searchAvailableRoomsForModification(
            K2636521Reservation reservation) {

        K2636521RoomType roomType = readRoomType();

        LocalDateTime checkIn = readCheckInDateTime();
        LocalDateTime checkOut = readCheckOutDateTime(checkIn);

        List<K2636521Room> availableRooms =
                bookingService.getAvailableRoomsForModification(
                        checkIn,
                        checkOut,
                        roomType,
                        reservation);

        System.out.println("\nRoom Type: " + roomType);
        System.out.println("Check-in: " + checkIn);
        System.out.println("Check-out: " + checkOut);

        if (availableRooms.isEmpty()) {
            System.out.println(
                    "No " + roomType
                    + " rooms are available for the selected dates.");
        } else {
            System.out.println("\nAvailable rooms:");
            displayAvailableRoomsTable(availableRooms);
        }

        return new BookingSearchResult(
                checkIn, checkOut, availableRooms);
    }

    // Allows a guest to modify an existing reservation.
    private void modifyMyBooking() {

        if (loggedInGuest == null) {
            System.out.println("Please log in as a guest first.");
            return;
        }

        System.out.println("\n========== MODIFY MY BOOKING ==========");

        String bookingId = readNonBlankInput("Enter Booking ID: ");

        K2636521Reservation reservation = findMyBooking(bookingId);

        if (reservation == null) {
            System.out.println(
                    "Booking not found or you are not authorized to modify it.");
            return;
        }

        if (reservation.getStatus() != K2636521BookingStatus.CONFIRMED) {
            System.out.println("Only confirmed bookings can be modified.");
            return;
        }

        if (LocalDateTime.now().plusHours(24)
                .isAfter(reservation.getCheckIn())) {
            System.out.println(
                    "Bookings cannot be modified within 24 hours of check-in.");
            return;
        } 

        System.out.println("\nCurrent Booking Details:");
        displayBookingDetails(reservation);

        System.out.println("\nEnter New Booking Details:");

        BookingSearchResult searchResult =
                searchAvailableRoomsForModification(reservation);

        if (searchResult == null
                || searchResult.availableRooms.isEmpty()) {
            System.out.println("No available rooms found.");
            return;
        }

        LocalDateTime checkIn = searchResult.checkIn;
        LocalDateTime checkOut = searchResult.checkOut;

        List<K2636521Room> availableRooms =
                searchResult.availableRooms;

        // Allow the guest to select an available room.
        K2636521Room selectedRoom = null;

        while (selectedRoom == null) {

            String roomId = readNonBlankInput("Enter Room ID: ");

            for (K2636521Room room : availableRooms) {

                if (room.getRoomId().equalsIgnoreCase(roomId)) {
                    selectedRoom = room;
                    break;
                }
            }

            if (selectedRoom == null) {
                System.out.println(
                        "Invalid Room ID. Select a room from the available list.");
            }
        }

        // Get the number of guests.
        int numberOfGuests;

        while (true) {

            numberOfGuests = readInteger("Enter number of guests: ");

            if (numberOfGuests > 0) {
                break;
            }

            System.out.println(
                    "Number of guests must be at least 1.");
        }

        // Update the reservation.
        boolean updated = bookingService.modifyBooking(
                bookingId,
                selectedRoom.getRoomId(),
                checkIn,
                checkOut,
                numberOfGuests
        );

        if (!updated) {
            System.out.println("Unable to modify the reservation.");
            return;
        }

        // Update the existing invoice.
        boolean invoiceUpdated =
                invoiceService.updateInvoiceForBooking(bookingId);

        if (invoiceUpdated) {

            System.out.println(
                    "\nReservation and invoice updated successfully.");

            K2636521Reservation updatedReservation =
                    bookingService.findBookingById(bookingId);

            displayBookingDetails(updatedReservation);

            K2636521Invoice invoice =
                    invoiceService.findInvoiceByBookingId(bookingId);

            if (invoice != null) {
                displayInvoice(invoice);
            }

        } else {
            System.out.println(
                    "Reservation updated, but invoice update failed.");
        }
    }

    // Allows the guest to cancel only their own reservation.
    private void cancelMyBooking() {
        if (loggedInGuest == null) {
            System.out.println("Please log in as a guest first.");
            return;
        }

        String bookingId = readNonBlankInput(
                "Enter your booking ID to cancel: ");

        K2636521Reservation reservation = findMyBooking(bookingId);

        if (reservation == null) {
            System.out.println("Your booking was not found.");
            return;
        }

        cancelBookingById(reservation.getBookingId(), "GUEST");
    }

    // Cancels a reservation and updates its existing invoice.
    private void cancelBookingById(
            String bookingId, String cancelledBy) {

        boolean cancelled = bookingService.cancelBooking(bookingId);

        if (cancelled) {
            System.out.println("\n========================================");
            System.out.println("Booking cancelled successfully.");

            boolean invoiceUpdated =
                    invoiceService.voidInvoiceForBooking(
                            bookingId, cancelledBy);

            if (invoiceUpdated) {
                K2636521Invoice invoice =
                        invoiceService.findInvoiceByBookingId(bookingId);

                System.out.println("Existing invoice updated successfully.");
                System.out.println("\n========================================");
                displayInvoice(invoice);
            } else {
                System.out.println("\n========================================");
                System.out.println(
                        "Warning: Booking cancelled, but its invoice "
                        + "could not be updated.");
                System.out.println("========================================");
            }

        } else {
            System.out.println("\n========================================");
            System.out.println(
                    "Cancellation failed. Check the booking status "
                    + "and the 24-hour cancellation rule.");
            System.out.println("========================================");
        }
    }

    // Processes the advance payment for a booking.
    private boolean processAdvancePayment() {
        BigDecimal requiredAmount = new BigDecimal("10000.00");

        System.out.println("\n========== ADVANCE PAYMENT ==========");
        System.out.println("Payment Amount: LKR " + requiredAmount);
        System.out.println("1. Pay Advance");
        System.out.println("0. Cancel Payment");

        int choice;

        while (true) {
            choice = readInteger("Select payment option: ");

            if (choice == 1 || choice == 0) {
                break;
            }

            System.out.println("Invalid choice. Select 1 or 0.");
        }

        if (choice == 0) {
            return false;
        }

        while (true) {
            System.out.print("Enter payment amount (LKR): ");
            String input = scanner.nextLine().trim();

            BigDecimal paidAmount;

            try {
                paidAmount = new BigDecimal(input);
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid payment amount.");
                continue;
            }

            if (paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Payment amount must be positive.");
                continue;
            }

            if (paidAmount.compareTo(requiredAmount) < 0) {
                System.out.println(
                        "Insufficient payment. LKR "
                        + requiredAmount.subtract(paidAmount)
                        + " more is required.");
                continue;
            }

            BigDecimal balance = paidAmount.subtract(requiredAmount);

            System.out.println("Payment successful!");
            System.out.println("Advance Paid: LKR " + requiredAmount);
            System.out.println("Balance: LKR " + balance);
            System.out.println("Payment Status: PAID");

            return true;
        }
    }

    // Completes an eligible stay and displays its invoice.
    private void completeStayAndGenerateInvoice() {
        String bookingId = readNonBlankInput("Enter booking ID: ");

        K2636521Reservation reservation =
                bookingService.findBookingById(bookingId);

        if (reservation == null) {
            System.out.println("Booking not found.");
            return;
        }
        if (reservation.getStatus() != K2636521BookingStatus.CONFIRMED) {
            System.out.println(
                    "Only confirmed bookings can be completed.");
            return;
        }
        if (reservation.getCheckOut().isAfter(LocalDateTime.now())) {
            System.out.println(
                    "The stay cannot be completed before check-out time.");
            return;
        }
        K2636521Invoice invoice = invoiceService.findInvoiceByBookingId(bookingId);

        if (invoice == null) {
            System.out.println("Invoice not found for this booking.");
            return;
        }
        reservation.setStatus(K2636521BookingStatus.COMPLETED);
        System.out.println("Stay completed successfully.");
        System.out.println("Room is now available for future dates.");

        displayInvoice(invoice);
    }

    /* Displays invoices belonging to the logged-in guest. */
    private void displayMyInvoices() {
        System.out.println("\n================== MY INVOICES ==================");

        boolean found = false;

        for (K2636521Invoice invoice : invoiceService.getAllInvoices()) {

            if (isMyBooking(invoice.getReservation())) {
                displayInvoice(invoice);
                found = true;
            }
        }

        if (!found) {
            System.out.println("You have no invoices.");
        }
    }

    /* Displays all invoices for the administrator. */
    private void displayAllInvoices() {
        System.out.println("\n================= ALL INVOICES =================");

        List<K2636521Invoice> invoices =
                invoiceService.getAllInvoices();

        if (invoices.isEmpty()) {
            System.out.println("No invoices found.");
            return;
        }

        for (K2636521Invoice invoice : invoices) {
            displayInvoice(invoice);
        }
    }

    /* Displays invoice details in separate sections. */
    private void displayInvoice(K2636521Invoice invoice) {

        K2636521Reservation reservation = invoice.getReservation();

        K2636521Guest guest = reservation.getGuest();

        K2636521Room room = reservation.getRoom();

        DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

        System.out.println("\n");
        System.out.println("================================================");
        System.out.println("                  HOTEL INVOICE");
        System.out.println("================================================");

        System.out.println("Invoice ID         : " + invoice.getInvoiceId());
        System.out.println("Invoice Status     : " + invoice.getInvoiceStatus());

        // Guest Details
        System.out.println("\n---------------- GUEST DETAILS ----------------");

        System.out.println("Guest ID           : " + guest.getGuestId());
        System.out.println("Guest Name         : " + guest.getFullName());
        System.out.println("Contact Number     : " + guest.getContactNumber());
        System.out.println("Email              : " + guest.getEmail());
        System.out.println("NIC                : " + guest.getNicNumber());
        System.out.println("Passport           : " + guest.getPassportNumber());

        // Booking Details
        System.out.println("\n--------------- BOOKING DETAILS ---------------");

        System.out.println("Booking ID         : " + reservation.getBookingId());

        System.out.println("Check-in Date      : "
                + reservation.getCheckIn().format(dateFormatter));

        System.out.println("Check-out Date     : "
                + reservation.getCheckOut().format(dateFormatter));

        System.out.println("Number of Nights   : "
                + reservation.getNumberOfNights());

        System.out.println("Number of Guests   : "
                + reservation.getNumberOfGuests());

        System.out.println("Booking Status     : " + reservation.getStatus());

        // Room Details
        printMenuHeader("ROOM DETAILS");

        System.out.println("Room ID            : " + room.getRoomId());
        System.out.println("Room Type          : " + room.getRoomType());
        System.out.println("Bed Type           : " + room.getBedType());
        System.out.println("Daily Rate         : LKR " + room.getDailyRate());
        System.out.println("Room Capacity      : " + room.getCapacity());

        // Payment Summary
        System.out.println("\n--------------- PAYMENT SUMMARY ---------------");

        System.out.println("Base Charge        : LKR " + invoice.getBaseCharge());

        System.out.println("Extra Guest Charge : LKR "
                + invoice.getExtraGuestCharge());

        System.out.println("Discount           : LKR " + invoice.getDiscount());

        String taxPercentage = invoice.getTaxRate()
                .multiply(new BigDecimal("100"))
                .stripTrailingZeros()
                .toPlainString();

        System.out.println("Tax (" + taxPercentage + "%)           : LKR "
                + invoice.getTax());

        System.out.println("Advance Deduction  : LKR "
                + invoice.getAdvanceDeduction());

        System.out.println("------------------------------------------------");

        if (invoice.getInvoiceStatus()
                == K2636521InvoiceStatus.VOIDED) {

            // Cancellation Details
            System.out.println("ORIGINAL FINAL PAYABLE: LKR "
                    + invoice.getFinalPayable());

            System.out.println("\n------------- CANCELLATION DETAILS ------------");

            System.out.println("Booking Status     : CANCELLED");

            System.out.println("Cancelled By       : "
                    + invoice.getCancelledBy());

            System.out.println("Advance Paid       : LKR 10000.00");

            System.out.println("Advance Refunded   : LKR "
                    + invoice.getRefundAmount());

            System.out.println("Refund Status      : "
                    + (invoice.getRefundAmount().compareTo(BigDecimal.ZERO) > 0
                    ? "REFUNDED" : "NOT REFUNDED"));

            System.out.println("------------------------------------------------");
            System.out.println("FINAL PAYABLE AFTER CANCELLATION: LKR 0.00");

        } else {

            System.out.println("FINAL PAYABLE      : LKR "
                    + invoice.getFinalPayable());
        }

        System.out.println("================================================");
        System.out.println();
    }

    // Reads a non-empty string from the user.
    private String readNonBlankInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            if (K2636521InputValidator.isNotBlank(value)) {
                return value;
            }

            System.out.println("Input cannot be empty.");
        }
    }

    // Reads an integer safely.
    private int readInteger(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException exception) {
                System.out.println(
                        "Please enter a valid whole number.");
            }
        }
    }

    // Reads a date and time in the required format.
    private LocalDateTime readDateTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return LocalDateTime.parse(
                        input, DATE_TIME_FORMAT);
            } catch (DateTimeParseException exception) {
                System.out.println(
                        "Invalid date/time. Use yyyy-MM-dd HH:mm.");
            }
        }
    }

    // Reads a valid check-in date at least two days ahead.
    private LocalDateTime readCheckInDateTime() {
        while (true) {
            LocalDateTime checkIn = readDateTime(
                    "Enter check-in (yyyy-MM-dd HH:mm): ");

            if (!K2636521InputValidator
                    .isBookingAtLeastTwoDaysAhead(checkIn)) {
                System.out.println(
                        "Booking must be made at least 2 days in advance.");
                continue;
            }

            return checkIn;
        }
    }

    // Reads a valid check-out date after check-in.
    private LocalDateTime readCheckOutDateTime(
            LocalDateTime checkIn) {

        while (true) {
            LocalDateTime checkOut = readDateTime(
                    "Enter check-out (yyyy-MM-dd HH:mm): ");

            if (!checkOut.isAfter(checkIn)
                    || !checkOut.toLocalDate()
                            .isAfter(checkIn.toLocalDate())) {
                System.out.println(
                        "Check-out must be at least one day after check-in.");
                continue;
            }

            return checkOut;
        }
    }
}