package smartstay.model;

/* Stores administrator details. */
public class K2636521Admin extends K2636521Person {

    private String adminId;

    public K2636521Admin(String adminId, String fullName,
                         String contactNumber, String email) {
        super(fullName, contactNumber, email);
        this.adminId = adminId;
    }

    // Returns the administrator ID.
    public String getAdminId() {
        return adminId;
    }

    // Updates the administrator ID.
    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }
}   