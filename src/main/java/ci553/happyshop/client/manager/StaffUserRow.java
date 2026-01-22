package ci553.happyshop.client.manager;

import ci553.happyshop.client.UserRole;

public class StaffUserRow {
    private final String userId;
    private final String name;
    private final UserRole role;

    public StaffUserRow(String userId, String name, UserRole role) {
        this.userId = userId;
        this.name = name;
        this.role = role;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public UserRole getRole() { return role; }

    // for TableView display if needed:
    public String getRoleText() { return role == null ? "" : role.name(); }
}
