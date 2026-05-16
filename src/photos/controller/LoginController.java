package photos.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import photos.Photos;
import photos.model.User;
import photos.model.UserManager;

/**
 * Controller for the login screen.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        if (username == null || username.trim().isEmpty()) {
            showError("Username is required.");
            return;
        }
        username = username.trim();

        UserManager manager = Photos.getUserManager();

        // Find existing user or create new
        User user = manager.getUser(username);
        if (user == null) {
            user = manager.addUser(username);
        }

        try {
            manager.save();
        } catch (Exception e) {
            showError("Failed to save users: " + e.getMessage());
            return;
        }

        // track current user globally
        Photos.setCurrentUser(user);

        Stage stage = (Stage) usernameField.getScene().getWindow();

        if ("admin".equals(username)) {
            // Load the admin screen
            try {
                FXMLLoader loader = new FXMLLoader(Photos.class.getResource("admin.fxml"));
                Scene adminScene = new Scene(loader.load());
                stage.setTitle("Photos - Admin");
                stage.setScene(adminScene);
            } catch (Exception e) {
                e.printStackTrace();
                showError("Failed to load admin screen: " + e.getMessage());
            }
        } else {
            // Load the albums screen for this user
            try {
                FXMLLoader loader = new FXMLLoader(Photos.class.getResource("albums.fxml"));
                Scene albumsScene = new Scene(loader.load());
                stage.setTitle("Photos - Albums");
                stage.setScene(albumsScene);
            } catch (Exception e) {
                e.printStackTrace();
                showError("Failed to load albums screen: " + e.getMessage());
            }
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
