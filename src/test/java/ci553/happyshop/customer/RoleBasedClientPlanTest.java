package ci553.happyshop.customer;

import ci553.happyshop.client.Main;
import ci553.happyshop.client.UserRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ci553.happyshop.client.Main.ClientType.*;

public class RoleBasedClientPlanTest {

    @Test
    void customerPlan_includesCustomerPickerTrackerEmergency() {
        List<Main.ClientType> plan = Main.planClientsForRole(UserRole.CUSTOMER);
        assertTrue(plan.contains(EMERGENCY));
        assertTrue(plan.contains(CUSTOMER));
        assertTrue(plan.contains(PICKER));
        assertTrue(plan.contains(TRACKER));
        assertFalse(plan.contains(MANAGER));
    }

    @Test
    void managerPlan_includesManager() {
        List<Main.ClientType> plan = Main.planClientsForRole(UserRole.MANAGER);
        assertTrue(plan.contains(EMERGENCY));
        assertTrue(plan.contains(MANAGER));
        assertTrue(plan.contains(PICKER));
        assertTrue(plan.contains(TRACKER));
    }

    @Test
    void warehousePlan_includesWarehouse() {
        List<Main.ClientType> plan = Main.planClientsForRole(UserRole.WAREHOUSE);
        assertTrue(plan.contains(EMERGENCY));
        assertTrue(plan.contains(WAREHOUSE));
        assertFalse(plan.contains(CUSTOMER));
    }
}
