package photos.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import photos.Photos;
import photos.model.User;
import photos.model.UserManager;

/**
 * Controller for the admin user subsystem.
 * Admin can list, add, and delete users.
 */
public class AdminController {

    @FXML
    private Label adminLabel;          // may be null if not present in admin.fxml

    @FXML
    private ListView<String> userListView;

    @FXML
    private TextField usernameField;

    private UserManager userManager;
    private ObservableList<String> usernames;

    @FXML
    private void initialize() {
        userManager = Photos.getUserManager();
        usernames = FXCollections.observableArrayList();
        userListView.setItems(usernames);

        // adminLabel is optional; avoid NPE if admin.fxml doesn't define it
        if (adminLabel != null) {
            adminLabel.setText("Admin - User Management");
        }

        refreshUserList();
    }

    private void refreshUserList() {
        usernames.clear();
        for (User u : userManager.getUsers()) {
            usernames.add(u.getUsername());
        }
    }

    @FXML
    private void handleAddUser() {
        String name = usernameField.getText();
        if (name == null || name.trim().isEmpty()) {
            showError("Username cannot be empty.");
            return;
        }
        name = name.trim();

        if (userManager.addUser(name) == null) {
            showError("User '" + name + "' already exists.");
            return;
        }

        try {
            userManager.save();
        } catch (Exception e) {
            showError("Failed to save: " + e.getMessage());
            return;
        }

        usernameField.clear();
        refreshUserList();
    }

    @FXML
    private void handleDeleteUser() {
        String selected = userListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a user to delete.");
            return;
        }

        if (selected.equalsIgnoreCase("admin")) {
            showError("Cannot delete admin user.");
            return;
        }

        if (!userManager.deleteUser(selected)) {
            showError("Could not delete user.");
            return;
        }

        try {
            userManager.save();
        } catch (Exception e) {
            showError("Failed to save: " + e.getMessage());
            return;
        }

        refreshUserList();
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(Photos.class.getResource("login.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) userListView.getScene().getWindow();
            stage.setTitle("Photos - Login");

            Photos.setCurrentUser(null);
            Photos.setCurrentAlbum(null);

            stage.setScene(scene);
        } catch (Exception e) {
            showError("Failed to return to login: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Admin Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
