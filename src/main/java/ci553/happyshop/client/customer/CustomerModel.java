package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Order;
import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.utility.StorageLocation;
import ci553.happyshop.utility.ProductListFormatter;
import ci553.happyshop.client.customer.RemoveProductNotifier;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 * You can either directly modify the CustomerModel class to implement the required tasks,
 * or create a subclass of CustomerModel and override specific methods where appropriate.
 */
public class CustomerModel {
    public CustomerView cusView;
    public DatabaseRW databaseRW; //Interface type, not specific implementation
                                  //Benefits: Flexibility: Easily change the database implementation.

    private Product theProduct =null; // product found from search
    private ArrayList<Product> trolley =  new ArrayList<>(); // a list of products in trolley

    // Four UI elements to be passed to CustomerView for display updates.
    private String imageName = "imageHolder.jpg";                // Image to show in product preview (Search Page)
    private String displayLaSearchResult = "No Product was searched yet"; // Label showing search result message (Search Page)
    private String displayTaTrolley = "";                                // Text area content showing current trolley items (Trolley Page)
    private String displayTaReceipt = "";                                // Text area content showing receipt after checkout (Receipt Page)


    private String loggedInUserId;
    private String loggedInUserName;

    public void setLoggedInUser(String userId, String userName) {
        this.loggedInUserId = userId;
        this.loggedInUserName = userName;
    }
    public RemoveProductNotifier removeProductNotifier;


    //SELECT productID, description, image, unitPrice,inStock quantity
    void search() throws SQLException {
    String productId = cusView.tfId.getText().trim();

    if(!productId.isEmpty()){
        theProduct = databaseRW.searchByProductId(productId); //search database
        if(theProduct != null && theProduct.getStockQuantity()>0){
            double unitPrice = theProduct.getUnitPrice();
            String description = theProduct.getProductDescription();
            int stock = theProduct.getStockQuantity();

            String baseInfo = String.format("Product_Id: %s\n%s,\nPrice: £%.2f", productId, description, unitPrice);
            String quantityInfo = stock < 100 ? String.format("\n%d units left.", stock) : "";
            displayLaSearchResult = baseInfo + quantityInfo;
            System.out.println(displayLaSearchResult);
        }
        else{
            theProduct=null;
            displayLaSearchResult = "No Product was found with ID " + productId;
            System.out.println("No Product was found with ID " + productId);
        }
    }
    else {
        //searches db for name keyword if no productId is provided
        String nameKeyword = cusView.tfName.getText().trim(); 

        if(!nameKeyword.isEmpty()){
            ArrayList<Product> matches = databaseRW.searchProduct(nameKeyword);

            if(matches != null && !matches.isEmpty()){
                theProduct = matches.get(0); 

                double unitPrice = theProduct.getUnitPrice();
                String description = theProduct.getProductDescription();
                int stock = theProduct.getStockQuantity();

                String baseInfo = String.format("Product_Id: %s\n%s,\nPrice: £%.2f",
                        theProduct.getProductId(), description, unitPrice);
                String quantityInfo = stock < 100 ? String.format("\n%d units left.", stock) : "";
                displayLaSearchResult = baseInfo + quantityInfo;

                System.out.println(displayLaSearchResult);
            }
            else{
                theProduct = null;
                displayLaSearchResult = "No Product was found with name containing: " + nameKeyword;
                System.out.println(displayLaSearchResult);
            }
        }
        else{
            theProduct=null;
            displayLaSearchResult = "Please type ProductID or Name";
            System.out.println(displayLaSearchResult);
        }
    }

    updateView();
}

    void addToTrolley(){
        if(theProduct!= null){

            // trolley.add(theProduct) — Product is appended to the end of the trolley.
            // To keep the trolley organized, add code here or call a method that:
            //TODO
            // 1. Merges items with the same product ID (combining their quantities).
            // 2. Sorts the products in the trolley by product ID.
            int requestedQty = cusView.getSelectedQty();
        int stock = theProduct.getStockQuantity();

        if (requestedQty > stock) {
            requestedQty = stock;
            cusView.showPopup(
                    
                    "Quantity adjusted",
                    "Exceeded maximum stock for this item.\n" +
                    "Set quantity to max available: " + stock
            );
            cusView.setSelectedQty(1);
            ci553.happyshop.utility.SoundManager.error();
        }

        if (requestedQty <= 0) {
            displayLaSearchResult = "Quantity must be at least 1.";
            updateView();
            return;
        }

       
        for (int i = 0; i < requestedQty; i++) {
            trolley.add(theProduct);
        }

        
            mergeAndSortTrolley();
            displayTaTrolley = ProductListFormatter.buildString(trolley); 
        }
        else{
            ci553.happyshop.utility.SoundManager.error();
            displayLaSearchResult = "Please search for an available product before adding it to the trolley";
            System.out.println("must search and get an available product before add to trolley");
        }
        displayTaReceipt=""; // Clear receipt to switch back to trolleyPage (receipt shows only when not empty)
        updateView();
    }

