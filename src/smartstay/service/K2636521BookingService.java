package smartstay.service;

import smartstay.model.K2636521BookingStatus;
import smartstay.model.K2636521Guest;
import smartstay.model.K2636521Reservation;
import smartstay.model.K2636521Room;
import smartstay.model.K2636521RoomStatus;
import smartstay.model.K2636521RoomType;
import smartstay.util.K2636521InputValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/* Manages reservations and date-based room availability. */
public class K2636521BookingService {

    private K2636521RoomService roomService;
    private List<K2636521Reservation> reservations;

    public K2636521BookingService(K2636521RoomService roomService) {
        if (roomService == null) {
            throw new IllegalArgumentException(
                    "Room service cannot be null.");
        }

        this.reservations = new ArrayList<>();
        this.roomService = roomService;
    }

    // Returns rooms available for the selected dates and room type.
    public List<K2636521Room> getAvailableRooms(
            LocalDateTime checkIn, LocalDateTime checkOut) {

        return getAvailableRooms(checkIn, checkOut, null);
    }

    // Returns available rooms matching the selected room type.
    public List<K2636521Room> getAvailableRooms(
            LocalDateTime checkIn, LocalDateTime checkOut,
            K2636521RoomType roomType) {

        List<K2636521Room> availableRooms = new ArrayList<>();

        if (!K2636521InputValidator.isValidStay(checkIn, checkOut)) {
            return availableRooms;
        }

        for (K2636521Room room : roomService.getAllRooms()) {

            boolean matchingRoomType = roomType == null
                    || room.getRoomType() == roomType;

            if (room.getStatus() == K2636521RoomStatus.AVAILABLE
                    && matchingRoomType
                    && !hasOverlappingBooking(room, checkIn, checkOut)) {

                availableRooms.add(room);
            }
        }

        return availableRooms;
    }

    // Returns available rooms for modification, excluding the current booking.
    public List<K2636521Room> getAvailableRoomsForModification(
            LocalDateTime checkIn, LocalDateTime checkOut,
            K2636521RoomType roomType, K2636521Reservation currentReservation) {

        List<K2636521Room> availableRooms = new ArrayList<>();

        if (currentReservation == null
                || !K2636521InputValidator.isValidStay(checkIn, checkOut)) {
            return availableRooms;
        }

        for (K2636521Room room : roomService.getAllRooms()) {

            boolean matchingRoomType = roomType == null
                    || room.getRoomType() == roomType;

            if (room.getStatus() == K2636521RoomStatus.AVAILABLE
                    && matchingRoomType
                    && !hasOverlappingBooking(
                            room, checkIn, checkOut, currentReservation)) {

                availableRooms.add(room);
            }
        }

        return availableRooms;
    }

    // Creates a booking after checking the selected room's availability.
    public K2636521Reservation createBooking(K2636521Guest guest, String roomId,
            LocalDateTime checkIn, LocalDateTime checkOut, int numberOfGuests) {

        if (guest == null || !guest.hasValidIdentification()) {
            return null;
        }

        if (!K2636521InputValidator.isValidStay(checkIn, checkOut)
                || !K2636521InputValidator
                        .isBookingAtLeastTwoDaysAhead(checkIn)) {
            return null;
        }

        if (!K2636521InputValidator.isPositive(numberOfGuests)
                || !K2636521InputValidator.isNotBlank(roomId)) {
            return null;
        }

        K2636521Room room = roomService.findRoomById(roomId);

        if (room == null
                || room.getStatus() != K2636521RoomStatus.AVAILABLE
                || hasOverlappingBooking(room, checkIn, checkOut)) {
            return null;
        }

        String bookingId = generateBookingId();

        K2636521Reservation reservation = new K2636521Reservation(
                bookingId, guest, room, checkIn,
                checkOut, numberOfGuests, K2636521BookingStatus.CONFIRMED, 
                true, false
        );

        reservations.add(reservation);

        return reservation;
    }

