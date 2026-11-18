package smartstay.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/* Stores the details of a room reservation. */
public class K2636521Reservation {

    private String bookingId;
    private K2636521Guest guest;
    private K2636521Room room;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private int numberOfGuests;
    private K2636521BookingStatus status;
    private boolean advancePaid;
    private boolean advanceRefunded;

    public K2636521Reservation(String bookingId, K2636521Guest guest,
                               K2636521Room room, LocalDateTime checkIn,
                               LocalDateTime checkOut, int numberOfGuests,
                               K2636521BookingStatus status,
                               boolean advancePaid, boolean advanceRefunded) {
        this.bookingId = bookingId;
        this.guest = guest;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.numberOfGuests = numberOfGuests;
        this.status = status;
        this.advancePaid = advancePaid;
        this.advanceRefunded = advanceRefunded;
    }

    // Returns the booking ID.
    public String getBookingId() {
        return bookingId;
    }

    // Returns the guest associated with the reservation.
    public K2636521Guest getGuest() {
        return guest;
    }

    // Returns the reserved room.
    public K2636521Room getRoom() {
        return room;
    }

    // Returns the check-in date and time.
    public LocalDateTime getCheckIn() {
        return checkIn;
    }

    // Returns the check-out date and time.
    public LocalDateTime getCheckOut() {
        return checkOut;
    }

    // Returns the number of guests in the reservation.
    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    // Returns the booking status of the reservation.
    public K2636521BookingStatus getStatus() {
        return status;
    }

    // Returns true if the advance payment has been made.
    public boolean isAdvancePaid() {
        return advancePaid;
    }

    // Returns true if the advance payment has been refunded.
    public boolean isAdvanceRefunded() {
        return advanceRefunded;
    }

    // Updates the booking status of the reservation.
    public void setStatus(K2636521BookingStatus status) {
        this.status = status;
    }

    // Updates the advance payment status of the reservation.
    public void setAdvancePaid(boolean advancePaid) {
        this.advancePaid = advancePaid;
    }

    // Updates the advance refund status of the reservation.
    public void setAdvanceRefunded(boolean advanceRefunded) {
        this.advanceRefunded = advanceRefunded;
    }

    // Updates the reserved room.
    public void setRoom(K2636521Room room) {
        this.room = room;
    }

    // Updates the check-in date and time.
    public void setCheckIn(LocalDateTime checkIn) {
        this.checkIn = checkIn;
    }

    // Updates the check-out date and time.
    public void setCheckOut(LocalDateTime checkOut) {
        this.checkOut = checkOut;
    }

    // Updates the number of guests.
    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    // Calculates the number of nights in the reservation.
    public long getNumberOfNights() {
        return ChronoUnit.DAYS.between(
                checkIn.toLocalDate(), checkOut.toLocalDate());
    }
}