    void checkOut() throws IOException, SQLException {
        if(!trolley.isEmpty()){
            // Group the products in the trolley by productId to optimize stock checking
            // Check the database for sufficient stock for all products in the trolley.
            // If any products are insufficient, the update will be rolled back.
            // If all products are sufficient, the database will be updated, and insufficientProducts will be empty.
            // Note: If the trolley is already organized (merged and sorted), grouping is unnecessary.
            mergeAndSortTrolley();
            ArrayList<Product> groupedTrolley= groupProductsById(trolley);
            ArrayList<Product> insufficientProducts= databaseRW.purchaseStocks(groupedTrolley);

            if(insufficientProducts.isEmpty()){ // If stock is sufficient for all products
                //get OrderHub and tell it to make a new Order
                ci553.happyshop.utility.SoundManager.purchase();
                OrderHub orderHub =OrderHub.getOrderHub();
                Order theOrder = orderHub.newOrder(new ArrayList<>(trolley));
                trolley.clear();
                displayTaTrolley ="";
                displayTaReceipt = buildReceipt(theOrder);
                System.out.println(displayTaReceipt);

            }
            else{ // Some products have insufficient stock — build an error message to inform the customer
                ci553.happyshop.utility.SoundManager.error();
                StringBuilder errorMsg = new StringBuilder();
                for(Product p : insufficientProducts){
                    errorMsg.append("\u2022 "+ p.getProductId()).append(", ")
                            .append(p.getProductDescription()).append(" (Only ")
                            .append(p.getStockQuantity()).append(" available, ")
                            .append(p.getOrderedQuantity()).append(" requested)\n");
                }
                theProduct=null;

                //TODO
                // Add the following logic here:
                // 1. Remove products with insufficient stock from the trolley.
                // 2. Trigger a message window to notify the customer about the insufficient stock, rather than directly changing displayLaSearchResult.
                //You can use the provided RemoveProductNotifier class and its showRemovalMsg method for this purpose.
                //remember close the message window where appropriate (using method closeNotifierWindow() of RemoveProductNotifier class)
                if (removeProductNotifier != null) {
                    removeProductNotifier.showRemovalMsg(errorMsg.toString());
                } else {
                    displayLaSearchResult = "Checkout failed due to insufficient stock:\n" + errorMsg;
                }
                System.out.println("stock is not enough");
            }
        }
        else{
            ci553.happyshop.utility.SoundManager.error();
            displayTaTrolley = "Your trolley is empty";
            System.out.println("Your trolley is empty");
        }
        updateView();
    }

    private void adjustTrolleyForInsufficientStock(ArrayList<Product> insufficientProducts) {
    
            Map<String, Integer> availableById = new HashMap<>();
            for (Product p : insufficientProducts) {
                availableById.put(p.getProductId(), p.getStockQuantity()); 
            
            }

            
            Map<String, Integer> keptCount = new HashMap<>();
            ArrayList<Product> newTrolley = new ArrayList<>();

        for (Product item : trolley) {
                String id = item.getProductId();
                if (!availableById.containsKey(id)) {
                    newTrolley.add(item);
                    continue;
                }

                int available = availableById.get(id);

                
            if (item.getOrderedQuantity() > 1) {
                    if (available <= 0) {
                        continue; // remove it
                    }
                    item.setOrderedQuantity(Math.min(item.getOrderedQuantity(), available));
                    newTrolley.add(item);
                    continue;
                }

                int keptSoFar = keptCount.getOrDefault(id, 0);
            if (keptSoFar < available) {
                    newTrolley.add(item);
                    keptCount.put(id, keptSoFar + 1);
                }
                
            }

            trolley = newTrolley; // if trolley is not final, replace it
        }

