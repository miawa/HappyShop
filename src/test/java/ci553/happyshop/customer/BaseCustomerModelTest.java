package ci553.happyshop.customer;
import ci553.happyshop.client.customer.CustomerModel;


import ci553.happyshop.catalogue.Product;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public abstract class BaseCustomerModelTest {

    protected CustomerModel model;

    @BeforeEach
    void setupBase() {
        model = new CustomerModel();
    }

    protected static Product product(String id) {
        
        return new Product(id, "Desc-" + id, "img.jpg", 1.0, 999);
    }

    @SuppressWarnings("unchecked")
    protected ArrayList<Product> getTrolley() {
        try {
            Field f = CustomerModel.class.getDeclaredField("trolley");
            f.setAccessible(true);
            return (ArrayList<Product>) f.get(model);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void invokeMergeAndSortTrolley() {
        try {
            Method m = CustomerModel.class.getDeclaredMethod("mergeAndSortTrolley");
            m.setAccessible(true);
            m.invoke(model);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
