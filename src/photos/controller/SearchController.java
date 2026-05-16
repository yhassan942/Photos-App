package photos.controller;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import photos.Photos;
import photos.model.Album;
import photos.model.Photo;
import photos.model.Tag;
import photos.model.User;
import photos.model.UserManager;

/**
 * Controller for searching photos:
 *  - by date range
 *  - by tag(s): single, AND, OR
 * and creating an album from the search results.
 */
public class SearchController {

    // Date search controls
    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    // Tag search controls
    @FXML
    private TextField tag1NameField;

    @FXML
    private TextField tag1ValueField;

    @FXML
    private TextField tag2NameField;

    @FXML
    private TextField tag2ValueField;

    @FXML
    private RadioButton rbSingle;

    @FXML
    private RadioButton rbAnd;

    @FXML
    private RadioButton rbOr;

    // Shared results + info
    @FXML
    private ListView<Photo> resultsListView;

    @FXML
    private Label infoLabel;

    private UserManager userManager;
    private User currentUser;
    private ObservableList<Photo> results;

    @FXML
    private void initialize() {
        userManager = Photos.getUserManager();
        currentUser = Photos.getCurrentUser();

        if (currentUser == null) {
            showError("No current user set.");
            return;
        }

        results = FXCollections.observableArrayList();
        resultsListView.setItems(results);
        infoLabel.setText("No results yet.");
    }

    // --------- SEARCH BY DATE RANGE ---------

    @FXML
    private void handleSearchByDate() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();

        if (from == null || to == null) {
            showError("Please select both a 'From' and 'To' date.");
            return;
        }

        if (from.isAfter(to)) {
            showError("'From' date cannot be after 'To' date.");
            return;
        }

        Set<Photo> matches = new LinkedHashSet<>();

        for (Album album : currentUser.getAlbums()) {
            for (Photo p : album.getPhotos()) {
                LocalDate d = p.getDateTime().toLocalDate();
                if ((d.isEqual(from) || d.isAfter(from)) &&
                    (d.isEqual(to)   || d.isBefore(to))) {
                    matches.add(p);
                }
            }
        }

        results.setAll(matches);

        if (matches.isEmpty()) {
            infoLabel.setText("No photos found in that date range.");
        } else {
            infoLabel.setText("Found " + matches.size() + " photo(s) by date.");
        }
    }

    // --------- SEARCH BY TAG(S) ---------

    @FXML
    private void handleSearchByTag() {
        String n1 = trimOrNull(tag1NameField.getText());
        String v1 = trimOrNull(tag1ValueField.getText());
        String n2 = trimOrNull(tag2NameField.getText());
        String v2 = trimOrNull(tag2ValueField.getText());

        if (n1 == null || v1 == null) {
            showError("Tag 1 name and value are required.");
            return;
        }

        boolean useSecond = rbAnd.isSelected() || rbOr.isSelected();
        if (useSecond && (n2 == null || v2 == null)) {
            showError("Tag 2 name and value are required for AND/OR searches.");
            return;
        }

        Set<Photo> matches = new LinkedHashSet<>();

        for (Album album : currentUser.getAlbums()) {
            for (Photo p : album.getPhotos()) {
                boolean has1 = hasTag(p, n1, v1);
                boolean has2 = useSecond ? hasTag(p, n2, v2) : false;

                boolean match;
                if (rbAnd.isSelected()) {
                    match = has1 && has2;
                } else if (rbOr.isSelected()) {
                    match = has1 || has2;
                } else {
                    // Single tag (default)
                    match = has1;
                }

                if (match) {
                    matches.add(p);
                }
            }
        }

        results.setAll(matches);

        if (matches.isEmpty()) {
            infoLabel.setText("No photos found for that tag query.");
        } else {
            infoLabel.setText("Found " + matches.size() + " photo(s) by tag.");
        }
    }

    private boolean hasTag(Photo p, String name, String value) {
        // case-insensitive match on name + value
        for (Tag t : p.getTags()) {
            if (t.getName().equalsIgnoreCase(name) &&
                t.getValue().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    // --------- CREATE ALBUM FROM RESULTS ---------

    @FXML
    private void handleCreateAlbumFromResults() {
        if (results.isEmpty()) {
            showError("There are no search results to create an album from.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Album from Results");
        dialog.setHeaderText("Create album from search results");
        dialog.setContentText("Enter new album name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return; // cancelled
        }

        String name = result.get().trim();
        if (name.isEmpty()) {
            showError("Album name cannot be empty.");
            return;
        }

        Album newAlbum = currentUser.addAlbum(name);
        if (newAlbum == null) {
            showError("Album '" + name + "' already exists.");
            return;
        }

        for (Photo p : results) {
            newAlbum.addPhoto(p); // duplicate prevention handled in addPhoto
        }

        try {
            userManager.save();
        } catch (Exception e) {
            showError("Failed to save: " + e.getMessage());
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Album Created");
        alert.setHeaderText(null);
        alert.setContentText("Album '" + name + "' created with " + results.size() + " photo(s).");
        alert.showAndWait();
    }

    // --------- NAVIGATION ---------

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(Photos.class.getResource("albums.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) resultsListView.getScene().getWindow();
            stage.setTitle("Photos - Albums");
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to go back to albums: " + e.getMessage());
        }
    }

    // --------- HELPERS ---------

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Search Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
