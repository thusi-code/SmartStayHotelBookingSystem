package smartstay.model;

import java.math.BigDecimal;

/* Stores common details and behaviour for all room types. */
public abstract class K2636521Room {

    private String roomId;
    private K2636521BedType bedType;
    private K2636521RoomStatus status;
    private int capacity;

    /* Creates a room with its basic details. */
    public K2636521Room(String roomId, K2636521BedType bedType,
                        K2636521RoomStatus status, int capacity) {
        this.roomId = roomId;
        this.bedType = bedType;
        this.status = status;
        this.capacity = capacity;
    }

    // Returns the room type.
    public abstract K2636521RoomType getRoomType();
    // Returns the daily room rate.
    public abstract BigDecimal getDailyRate();
    // Indicates whether breakfast is included.
    public abstract boolean hasBreakfast();
    // Returns the extra guest charge per person per day.
    public abstract BigDecimal getExtraGuestCharge();
    // Returns the room's tax rate.
    public abstract BigDecimal getTaxRate();

    // Returns the room ID.
    public String getRoomId() {
        return roomId;
    }

    // Returns the bed type.
    public K2636521BedType getBedType() {
        return bedType;
    }

    // Returns the room status.
    public K2636521RoomStatus getStatus() {
        return status;
    }

    // Returns the room capacity.
    public int getCapacity() {
        return capacity;
    }

    // Updates the room status.
    public void setStatus(K2636521RoomStatus status) {
        this.status = status;
    }

    // Updates the bed type.
    public void setBedType(K2636521BedType bedType) {
        this.bedType = bedType;
    }

    // Updates the room capacity.
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}