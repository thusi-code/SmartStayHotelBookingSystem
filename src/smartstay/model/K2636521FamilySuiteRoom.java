package smartstay.model;

import java.math.BigDecimal;

/* Defines the Family Suite package and charges. */
public class K2636521FamilySuiteRoom extends K2636521Room {

    public K2636521FamilySuiteRoom(String roomId, K2636521BedType bedType,
                                   K2636521RoomStatus status, int capacity) {
        super(roomId, bedType, status, capacity);
    }

    // Returns the room type.
    @Override
    public K2636521RoomType getRoomType() {
        return K2636521RoomType.FAMILY_SUITE;
    }

    // Returns the daily rate of LKR 18,000.
    @Override
    public BigDecimal getDailyRate() {
        return new BigDecimal("18000.00");
    }

    // Family Suite rooms include breakfast.
    @Override
    public boolean hasBreakfast() {
        return true;
    }

    // Returns the extra guest charge of LKR 2,500.
    @Override
    public BigDecimal getExtraGuestCharge() {
        return new BigDecimal("2500.00");
    }

    // Returns the 15% tax rate.
    @Override
    public BigDecimal getTaxRate() {
        return new BigDecimal("0.15");
    }
}