package ci553.happyshop.client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ci553.happyshop.client.AccountManager;
import ci553.happyshop.utility.UIStyle;

/**
 * Simple JavaFX login UI where a user can enter userID and password.
 * Includes a "Create Account" option that shows a small account creation dialog.
 * This class only provides the UI and basic callbacks; authentication persistence
 * and wiring into the rest of the application will be implemented in later steps.
 * 
 * User can "Create Account" or "Login" with existing login information; or may Skip Login. 
 */
public class LoginView {

    public void start(Stage stage) {
        stage.setTitle("HappyShop - Login");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        Label lblUser = new Label("User ID:");
        TextField tfUser = new TextField();
        Label lblPass = new Label("Password:");
        PasswordField pf = new PasswordField();

        Button btnLogin = new Button("Login");
        Button btnCreate = new Button("Create Account");
        Button btnSkip = new Button("Skip Login");

        HBox hbButtons = new HBox(10, btnLogin, btnCreate, btnSkip);
        hbButtons.setAlignment(Pos.CENTER_RIGHT);

        grid.add(lblUser, 0, 0);
        grid.add(tfUser, 1, 0);
        grid.add(lblPass, 0, 1);
        grid.add(pf, 1, 1);
        grid.add(hbButtons, 1, 2);

        Scene scene = new Scene(grid);
        stage.setScene(scene);
        grid.applyCss();
        grid.layout();
        stage.sizeToScene();
        grid.setStyle(UIStyle.rootStyleBlue);
        
        stage.centerOnScreen();

        stage.show();

        btnLogin.setOnAction(e -> {
            String user = tfUser.getText().trim();
            String pass = pf.getText();
            AccountManager mgr = AccountManager.getInstance();
            if (mgr.authenticate(user, pass)) {
                mgr.setCurrentUser(user);
                if (onSuccess != null) onSuccess.run();
                stage.close();
            } else {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Login Failed");
                a.setHeaderText(null);
                a.setContentText("Invalid user ID or PIN. Please try again.");
                a.showAndWait();
            }
        });
        btnCreate.setOnAction(e -> showCreateAccountDialog(stage));

        btnSkip.setOnAction(e -> {
            AccountManager.getInstance().clearCurrentUser();
            if (onSuccess != null) onSuccess.run();
            stage.close();
        });

    }

    /**
     * Optional callback executed when user chooses to skip login or when login succeeds.
     */
    public Runnable onSuccess;

    private void showCreateAccountDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.setTitle("Create Account");
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);

        GridPane g = new GridPane();
        g.setPadding(new Insets(15));
        g.setHgap(10);
        g.setVgap(10);
        g.setAlignment(Pos.CENTER);
        AccountManager mgr = AccountManager.getInstance();
        String previewId = mgr.peekAvailableUserId();

        Label lid = new Label("New user ID - " + previewId + "  (write this down or save somewhere important)");
        Label lu = new Label("Name:");
        TextField tfu = new TextField();
        Label lp = new Label("4-digit PIN:");
        PasswordField pfp = new PasswordField();
        Button create = new Button("Create");
        Button cancel = new Button("Cancel");
        HBox hb = new HBox(10, create, cancel);
        hb.setAlignment(Pos.CENTER_RIGHT);

        g.add(lid, 0, 0, 2, 1);
        g.add(lu, 0, 1);
        g.add(tfu, 1, 1);
        g.add(lp, 0, 2);
        g.add(pfp, 1, 2);
        g.add(hb, 1, 3);

        Scene s = new Scene(g, 360, 180);
        dialog.setScene(s);
        dialog.show();

        create.setOnAction(ev -> {
            String name = tfu.getText().trim();
            String pin = pfp.getText().trim();

            if (name.isEmpty()) {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Invalid Name");
                err.setHeaderText(null);
                err.setContentText("Please enter a name for the account.");
                err.showAndWait();
                return;
            }

            // Validate PIN: must be exactly 4 digits
            if (!AccountManager.isValidPin(pin)) {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Invalid PIN");
                err.setHeaderText(null);
                err.setContentText("PIN must be exactly 4 numeric digits (e.g. 0423).");
                err.showAndWait();
                return;
            }

            try {
                // Use the previewed id when creating the account
                String generatedId = mgr.createAccount(previewId, name, pin);

                // Show confirmation including the generated user ID
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Account Created");
                a.setHeaderText(null);
                a.setContentText("Account created.\nUser ID: " + generatedId + "\n(Please remember your 4-digit PIN)");
                a.showAndWait();
                dialog.close();
            } catch (Exception ex) {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Create Account Error");
                err.setHeaderText(null);
                err.setContentText("Could not create account: " + ex.getMessage());
                err.showAndWait();
            }
        });

        cancel.setOnAction(ev2 -> dialog.close());
    }
}
