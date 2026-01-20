package ci553.happyshop.client.customer;

import ci553.happyshop.client.AccountManager;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.storageAccess.DatabaseRWFactory;
import javafx.application.Application;
import javafx.stage.Stage;


/**
 * A standalone Customer Client that can be run independently without launching the full system.
 * Designed for early-stage testing, though full functionality may require other clients to be active.
 */

public class CustomerClient extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Creates the Model, View, and Controller objects and links them together for communication.
     * It also creates the DatabaseRW instance via the DatabaseRWFactory and injects it into the CustomerModel.
     * Once the components are linked, the customer interface (view) is started.
     *
     * Also creates the RemoveProductNotifier, which tracks the position of the Customer View
     * and is triggered by the Customer Model when needed.
     */
    

   

    //private String loggedInUserId;
    //private String loggedInUserName;



    //public void setLoggedInUser(String userId, String userName) {
    //    this.loggedInUserId = userId;
    //    this.loggedInUserName = userName;
    //}

    @Override
    public void start(Stage window) {
        CustomerView cusView = new CustomerView();
        CustomerController cusController = new CustomerController();
        CustomerModel cusModel = new CustomerModel();
        DatabaseRW databaseRW = DatabaseRWFactory.createDatabaseRW();

        
        AccountManager mgr = AccountManager.getInstance();
        String uid = mgr.getCurrentUserId();
        String name = (uid == null) ? null : mgr.getNameFor(uid);
        cusView.setLoginStatus(uid, name);
        cusModel.setLoggedInUser(uid, name);


        cusView.cusController = cusController;
        cusController.cusModel = cusModel;
        cusModel.cusView = cusView;
        cusModel.databaseRW = databaseRW;

        cusView.start(window);
    }
}


