package smartstay.model;

import java.math.BigDecimal;

/* Defines the Suite room package and charges. */
public class K2636521SuiteRoom extends K2636521Room {

    public K2636521SuiteRoom(String roomId, K2636521BedType bedType,
                             K2636521RoomStatus status, int capacity) {
        super(roomId, bedType, status, capacity);
    }

    // Returns the room type.
    @Override
    public K2636521RoomType getRoomType() {
        return K2636521RoomType.SUITE;
    }

    // Returns the daily rate of LKR 14,000.
    @Override
    public BigDecimal getDailyRate() {
        return new BigDecimal("14000.00");
    }

    // Suite rooms include breakfast.
    @Override
    public boolean hasBreakfast() {
        return true;
    }

    // Returns the extra guest charge of LKR 2,000.
    @Override
    public BigDecimal getExtraGuestCharge() {
        return new BigDecimal("2000.00");
    }

    // Returns the 15% tax rate.
    @Override
    public BigDecimal getTaxRate() {
        return new BigDecimal("0.15");
    }
}