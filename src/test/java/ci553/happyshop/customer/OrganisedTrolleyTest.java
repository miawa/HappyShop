package ci553.happyshop.customer;
import ci553.happyshop.client.customer.CustomerModel;


import ci553.happyshop.catalogue.Product;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class OrganisedTrolleyTest extends BaseCustomerModelTest {

    @Test
    void mergesSameProductId_andAggregatesQuantity() {
        ArrayList<Product> trolley = getTrolley();

        // simulate trolley containing repeated items (same id)
        Product p1 = product("0002"); p1.setOrderedQuantity(1);
        Product p2 = product("0002"); p2.setOrderedQuantity(1);
        Product p3 = product("0002"); p3.setOrderedQuantity(1);

        trolley.add(p1);
        trolley.add(p2);
        trolley.add(p3);

        invokeMergeAndSortTrolley();

        assertEquals(1, trolley.size(), "Expected merge into single product entry");
        assertEquals("0002", trolley.get(0).getProductId());
        assertEquals(3, trolley.get(0).getOrderedQuantity(), "Expected aggregated ordered quantity");
    }

    @Test
    void sortingOrder_isByProductIdAscending() {
        ArrayList<Product> trolley = getTrolley();

        Product a = product("0009"); a.setOrderedQuantity(1);
        Product b = product("0001"); b.setOrderedQuantity(1);
        Product c = product("0005"); c.setOrderedQuantity(1);

        trolley.add(a);
        trolley.add(b);
        trolley.add(c);

        invokeMergeAndSortTrolley();

        assertEquals("0001", trolley.get(0).getProductId());
        assertEquals("0005", trolley.get(1).getProductId());
        assertEquals("0009", trolley.get(2).getProductId());
    }

    @Test
    void mergeDoesNotLoseDistinctProducts() {
        ArrayList<Product> trolley = getTrolley();

        Product a = product("0001"); a.setOrderedQuantity(2);
        Product b = product("0002"); b.setOrderedQuantity(1);

        trolley.add(a);
        trolley.add(b);

        invokeMergeAndSortTrolley();

        assertEquals(2, trolley.size());
        assertEquals("0001", trolley.get(0).getProductId());
        assertEquals("0002", trolley.get(1).getProductId());
    }
}
