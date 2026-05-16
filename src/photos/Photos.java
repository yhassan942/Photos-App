package photos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import photos.model.User;
import photos.model.Album;
import photos.model.UserManager;

/**
 * Main entry point for the Photos application.
 */
public class Photos extends Application {

    private static UserManager userManager;
    private static User currentUser;
    private static Album currentAlbum;

    public static UserManager getUserManager() {
        return userManager;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static Album getCurrentAlbum() {
        return currentAlbum;
    }

    public static void setCurrentAlbum(Album album) {
        currentAlbum = album;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load model
        userManager = UserManager.load();

        FXMLLoader loader = new FXMLLoader(Photos.class.getResource("login.fxml"));
        Scene scene = new Scene(loader.load());

        primaryStage.setTitle("Photos - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        // Called when the application is closing
        if (userManager != null) {
            try {
                userManager.save();
            } catch (Exception e) {
                // ignore; app is exiting
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
