package ci553.happyshop.customer;
import ci553.happyshop.client.customer.CustomerModel;


import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CheckoutMockitoTest extends BaseCustomerModelTest {

    @Test
    void attemptPurchase_callsPurchaseStocksOnce_withGroupedProducts() throws Exception {
        DatabaseRW db = mock(DatabaseRW.class);

      
        when(db.purchaseStocks(any())).thenReturn(new ArrayList<>());

        model.databaseRW = db;

    
        ArrayList<Product> trolley = getTrolley();
        Product p = product("0002");
        p.setOrderedQuantity(1);
        trolley.add(p);
        trolley.add(product("0002"));
        trolley.add(product("0002"));

 
        model.attemptPurchase();

        // Verify interaction
        ArgumentCaptor<ArrayList<Product>> captor = ArgumentCaptor.forClass(ArrayList.class);
        verify(db, times(1)).purchaseStocks(captor.capture());

        ArrayList<Product> sent = captor.getValue();
        assertNotNull(sent);
        assertEquals(1, sent.size(), "Expected grouped list sent to database");
        assertEquals("0002", sent.get(0).getProductId());
    }
}
