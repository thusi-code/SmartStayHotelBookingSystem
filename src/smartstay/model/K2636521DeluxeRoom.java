package smartstay.model;

import java.math.BigDecimal;

/* Defines the Deluxe room package and charges. */
public class K2636521DeluxeRoom extends K2636521Room {

    public K2636521DeluxeRoom(String roomId, K2636521BedType bedType,
                              K2636521RoomStatus status, int capacity) {
        super(roomId, bedType, status, capacity);
    }

    // Returns the room type.
    @Override
    public K2636521RoomType getRoomType() {
        return K2636521RoomType.DELUXE;
    }

    // Returns the daily rate of LKR 9,000.
    @Override
    public BigDecimal getDailyRate() {
        return new BigDecimal("9000.00");
    }

    // Deluxe rooms include breakfast.
    @Override
    public boolean hasBreakfast() {
        return true;
    }

    // Returns the extra guest charge of LKR 1,500.
    @Override
    public BigDecimal getExtraGuestCharge() {
        return new BigDecimal("1500.00");
    }

    // Returns the 12% tax rate.
    @Override
    public BigDecimal getTaxRate() {
        return new BigDecimal("0.12");
    }
}