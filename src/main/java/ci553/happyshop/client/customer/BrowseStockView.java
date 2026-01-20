package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Product;
import ci553.happyshop.utility.UIStyle;
import ci553.happyshop.utility.WindowBounds;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Displays current stock of products when browsing. 
 */
public final class BrowseStockView {

    private static final int WIDTH = 520;
    private static final int HEIGHT = 520;

    public void show(List<Product> products, WindowBounds ownerBounds) {
       // System.out.println("[BrowseStockView] show() products size = " + (products == null ? -1 : products.size()));

        Stage window = new Stage();
        window.setTitle("Current Stock");

        Label title = new Label("Current Stock");
        title.setStyle(UIStyle.labelTitleStyle);

        TableView<Product> table = new TableView<>();
       table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(HEIGHT - 120);

        TableColumn<Product, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("productId"));

        TableColumn<Product, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("productDescription"));

        TableColumn<Product, Double> colPrice = new TableColumn<>("Price (£)");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        TableColumn<Product, Integer> colStock = new TableColumn<>("In Stock");
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        table.getColumns().addAll(colId, colName, colPrice, colStock);

        ObservableList<Product> items = FXCollections.observableArrayList(products);
        //System.out.println("[BrowseStockView] Table items set. table.getItems().size() = " + table.getItems().size());
        table.setItems(items);

        Button btnClose = new Button("Close");
        btnClose.setStyle(UIStyle.buttonStyle);
        btnClose.setOnAction(e -> {
            ci553.happyshop.utility.SoundManager.click();
            window.close();
        });

        HBox controls = new HBox(btnClose);
        controls.setAlignment(Pos.CENTER);

        VBox root = new VBox(12, title, table, controls);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle(UIStyle.rootStyle);

        Scene scene = new Scene(root);
        window.setScene(scene);

        window.sizeToScene();
        window.setMinWidth(window.getWidth());
        window.setMinHeight(window.getHeight());

        window.show();
    }
}
