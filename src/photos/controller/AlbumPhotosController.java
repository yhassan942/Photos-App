package photos.controller;

import java.io.File;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import photos.Photos;
import photos.model.Album;
import photos.model.Photo;
import photos.model.Tag;
import photos.model.User;
import photos.model.UserManager;

/**
 * Controller for viewing and editing photos inside a single album,
 * including captions, tags, copy/move, and manual slideshow.
 */
public class AlbumPhotosController {

    @FXML
    private Label albumLabel;

    @FXML
    private ListView<Photo> photoListView;

    @FXML
    private ImageView imageView;

    @FXML
    private TextField captionField;

    @FXML
    private Label dateLabel;

    @FXML
    private ListView<Tag> tagListView;

    @FXML
    private TextField tagNameField;

    @FXML
    private TextField tagValueField;

    private Album currentAlbum;
    private UserManager userManager;
    private ObservableList<Photo> photos;
    private ObservableList<Tag> tags;

    @FXML
    private void initialize() {
        userManager = Photos.getUserManager();
        currentAlbum = Photos.getCurrentAlbum();

        if (currentAlbum == null) {
            showError("No album selected.");
            return;
        }

        albumLabel.setText("Album: " + currentAlbum.getName());

        photos = FXCollections.observableArrayList(currentAlbum.getPhotos());
        photoListView.setItems(photos);

        tags = FXCollections.observableArrayList();
        tagListView.setItems(tags);

        // When selection changes, update caption, image, date, and tags
        photoListView.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> {
            if (newP != null) {
                captionField.setText(newP.getCaption());
                if (dateLabel != null) {
                    dateLabel.setText("Date: " + newP.getDateTime().toString());
                }
                loadImage(newP);
                refreshTagList(newP);
            } else {
                captionField.clear();
                if (dateLabel != null) {
                    dateLabel.setText("Date: -");
                }
                imageView.setImage(null);
                tags.clear();
            }
        });

