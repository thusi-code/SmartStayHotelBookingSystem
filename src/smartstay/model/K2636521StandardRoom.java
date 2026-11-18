package smartstay.model;

import java.math.BigDecimal;

/* Defines the Standard room package and charges. */
public class K2636521StandardRoom extends K2636521Room {

    public K2636521StandardRoom(String roomId, K2636521BedType bedType,
                             K2636521RoomStatus status, int capacity) {
        super(roomId, bedType, status, capacity);
    }

    // Returns the daily rate of LKR 6,000.
    @Override
    public BigDecimal getDailyRate() {
        return new BigDecimal("6000.00");
    }

    // Returns the room type.
    @Override
    public K2636521RoomType getRoomType() {
        return K2636521RoomType.STANDARD;
    }

    // Standard rooms do not include breakfast.
    @Override
    public boolean hasBreakfast() {
        return false;
    }

    // Returns the extra guest charge of LKR 1,000.
    @Override
    public BigDecimal getExtraGuestCharge() {
        return new BigDecimal("1000.00");
    }

    // Returns the 10% tax rate.
    @Override
    public BigDecimal getTaxRate() {
        return new BigDecimal("0.10");
    }
}