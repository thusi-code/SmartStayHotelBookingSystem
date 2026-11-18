package smartstay.model;

/* Stores common details shared by guests and administrators. */
public abstract class K2636521Person {

    private String fullName;
    private String contactNumber;
    private String email;

    /* Creates a person with the supplied details. */
    public K2636521Person(String fullName, 
            String contactNumber, String email) {
        this.fullName = fullName;
        this.contactNumber = contactNumber;
        this.email = email;
    }

    // Returns the person's full name.
    public String getFullName() {
        return fullName;
    }

    // Returns the person's contact number.
    public String getContactNumber() {
        return contactNumber;
    }

    // Returns the person's email address.
    public String getEmail() {
        return email;
    }

    // Updates the person's full name.
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    // Updates the person's contact number.
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    // Updates the person's email address.
    public void setEmail(String email) {
        this.email = email;
    }
}