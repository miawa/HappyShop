package ci553.happyshop.client;


public enum UserRole {
    CUSTOMER,
    PICKER,
    TRACKER,
    WAREHOUSE,
    MANAGER,
    ALL; // used for skip login

   
    public static UserRole fromString(String raw) {
        if (raw == null) return CUSTOMER;
        String s = raw.trim();
        if (s.isEmpty()) return CUSTOMER;

        s = s.toUpperCase();

       
        if (s.equals("ORDERTRACKER") || s.equals("ORDER_TRACKER")) return TRACKER;
        if (s.equals("WAREHOUSESTAFF") || s.equals("WAREHOUSE_STAFF")) return WAREHOUSE;
        if (s.equals("ADMIN")) return MANAGER; 

        try {
            return UserRole.valueOf(s);
        } catch (IllegalArgumentException ex) {
            return CUSTOMER; 
        }
    }
}
