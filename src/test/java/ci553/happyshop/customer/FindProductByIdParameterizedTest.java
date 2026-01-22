package ci553.happyshop.customer;
import ci553.happyshop.client.customer.CustomerModel;


import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class FindProductByIdParameterizedTest extends BaseCustomerModelTest {

    static class FakeDb implements DatabaseRW {
        @Override
        public Product searchByProductId(String productId) throws SQLException {
            if ("0001".equals(productId)) return new Product("0001", "X", "x.jpg", 1.0, 1);
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

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\n"})
    void findProductById_blankInputs_returnNull(String input) throws Exception {
        model.databaseRW = new FakeDb();
        assertNull(model.findProductById(input));
    }
}
