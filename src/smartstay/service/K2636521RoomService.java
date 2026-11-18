package smartstay.service;

import smartstay.model.K2636521Room;
import smartstay.model.K2636521RoomStatus;
import smartstay.model.K2636521RoomType;
import smartstay.util.K2636521InputValidator;
import smartstay.model.K2636521BedType;

import java.util.ArrayList;
import java.util.List;

/* Manages hotel rooms and their operational status. */
public class K2636521RoomService {

    private List<K2636521Room> rooms;

    public K2636521RoomService() {
        rooms = new ArrayList<>();
    }

    // Generates the next room ID based on room type.
    public String generateRoomId(K2636521RoomType roomType) {
        if (roomType == null) {
            return null;
        }

        String prefix;
        int startingNumber;

        switch (roomType) {
            case STANDARD:
                prefix = "S";
                startingNumber = 101;
                break;
            case DELUXE:
                prefix = "D";
                startingNumber = 201;
                break;
            case SUITE:
                prefix = "SU";
                startingNumber = 301;
                break;
            case FAMILY_SUITE:
                prefix = "FS";
                startingNumber = 401;
                break;
            default:
                return null;
        }

        int highestNumber = startingNumber - 1;

        for (K2636521Room room : rooms) {
            String id = room.getRoomId();

            if (id != null && id.startsWith(prefix)) {
                String numberPart = id.substring(prefix.length());
                if (numberPart.matches("\\d+")) {
                    int number = Integer.parseInt(numberPart);
                    highestNumber = Math.max(highestNumber, number);
                }
            }
        }
        return prefix + (highestNumber + 1);
    }

    // Adds a room if its details are valid and its ID is unique.
    public boolean addRoom(K2636521Room room) {
        if (room == null
                || !K2636521InputValidator.isNotBlank(room.getRoomId())
                || !K2636521InputValidator.isPositive(room.getCapacity())
                || room.getBedType() == null
                || room.getStatus() == null
                || room.getRoomType() == null
                || findRoomById(room.getRoomId()) != null) {
            return false;
        }
        rooms.add(room);
        return true;
    }

    // Removes a room using its room ID.
    public boolean removeRoom(String roomId) {
        if (!K2636521InputValidator.isNotBlank(roomId)) {
            return false;
        }
        K2636521Room room = findRoomById(roomId);
        if (room == null) {
            return false;
        }
        return rooms.remove(room);
    }

    // Returns a copy of all registered rooms.
    public List<K2636521Room> getAllRooms() {
        return new ArrayList<>(rooms);
    }

    // Finds a room using its room ID.
    public K2636521Room findRoomById(String roomId) {
        if (!K2636521InputValidator.isNotBlank(roomId)) {
            return null;
        }

        for (K2636521Room room : rooms) {
            if (room.getRoomId()
                    .equalsIgnoreCase(roomId.trim())) {
                return room;
            }
        }

        return null;
    }

    // Returns rooms that are not under maintenance.
    public List<K2636521Room> getAvailableRooms() {
        List<K2636521Room> availableRooms = new ArrayList<>();

        for (K2636521Room room : rooms) {
            if (room.getStatus() == K2636521RoomStatus.AVAILABLE) {
                availableRooms.add(room);
            }
        }

        return availableRooms;
    }

    // Updates the operational status of a room.
    public boolean updateRoomStatus(
            String roomId, K2636521RoomStatus newStatus) {

        if (!K2636521InputValidator.isNotBlank(roomId)
                || newStatus == null
                || newStatus == K2636521RoomStatus.RESERVED) {
            return false;
        }

        K2636521Room room = findRoomById(roomId);

        if (room == null) {
            return false;
        }

        room.setStatus(newStatus);
        return true;
    }

    // Updates the bed type and capacity of a room.
    public boolean updateRoomDetails(
            String roomId,
            K2636521BedType bedType,
            int capacity) {

        if (!K2636521InputValidator.isNotBlank(roomId)
                || bedType == null
                || !K2636521InputValidator.isPositive(capacity)) {
            return false;
        }

        K2636521Room room = findRoomById(roomId);

        if (room == null) {
            return false;
        }

        room.setBedType(bedType);
        room.setCapacity(capacity);

        return true;
    }

    // Returns the total number of registered rooms.
    public int getRoomCount() {
        return rooms.size();
    }
}