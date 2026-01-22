package ci553.happyshop.client.manager;

import ci553.happyshop.client.UserRole;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.util.Duration;

import java.util.Optional;

public class ManagerController {
    public ManagerModel model;
    private Timeline autoRefreshTimeline;

    public void refresh() {
        if (model == null || model.view == null) return;
        model.view.setTableItems(model.loadStaffUsers());
    }

    public void deleteSelected() {
        if (model == null || model.view == null) return;

        StaffUserRow selected = model.view.getSelected();
        if (selected == null) {
            ci553.happyshop.utility.SoundManager.error();
            model.view.showInfo("Delete user", "Please select a user first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm delete");
        confirm.setHeaderText("Delete user: " + selected.getUserId());
        confirm.setContentText("This cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ci553.happyshop.utility.SoundManager.success();
            model.deleteUser(selected.getUserId());
            refresh();
        }
    }

    public void editSelected() {
        if (model == null || model.view == null) return;

        StaffUserRow selected = model.view.getSelected();
        if (selected == null) {
            ci553.happyshop.utility.SoundManager.error();
            model.view.showInfo("Edit user", "Please select a user first.");
            return;
        }

        model.view.showEditDialog(selected, (newName, newRole) -> {
            ci553.happyshop.utility.SoundManager.success();
            model.updateUser(selected.getUserId(), newName, UserRole.fromString(newRole));
            refresh();
        });
    }

    public void startAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> refresh()));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    public void stopAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
    }
}
