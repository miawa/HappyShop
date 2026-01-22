package ci553.happyshop.customer;
import ci553.happyshop.client.customer.CustomerModel;
import ci553.happyshop.client.AccountManager;
import ci553.happyshop.client.UserRole;
import ci553.happyshop.client.LoginState;



import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginStateAndAuthTest {

    @Test
    void successfulLogin_authenticateTrue_andStateAuthenticatedWhenSet() {
        AccountManager mgr = AccountManager.getInstance();

        String id = mgr.peekAvailableUserId();
        mgr.createAccount(id, "Test User", "1234", UserRole.CUSTOMER);

        assertTrue(mgr.authenticate(id, "1234"));

        mgr.setCurrentUser(id);
        mgr.setLoginState(LoginState.AUTHENTICATED);

        assertEquals(LoginState.AUTHENTICATED, mgr.getLoginState());
        assertEquals(id, mgr.getCurrentUserId());
    }

    @Test
    void failedLogin_authenticateFalse_andCanMarkInvalid() {
        AccountManager mgr = AccountManager.getInstance();

        String id = mgr.peekAvailableUserId();
        mgr.createAccount(id, "Test User2", "4321", UserRole.CUSTOMER);

        assertFalse(mgr.authenticate(id, "9999"));

        mgr.setLoginState(LoginState.INVALID);
        assertEquals(LoginState.INVALID, mgr.getLoginState());
    }

    @Test
    void skipLogin_setsSkippedState_andClearsUser() {
        AccountManager mgr = AccountManager.getInstance();

        mgr.setCurrentUser("1111");
        mgr.setLoginState(LoginState.AUTHENTICATED);

        mgr.clearCurrentUser();
        mgr.setLoginState(LoginState.SKIPPED);

        assertNull(mgr.getCurrentUserId());
        assertEquals(LoginState.SKIPPED, mgr.getLoginState());
    }
}
