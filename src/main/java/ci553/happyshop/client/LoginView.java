package ci553.happyshop.client;

import ci553.happyshop.utility.UIStyle;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginView {

    private TextField tfUser;
    private PasswordField pf;
    private Stage stage;

    
    private Stage createDialog;
    private TextField tfName;
    private PasswordField pfPin;
    private ComboBox<UserRole> cbRole;
    private String previewId;

 
    public Runnable onSuccess;

    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("HappyShop - Login");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        Label lblUser = new Label("User ID:");
        tfUser = new TextField();

        Label lblPass = new Label("Password:");
        pf = new PasswordField();

        Button btnLogin = new Button("Login");
        Button btnCreate = new Button("Create Account");
        Button btnSkip = new Button("Skip Login");

  
        btnLogin.setOnAction(this::handleLogin);
        btnCreate.setOnAction(this::handleOpenCreateAccountDialog);
        btnSkip.setOnAction(this::handleSkipLogin);

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
    }


    private void handleLogin(ActionEvent e) {
        String user = tfUser.getText() == null ? "" : tfUser.getText().trim();
        String pass = pf.getText() == null ? "" : pf.getText();

        AccountManager mgr = AccountManager.getInstance();

        if (mgr.authenticate(user, pass)) {
            ci553.happyshop.utility.SoundManager.success();

            mgr.setCurrentUser(user);
            mgr.setLoginState(LoginState.AUTHENTICATED);

            if (onSuccess != null) onSuccess.run();
            stage.close();
        } else {
            ci553.happyshop.utility.SoundManager.error();

            mgr.setLoginState(LoginState.INVALID);
            showError("Login Failed", "Invalid user ID or PIN. Please try again.");
        }
    }

    private void handleSkipLogin(ActionEvent e) {
        ci553.happyshop.utility.SoundManager.click();

        AccountManager mgr = AccountManager.getInstance();
        mgr.clearCurrentUser();
        mgr.setLoginState(LoginState.SKIPPED);

        stage.close();

        try {
            Main main = new Main();
            main.startAllClients();
        } catch (IOException ex) {
            ex.printStackTrace();
            showError("Start Error", "Could not start clients: " + ex.getMessage());
        }
    }

    private void handleOpenCreateAccountDialog(ActionEvent e) {
        showCreateAccountDialog(stage);
    }


    private void showCreateAccountDialog(Stage owner) {
        AccountManager mgr = AccountManager.getInstance();
        previewId = mgr.peekAvailableUserId();

        createDialog = new Stage();
        createDialog.setTitle("Create Account");
        createDialog.initOwner(owner);
        createDialog.initModality(Modality.APPLICATION_MODAL);

        GridPane g = new GridPane();
        g.setPadding(new Insets(15));
        g.setHgap(10);
        g.setVgap(10);
        g.setAlignment(Pos.CENTER);

        Label lid = new Label("New user ID - " + previewId + "  (write this down or save somewhere important)");

        Label lu = new Label("Name:");
        tfName = new TextField();

        Label lr = new Label("Role:");
        cbRole = new ComboBox<>();
        cbRole.getItems().addAll(
                UserRole.CUSTOMER,
                UserRole.PICKER,
                UserRole.TRACKER,
                UserRole.WAREHOUSE,
                UserRole.MANAGER
        );
        cbRole.setValue(UserRole.CUSTOMER);

        Label lp = new Label("4-digit PIN:");
        pfPin = new PasswordField();

        Button btnCreate = new Button("Create");
        Button btnCancel = new Button("Cancel");

      
        btnCreate.setOnAction(this::handleCreateAccount);
        btnCancel.setOnAction(this::handleCancelCreateAccount);

        HBox hb = new HBox(10, btnCreate, btnCancel);
        hb.setAlignment(Pos.CENTER_RIGHT);

        g.add(lid, 0, 0, 2, 1);
        g.add(lu, 0, 1);
        g.add(tfName, 1, 1);

        g.add(lr, 0, 2);
        g.add(cbRole, 1, 2);

        g.add(lp, 0, 3);
        g.add(pfPin, 1, 3);

        g.add(hb, 1, 4);

        Scene s = new Scene(g, 420, 210);
        createDialog.setScene(s);
        createDialog.show();
    }


    private void handleCreateAccount(ActionEvent e) {
        AccountManager mgr = AccountManager.getInstance();

        String name = tfName.getText() == null ? "" : tfName.getText().trim();
        String pin = pfPin.getText() == null ? "" : pfPin.getText().trim();
        UserRole role = cbRole.getValue();

        if (name.isEmpty()) {
            ci553.happyshop.utility.SoundManager.error();
            showError("Invalid Name", "Please enter a name for the account.");
            return;
        }

        if (!AccountManager.isValidPin(pin)) {
            ci553.happyshop.utility.SoundManager.error();
            showError("Invalid PIN", "PIN must be exactly 4 numeric digits (e.g. 0423).");
            return;
        }

        try {
            String generatedId = mgr.createAccount(previewId, name, pin, role);

            ci553.happyshop.utility.SoundManager.success();
            showInfo("Account Created",
                    "Account created.\nUser ID: " + generatedId + "\n(Please remember your 4-digit PIN)");
            createDialog.close();
        } catch (Exception ex) {
            ci553.happyshop.utility.SoundManager.error();
            showError("Create Account Error", "Could not create account: " + ex.getMessage());
        }
    }

    private void handleCancelCreateAccount(ActionEvent e) {
        if (createDialog != null) createDialog.close();
    }

 
    private static void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    private static void showInfo(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
