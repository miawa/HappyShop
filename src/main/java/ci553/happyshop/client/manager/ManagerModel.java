package ci553.happyshop.client.manager;

import ci553.happyshop.client.AccountManager;
import ci553.happyshop.client.UserRole;

import java.util.ArrayList;
import java.util.List;

public class ManagerModel {
    public ManagerView view;

    public List<StaffUserRow> loadStaffUsers() {
        AccountManager mgr = AccountManager.getInstance();

        List<StaffUserRow> rows = new ArrayList<>();

        for (AccountManager.AccountInfo acc : mgr.getAllAccounts()) {
            if (acc == null) continue;

            UserRole role = acc.role;
            if (role == null || role == UserRole.CUSTOMER) continue;

            rows.add(new StaffUserRow(
                    safe(acc.userId),
                    safe(acc.name),
                    role   
            ));
        }

        return rows;
    }

    public void deleteUser(String userId) {
        if (userId == null || userId.isBlank()) return;
        AccountManager.getInstance().deleteAccount(userId);
    }

    public void updateUser(String userId, String newName, UserRole newRole) {
        if (userId == null || userId.isBlank()) return;

        AccountManager mgr = AccountManager.getInstance();
        mgr.updateAccount(userId, newName, newRole); 
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
