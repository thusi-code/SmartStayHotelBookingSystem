
package smartstay.util;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/* Validates user input before processing system operations. */
public class K2636521InputValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+()\\- ]{7,20}$");
    private static final Pattern OLD_NIC_PATTERN = Pattern.compile("^[0-9]{9}[VvXx]$");
    private static final Pattern NEW_NIC_PATTERN = Pattern.compile("^[0-9]{12}$");
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[A-Za-z0-9]{6,12}$");

    private K2636521InputValidator() {}

    // Validates that a string is not null, empty, or whitespace only.
    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // validates email format using regex pattern.
    public static boolean isValidEmail(String email) {
        return isNotBlank(email)
                && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    // Validates Sri Lankan NIC format using regex patterns for old and new formats.
    public static boolean isValidSriLankanNIC(String nic) {
        return isNotBlank(nic)
                && (OLD_NIC_PATTERN.matcher(nic.trim()).matches()
                || NEW_NIC_PATTERN.matcher(nic.trim()).matches());
    }

    // validates check-in and check-out dates to ensure check-in is before check-out.
    public static boolean isValidStay(
            LocalDateTime checkIn, LocalDateTime checkOut) {
        return checkIn != null
                && checkOut != null
                && checkIn.isBefore(checkOut);
    }

    // Validates phone number format using regex pattern.
    public static boolean isValidPhoneNumber(String phoneNumber) {
        return isNotBlank(phoneNumber)
                && PHONE_PATTERN.matcher(phoneNumber.trim()).matches();
    }

    // Validates passport number format using regex pattern.
    public static boolean isValidPassportNumber(String passport) {
        return isNotBlank(passport)
                && PASSPORT_PATTERN.matcher(passport.trim()).matches();
    }

    // Validates that an integer is positive (greater than zero).
    public static boolean isPositive(int value) {
        return value > 0;
    }

    // Validates that a double is non-negative (zero or greater).
    public static boolean isNonNegativeAmount(double amount) {
        return Double.isFinite(amount) && amount >= 0;
    }

    // Validates that a booking is made at least two days ahead of the check-in date.
    public static boolean isBookingAtLeastTwoDaysAhead(
            LocalDateTime checkIn) {
        return checkIn != null
                && !checkIn.isBefore(LocalDateTime.now().plusDays(2));
    }

    // Validates that the number of guests does not exceed the room capacity.
    public static boolean isValidGuestCount(
            int numberOfGuests, int roomCapacity) {
        return numberOfGuests > 0
                && roomCapacity > 0
                && numberOfGuests <= roomCapacity;
    }

    // Validates that a string contains only digits (0-9).
    public static boolean containsOnlyDigits(String value) {
        return isNotBlank(value)
                && value.trim().matches("\\d+");
    }
}