        // Show thumbnails + caption in the photo list (required: "thumbnail images and captions")
        photoListView.setCellFactory(list -> new ListCell<Photo>() {
            private final ImageView thumbView = new ImageView();

            {
                thumbView.setFitWidth(80);
                thumbView.setFitHeight(60);
                thumbView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Photo item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                // Caption or filename
                String text = item.getCaption();
                if (text == null || text.isBlank()) {
                    text = new File(item.getFilePath()).getName();
                }
                setText(text);

                // Thumbnail image
                try {
                    File file = new File(item.getFilePath());
                    if (file.exists()) {
                        Image img = new Image(file.toURI().toString(), 80, 60, true, true);
                        thumbView.setImage(img);
                        setGraphic(thumbView);
                    } else {
                        thumbView.setImage(null);
                        setGraphic(null);
                    }
                } catch (Exception e) {
                    thumbView.setImage(null);
                    setGraphic(null);
                }
            }
        });
    }

    private void refreshPhotoList() {
        photos.setAll(currentAlbum.getPhotos());
    }

    private void refreshTagList(Photo p) {
        tags.setAll(p.getTags());
    }

    private void loadImage(Photo p) {
        try {
            File file = new File(p.getFilePath());
            if (!file.exists()) {
                imageView.setImage(null);
                return;
            }
            Image img = new Image(file.toURI().toString());
            imageView.setImage(img);
        } catch (Exception e) {
            imageView.setImage(null);
        }
    }

    // ---------- Photo management ----------

    @FXML
    private void handleAddPhoto() {
        Stage stage = (Stage) photoListView.getScene().getWindow();

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Photo");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.bmp", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selected = chooser.showOpenDialog(stage);
        if (selected == null) {
            return;
        }

        Photo p = new Photo(selected.getAbsolutePath());
        if (!currentAlbum.addPhoto(p)) {
            showError("This photo is already in the album.");
            return;
        }

        saveUsers();
        refreshPhotoList();
        photoListView.getSelectionModel().select(p);
    }

    @FXML
    private void handleRemovePhoto() {
        Photo selected = photoListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a photo to remove.");
            return;
        }

        if (!currentAlbum.removePhoto(selected)) {
            showError("Could not remove photo.");
            return;
        }

        saveUsers();
        captionField.clear();
        if (dateLabel != null) {
            dateLabel.setText("Date: -");
        }
        imageView.setImage(null);
        tags.clear();
        refreshPhotoList();
    }

    @FXML
    private void handleSaveCaption() {
        Photo selected = photoListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a photo first.");
            return;
        }

        String caption = captionField.getText();
        selected.setCaption(caption);

        saveUsers();
        // refresh list to update toString display / thumbnails text
        refreshPhotoList();
    }

    // ---------- Tags ----------

    @FXML
    private void handleAddTag() {
        Photo selected = photoListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a photo first.");
            return;
        }

        String name = tagNameField.getText();
        String value = tagValueField.getText();

        if (name == null || name.trim().isEmpty()
                || value == null || value.trim().isEmpty()) {
            showError("Tag name and value cannot be empty.");
            return;
        }

        Tag t = new Tag(name.trim(), value.trim());
        if (!selected.addTag(t)) {
            showError("This tag already exists for the photo.");
            return;
        }

        saveUsers();
        tagNameField.clear();
        tagValueField.clear();
        refreshTagList(selected);
    }

    @FXML
    private void handleDeleteTag() {
        Photo selected = photoListView.getSelectionModel().getSelectedItem();
        Tag tag = tagListView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Select a photo first.");
            return;
        }

        if (tag == null) {
            showError("Select a tag to delete.");
            return;
        }

        if (!selected.removeTag(tag)) {
            showError("Could not remove tag.");
            return;
        }

        saveUsers();
        refreshTagList(selected);
    }

    // ---------- Manual slideshow (Prev / Next) ----------

    @FXML
    private void handlePrevPhoto() {
        int index = photoListView.getSelectionModel().getSelectedIndex();
        if (photos.isEmpty()) {
            return;
        }
        if (index <= 0) {
            photoListView.getSelectionModel().select(photos.size() - 1);
        } else {
            photoListView.getSelectionModel().select(index - 1);
        }
    }

    @FXML
    private void handleNextPhoto() {
        if (photos.isEmpty()) {
            return;
        }
        int index = photoListView.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            photoListView.getSelectionModel().select(0);
        } else {
            int next = (index + 1) % photos.size();
            photoListView.getSelectionModel().select(next);
        }
    }

    // ---------- Copy / Move between albums ----------

    @FXML
    private void handleCopyPhoto() {
        Photo selected = photoListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a photo to copy.");
            return;
        }

        Album dest = promptForDestinationAlbum();
        if (dest == null) {
            return; // user cancelled or invalid
        }

        if (dest == currentAlbum) {
            showError("Destination album is the same as the current album.");
            return;
        }

        // Add the SAME Photo object to the other album (shared photo)
        if (!dest.addPhoto(selected)) {
            showError("Photo is already in destination album.");
            return;
        }

        saveUsers();
    }

    @FXML
    private void handleMovePhoto() {
        Photo selected = photoListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a photo to move.");
            return;
        }

        Album dest = promptForDestinationAlbum();
        if (dest == null) {
            return; // cancelled/invalid
        }

        if (dest == currentAlbum) {
            showError("Destination album is the same as the current album.");
            return;
        }

        if (!dest.addPhoto(selected)) {
            showError("Photo is already in destination album.");
            return;
        }

        // Remove from current album AFTER successful add
        currentAlbum.removePhoto(selected);

        saveUsers();
        refreshPhotoList();
    }

    private Album promptForDestinationAlbum() {
        User currentUser = Photos.getCurrentUser();
        if (currentUser == null) {
            showError("No current user set.");
            return null;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Destination Album");
        dialog.setHeaderText("Copy/Move Photo");
        dialog.setContentText("Enter destination album name:");

        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent()) {
            return null; // cancelled
        }

        String name = result.get().trim();
        if (name.isEmpty()) {
            showError("Album name cannot be empty.");
            return null;
        }

        Album dest = currentUser.getAlbum(name);
        if (dest == null) {
            showError("Album '" + name + "' does not exist for this user.");
            return null;
        }

        return dest;
    }

    // ---------- Navigation ----------

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(Photos.class.getResource("albums.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) photoListView.getScene().getWindow();
            stage.setTitle("Photos - Albums");
            stage.setScene(scene);
        } catch (Exception e) {
            showError("Failed to go back to albums: " + e.getMessage());
        }
    }

    // ---------- Helpers ----------

    private void saveUsers() {
        try {
            userManager.save();
        } catch (Exception e) {
            showError("Failed to save: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Album Photos Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
