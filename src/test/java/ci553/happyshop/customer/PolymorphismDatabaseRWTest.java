package ci553.happyshop.customer;
import ci553.happyshop.client.customer.CustomerModel;



import ci553.happyshop.catalogue.Product;
import ci553.happyshop.client.customer.CustomerModel;
import ci553.happyshop.storageAccess.DatabaseRW;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class PolymorphismDatabaseRWTest {

    static class FakeDatabaseRW implements DatabaseRW {
        int searchByIdCalls = 0;

        @Override
        public Product searchByProductId(String productId) throws SQLException {
            searchByIdCalls++;
            if ("0001".equals(productId)) {
                return new Product("0001", "Test Apples", "apple.jpg", 1.99, 50);
            }
            return null;
        }

       
        @Override public ArrayList<Product> searchProduct(String keyword) { throw new UnsupportedOperationException(); }
        @Override public ArrayList<Product> purchaseStocks(ArrayList<Product> proList) { throw new UnsupportedOperationException(); }
        @Override public ArrayList<Product> getAllProducts() { throw new UnsupportedOperationException(); }
        @Override public void updateProduct(String id, String des, double price, String imageName, int stock) { throw new UnsupportedOperationException(); }
        @Override public void deleteProduct(String id) { throw new UnsupportedOperationException(); }
        @Override public void insertNewProduct(String id, String des, double price, String image, int stock) { throw new UnsupportedOperationException(); }
        @Override public boolean isProIdAvailable(String productId) { throw new UnsupportedOperationException(); }
    }

    @Test
    void polymorphism_callViaSupertypeReference_runtimeBindingHappens() throws Exception {
        
        DatabaseRW db = new FakeDatabaseRW();

        CustomerModel model = new CustomerModel();
        model.databaseRW = db; 

        Product p = model.findProductById("0001");

        assertNotNull(p);
        assertEquals("0001", p.getProductId());

        FakeDatabaseRW fake = (FakeDatabaseRW) db;
        assertEquals(1, fake.searchByIdCalls);
    }

    @Test
    void lsp_substitutability_customerModelWorksWithAnyDatabaseRW() throws Exception {
        
        DatabaseRW db = new FakeDatabaseRW();

        CustomerModel model = new CustomerModel();
        model.databaseRW = db;

        assertNull(model.findProductById("9999"));
        assertNull(model.findProductById("   "));
        assertNull(model.findProductById(null));
    }
}
