package smartstay.model;

import smartstay.util.K2636521InputValidator;

/* Stores guest details for local and international guests. */
public class K2636521Guest extends K2636521Person {

    private String guestId;
    private K2636521GuestType guestType;
    private String nicNumber;
    private String passportNumber;

    public K2636521Guest(String guestId, String guestType,
                         String fullName, String contactNumber,
                         String email, String nicNumber,
                         String passportNumber) {
        super(fullName, contactNumber, email);

        this.guestId = guestId;
        this.guestType = K2636521GuestType.valueOf(
                guestType.trim().toUpperCase());
        this.nicNumber = nicNumber;
        this.passportNumber = passportNumber;
    }

    // Returns the guest's ID.
    public String getGuestId() {
        return guestId;
    }

    // Returns the guest's type.
    public K2636521GuestType getGuestType() {
        return guestType;
    }

    // Returns the guest's NIC number.
    public String getNicNumber() {
        return nicNumber;
    }

    // update the guest's NIC number.
    public void setNicNumber(String nicNumber) {
        this.nicNumber = nicNumber;
    }

    // Returns the guest's passport number.
    public String getPassportNumber() {
        return passportNumber;
    }

    // update the guest's passport number.
    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    // Returns true if the guest has a valid identification number.
    public boolean hasValidIdentification() {

        if (guestType == K2636521GuestType.LOCAL) {
            return K2636521InputValidator.isValidSriLankanNIC(nicNumber)
                    || K2636521InputValidator.isValidPassportNumber(
                            passportNumber);
        }

        return K2636521InputValidator.isValidPassportNumber(
                passportNumber);
    }
}
