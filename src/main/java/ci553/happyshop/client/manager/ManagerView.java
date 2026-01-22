package ci553.happyshop.client.manager;

import ci553.happyshop.utility.UIStyle; 
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.function.BiConsumer;
import javafx.event.ActionEvent;
import ci553.happyshop.utility.SoundManager;



public class ManagerView {
    public ManagerController controller;

    private final TableView<StaffUserRow> table = new TableView<>();
    private final ObservableList<StaffUserRow> items = FXCollections.observableArrayList();

    public void start(Stage stage) {
        stage.setTitle("Manager - Staff Accounts");

        Label title = new Label("Staff Accounts");
        title.setStyle(UIStyle.labelTitleStyle);

        setupTable();

        Button btnEdit = new Button("Edit");
        Button btnDelete = new Button("Delete");
        Button btnRefresh = new Button("Refresh");
        Button btnClose = new Button("Close");

        btnEdit.setStyle(UIStyle.buttonStyle);
        btnDelete.setStyle(UIStyle.buttonStyle);
        btnRefresh.setStyle(UIStyle.buttonStyle);
        btnClose.setStyle(UIStyle.buttonStyle);

        btnEdit.setOnAction(e -> { if (controller != null) controller.editSelected(); });
        btnDelete.setOnAction(e -> { if (controller != null) controller.deleteSelected(); });
        btnRefresh.setOnAction(e -> { if (controller != null) controller.refresh(); });
        btnClose.setOnAction(e -> stage.close());

        HBox buttons = new HBox(10, btnEdit, btnDelete, btnRefresh, btnClose);
        buttons.setAlignment(Pos.CENTER);

        VBox root = new VBox(12, title, table, buttons);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle(UIStyle.rootStyle);

        root.addEventFilter(ActionEvent.ACTION, e -> {
            if (e.getTarget() instanceof Button) {
                SoundManager.click();
            }
        });

        Scene scene = new Scene(root, 650, 500);
        stage.setScene(scene);
        stage.show();

        if (controller != null) {
            controller.refresh();
            controller.startAutoRefresh();
        }
    }

    private void setupTable() {
        table.setItems(items);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<StaffUserRow, String> colId = new TableColumn<>("User ID");
        colId.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getUserId()));

        TableColumn<StaffUserRow, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getName()));

        TableColumn<StaffUserRow, String> colRole = new TableColumn<>("Role");
        colRole.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getRole().name()));

        table.getColumns().setAll(colId, colName, colRole);
    }

    public void setTableItems(List<StaffUserRow> rows) {
        items.setAll(rows);
    }

    public StaffUserRow getSelected() {
        return table.getSelectionModel().getSelectedItem();
    }

    public void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    public void showEditDialog(StaffUserRow row, BiConsumer<String, String> onSave) {
        Stage dialog = new Stage();
        dialog.setTitle("Edit Staff User");
        dialog.initModality(Modality.APPLICATION_MODAL);

        Label labId = new Label("User ID: " + row.getUserId());

        TextField tfName = new TextField(row.getName());
        tfName.setPromptText("Name");

        ComboBox<String> cbRole = new ComboBox<>();
        cbRole.getItems().addAll("PICKER", "TRACKER", "WAREHOUSE", "MANAGER");
        cbRole.setValue(row.getRole().name());

        Button btnSave = new Button("Save");
        Button btnCancel = new Button("Cancel");
        btnSave.setStyle(UIStyle.buttonStyle);
        btnCancel.setStyle(UIStyle.buttonStyle);

        btnSave.setOnAction(e -> {
            String newName = tfName.getText().trim();
            String newRole = cbRole.getValue();
            if (newName.isEmpty()) {
                ci553.happyshop.utility.SoundManager.error();
                showInfo("Invalid input", "Name cannot be empty.");
                return;
            }
            if (onSave != null) onSave.accept(newName, newRole);
            ci553.happyshop.utility.SoundManager.success();
            dialog.close();
        });

        btnCancel.setOnAction(e -> dialog.close());

        HBox controls = new HBox(10, btnSave, btnCancel);
        controls.setAlignment(Pos.CENTER);

        VBox root = new VBox(10,
                labId,
                new Label("Name:"), tfName,
                new Label("Role:"), cbRole,
                controls
        );
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_LEFT);

        dialog.setScene(new Scene(root, 350, 250));
        dialog.showAndWait();
    }
}
