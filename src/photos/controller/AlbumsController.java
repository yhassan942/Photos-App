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
import photos.model.Album;
import photos.model.User;
import photos.model.UserManager;

/**
 * Controller for the normal user albums screen.
 */
public class AlbumsController {

    @FXML
    private Label userLabel;

    @FXML
    private ListView<Album> albumListView;

    @FXML
    private TextField albumNameField;

    private UserManager userManager;
    private User currentUser;
    private ObservableList<Album> albums;

    @FXML
    private void initialize() {
        userManager = Photos.getUserManager();
        currentUser = Photos.getCurrentUser();

        if (currentUser == null) {
            showError("No current user set.");
            return;
        }

        userLabel.setText("Albums - " + currentUser.getUsername());

        albums = FXCollections.observableArrayList(currentUser.getAlbums());
        albumListView.setItems(albums);
    }

    private void refreshAlbumList() {
        albums.setAll(currentUser.getAlbums());
    }

    @FXML
    private void handleAddAlbum() {
        String name = albumNameField.getText();
        if (name == null || name.trim().isEmpty()) {
            showError("Album name cannot be empty.");
            return;
        }
        name = name.trim();

        if (currentUser.addAlbum(name) == null) {
            showError("Album '" + name + "' already exists.");
            return;
        }

        try {
            userManager.save();
        } catch (Exception e) {
            showError("Failed to save: " + e.getMessage());
            return;
        }

        albumNameField.clear();
        refreshAlbumList();
    }

    @FXML
    private void handleRenameAlbum() {
        Album selected = albumListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select an album to rename.");
            return;
        }

        String newName = albumNameField.getText();
        if (newName == null || newName.trim().isEmpty()) {
            showError("Enter a new name in the text field.");
            return;
        }
        newName = newName.trim();

        boolean ok = currentUser.renameAlbum(selected.getName(), newName);
        if (!ok) {
            showError("Could not rename album. An album with that name may already exist.");
            return;
        }

        try {
            userManager.save();
        } catch (Exception e) {
            showError("Failed to save: " + e.getMessage());
            return;
        }

        albumNameField.clear();
        refreshAlbumList();
    }

    @FXML
    private void handleDeleteAlbum() {
        Album selected = albumListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select an album to delete.");
            return;
        }

        boolean ok = currentUser.deleteAlbum(selected.getName());
        if (!ok) {
            showError("Could not delete album.");
            return;
        }

        try {
            userManager.save();
        } catch (Exception e) {
            showError("Failed to save: " + e.getMessage());
            return;
        }

        refreshAlbumList();
    }

    @FXML
    private void handleOpenAlbum() {
        Album selected = albumListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select an album to open.");
            return;
        }

        // Remember which album is open
        Photos.setCurrentAlbum(selected);

        try {
            FXMLLoader loader = new FXMLLoader(Photos.class.getResource("album_photos.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) albumListView.getScene().getWindow();
            stage.setTitle("Photos - Album: " + selected.getName());
            stage.setScene(scene);
        } catch (Exception e) {
            showError("Failed to open album: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        try {
            FXMLLoader loader = new FXMLLoader(Photos.class.getResource("search.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) albumListView.getScene().getWindow();
            stage.setTitle("Photos - Search");
            stage.setScene(scene);
        } catch (Exception e) {
            showError("Failed to open search screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(Photos.class.getResource("login.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) albumListView.getScene().getWindow();
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
        alert.setTitle("Album Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