    // Modifies an existing confirmed reservation.
    public boolean modifyBooking(String bookingId, String roomId,
            LocalDateTime checkIn, LocalDateTime checkOut,
            int numberOfGuests) {

        if (!K2636521InputValidator.isNotBlank(bookingId)
                || !K2636521InputValidator.isNotBlank(roomId)
                || !K2636521InputValidator.isPositive(numberOfGuests)) {
            return false;
        }

        K2636521Reservation reservation =
                findBookingById(bookingId);

        if (reservation == null
                || reservation.getStatus()
                        != K2636521BookingStatus.CONFIRMED
                || !reservation.isAdvancePaid()
                || reservation.isAdvanceRefunded()) {
            return false;
        }

        // Modifications are not allowed within 24 hours of check-in.
        if (!LocalDateTime.now().plusHours(24)
                .isBefore(reservation.getCheckIn())
                && !LocalDateTime.now().plusHours(24)
                        .isEqual(reservation.getCheckIn())) {
            return false;
        }

        if (!K2636521InputValidator.isValidStay(checkIn, checkOut)
                || !K2636521InputValidator
                        .isBookingAtLeastTwoDaysAhead(checkIn)) {
            return false;
        }

        K2636521Room newRoom = roomService.findRoomById(roomId);

        if (newRoom == null
                || newRoom.getStatus() != K2636521RoomStatus.AVAILABLE) {
            return false;
        }

        // Excludes the current reservation from the availability check.
        if (hasOverlappingBooking(
                newRoom, checkIn, checkOut, reservation)) {
            return false;
        }

        reservation.setRoom(newRoom);
        reservation.setCheckIn(checkIn);
        reservation.setCheckOut(checkOut);
        reservation.setNumberOfGuests(numberOfGuests);

        return true;
    }

    // Generates the next booking ID.
    private String generateBookingId() {
        int highestId = 0;

        for (K2636521Reservation reservation : reservations) {
            String id = reservation.getBookingId();

            if (id != null && id.matches("BK\\d+")) {
                int number = Integer.parseInt(id.substring(2));
                highestId = Math.max(highestId, number);
            }
        }

        return String.format("BK%03d", highestId + 1);
    }

    // Checks whether the selected dates overlap an existing booking.
    private boolean hasOverlappingBooking(
            K2636521Room room,
            LocalDateTime checkIn,
            LocalDateTime checkOut) {

        return hasOverlappingBooking(room, checkIn, checkOut,null);
    }

    // Checks room availability while excluding a selected reservation.
    private boolean hasOverlappingBooking(
            K2636521Room room,
            LocalDateTime checkIn,
            LocalDateTime checkOut,
            K2636521Reservation excludedReservation) {

        for (K2636521Reservation reservation : reservations) {
            if (reservation == excludedReservation) {
                continue;
            }

            boolean sameRoom = reservation.getRoom()
                    .getRoomId().equalsIgnoreCase(room.getRoomId());
            boolean activeBooking =
                    reservation.getStatus()
                            != K2636521BookingStatus.CANCELLED;

            boolean datesOverlap =
                    checkIn.isBefore(reservation.getCheckOut())
                    && checkOut.isAfter(reservation.getCheckIn());
            if (sameRoom && activeBooking && datesOverlap) {
                return true;
            }
        }
        return false;
    }

    // Finds a booking using its booking ID.
    public K2636521Reservation findBookingById(String bookingId) {
        if (!K2636521InputValidator.isNotBlank(bookingId)) {
            return null;
        }

        for (K2636521Reservation reservation : reservations) {
            if (reservation.getBookingId()
                    .equalsIgnoreCase(bookingId.trim())) {
                return reservation;
            }
        }

        return null;
    }

    // Checks whether a room has any booking history.
    public boolean hasBookingsForRoom(String roomId) {
        if (!K2636521InputValidator.isNotBlank(roomId)) {
            return false;
        }

        for (K2636521Reservation reservation : reservations) {
            if (reservation.getRoom().getRoomId()
                    .equalsIgnoreCase(roomId.trim())) {
                return true;
            }
        }

        return false;
    }

    // Finds bookings using a guest name.
    public List<K2636521Reservation> findBookingsByGuestName(
            String guestName) {

        List<K2636521Reservation> matchingBookings = new ArrayList<>();

        if (!K2636521InputValidator.isNotBlank(guestName)) {
            return matchingBookings;
        }

        String searchName = guestName.trim().toLowerCase();

        for (K2636521Reservation reservation : reservations) {

            String fullName = reservation.getGuest().getFullName();

            if (fullName != null
                    && fullName.toLowerCase().contains(searchName)) {
                matchingBookings.add(reservation);
            }
        }

        return matchingBookings;
    }

    // Returns all bookings.
    public List<K2636521Reservation> getAllBookings() {
        return new ArrayList<>(reservations);
    }

    // Cancels a confirmed booking and refunds its advance payment.
    public boolean cancelBooking(String bookingId) {
        if (!K2636521InputValidator.isNotBlank(bookingId)) {
            return false;
        }

        K2636521Reservation reservation =
                findBookingById(bookingId);

        if (reservation == null
                || reservation.getStatus()
                        != K2636521BookingStatus.CONFIRMED) {
            return false;
        }

        if (LocalDateTime.now().plusHours(24)
                .isAfter(reservation.getCheckIn())) {
            return false;
        }

        reservation.setStatus(K2636521BookingStatus.CANCELLED);

        if (reservation.isAdvancePaid()
                && !reservation.isAdvanceRefunded()) {
            reservation.setAdvanceRefunded(true);
        }

        return true;
    }
}