    private String buildReceipt(Order order) {
    
        StringBuilder sb = new StringBuilder();
        sb.append("=-= HappyShop Receipt =-=\n");
        sb.append("Order_ID: ").append(order.getOrderId()).append("\n");
        sb.append("Ordered_Date_Time: ").append(order.getOrderedDateTime()).append("\n\n");

        if (loggedInUserId != null && loggedInUserName != null) {
            sb.append("Customer: ").append(loggedInUserName)
            .append(" (").append(loggedInUserId).append(")\n\n");
        }
        sb.append("--- Items ---\n");
        sb.append(ProductListFormatter.buildString(order.getProductList())).append("\n");

    
        double subtotal = 0.0;
        for (Product p : order.getProductList()) {
            subtotal += p.getUnitPrice() * p.getOrderedQuantity();
        }

        sb.append("\n--- Summary ---\n");
        sb.append(String.format("Subtotal: £%.2f\n", subtotal));

        
   
        sb.append("\nThank you for shopping with HappyShop!\n");
        return sb.toString();
    }

    private void mergeAndSortTrolley() {
    Map<String, Product> grouped = new HashMap<>();

    for (Product p : trolley) {
        String id = p.getProductId();

        if (grouped.containsKey(id)) {
            Product existing = grouped.get(id);
            existing.setOrderedQuantity(existing.getOrderedQuantity() + p.getOrderedQuantity());
        } else {
           
            Product copy = new Product(
                    p.getProductId(),
                    p.getProductDescription(),
                    p.getProductImageName(),
                    p.getUnitPrice(),
                    p.getStockQuantity()
            );
            copy.setOrderedQuantity(p.getOrderedQuantity());
            grouped.put(id, copy);
        }
    }

    trolley.clear();
    trolley.addAll(grouped.values());

    trolley.sort((a, b) -> a.getProductId().compareTo(b.getProductId()));
}



    /**
     * Groups products by their productId to optimize database queries and updates.
     * By grouping products, we can check the stock for a given `productId` once, rather than repeatedly
     */
    private ArrayList<Product> groupProductsById(ArrayList<Product> proList) {
        Map<String, Product> grouped = new HashMap<>();
        for (Product p : proList) {
            String id = p.getProductId();
            if (grouped.containsKey(id)) {
                Product existing = grouped.get(id);
                existing.setOrderedQuantity(existing.getOrderedQuantity() + p.getOrderedQuantity());
            } else {
                // Make a shallow copy to avoid modifying the original
                grouped.put(id,new Product(p.getProductId(),p.getProductDescription(),
                        p.getProductImageName(),p.getUnitPrice(),p.getStockQuantity()));
            }
        }
        return new ArrayList<>(grouped.values());
    }

    void cancel(){
        trolley.clear();
        displayTaTrolley="";
        updateView();
    }
    void closeReceipt(){
        displayTaReceipt="";
    }

    void browseStock() throws SQLException {
        
    ArrayList<Product> products = databaseRW.getAllProducts();
    System.out.println("[CustomerModel] getAllProducts size = " + products.size());
    if (products == null || products.isEmpty()) {
        cusView.showPopup("Current Stock", "No products found in the database.");
        return;
    }

    BrowseStockView browseStockView = new BrowseStockView();

    browseStockView.show(products, cusView.getWindowBounds());
    }

    

    void updateView() {
        if(theProduct != null){
            imageName = theProduct.getProductImageName();
            String relativeImageUrl = StorageLocation.imageFolder +imageName; //relative file path, eg images/0001.jpg
            // Get the full absolute path to the image
            Path imageFullPath = Paths.get(relativeImageUrl).toAbsolutePath();
            imageName = imageFullPath.toUri().toString(); //get the image full Uri then convert to String
            System.out.println("Image absolute path: " + imageFullPath); // Debugging to ensure path is correct
        }
        else{
            imageName = "imageHolder.jpg";
        }
        cusView.update(imageName, displayLaSearchResult, displayTaTrolley,displayTaReceipt);
    }
     // extra notes:
     //Path.toUri(): Converts a Path object (a file or a directory path) to a URI object.
     //File.toURI(): Converts a File object (a file on the filesystem) to a URI object

    //for test only
    public ArrayList<Product> getTrolley() {
        return trolley;
    }
}
