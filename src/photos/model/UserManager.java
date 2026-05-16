package photos.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages all users in the system and handles
 * serialization to and from disk.
 */
public class UserManager implements Serializable {

    private static final long serialVersionUID = 1L;

    // Where we store the serialized UserManager
    private static final String DATA_FILE = "data/users.dat";

    private List<User> users = new ArrayList<>();

    // ----- BASIC USER OPERATIONS -----

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public User getUser(String username) {
        if (username == null) return null;
        String target = username.trim();
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(target)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Adds a new user. Returns null if username already exists.
     * Also creates a "stock images" album for non-admin users.
     */
    public User addUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("username cannot be empty");
        }
        if (getUser(username) != null) {
            return null; // duplicate
        }
        User u = new User(username.trim());
        users.add(u);

        // Do not give admin albums (per spec)
        if (!u.getUsername().equalsIgnoreCase("admin")) {
            // Create per-user "stock images" album and populate it
            Album stockImages = u.addAlbum("stock images");
            populateStockImagesAlbum(stockImages);
        }

        return u;
    }

    /**
     * Deletes a user by username.
     */
    public boolean deleteUser(String username) {
        User u = getUser(username);
        if (u == null) return false;
        return users.remove(u);
    }

    // ----- LOAD / SAVE -----

    /**
     * Load UserManager from disk or create a new one.
     * Also ensures that:
     *  - admin user exists
     *  - stock user with album "stock" exists and has stock photos
     *  - every non-admin user has a "stock images" album with stock photos
     */
    public static UserManager load() {
        File f = new File(DATA_FILE);
        UserManager mgr = null;

        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                Object obj = ois.readObject();
                if (obj instanceof UserManager) {
                    mgr = (UserManager) obj;
                }
            } catch (Exception e) {
                // If the file is corrupted or incompatible, ignore it and start fresh
                mgr = null;
            }
        }

        if (mgr == null) {
            mgr = new UserManager();
        }

        // Ensure admin user exists
        mgr.ensureAdminUser();

        // 1) Ensure stock user/album + stock photos
        mgr.ensureStockUserAndAlbum();

        // 2) Ensure each non-admin user has "stock images" album with stock photos
        mgr.ensureStockImagesAlbumForAllUsers();

        try {
            mgr.save();
        } catch (IOException e) {
            // Ignore save errors here; app will attempt future saves
        }

        return mgr;
    }

    /**
     * Save this UserManager to disk.
     */
    public void save() throws IOException {
        File f = new File(DATA_FILE);
        File parent = f.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(this);
        }
    }

    // ----- ADMIN / STOCK HELPERS -----

    /**
     * Ensures that an 'admin' user exists.
     * Admin has no albums/photos.
     */
    private void ensureAdminUser() {
        if (getUser("admin") == null) {
            users.add(new User("admin"));
        }
    }

    /**
     * Ensures there is a "stock" user with an album "stock"
     * populated with the 7 stock images from the data folder.
     */
    private void ensureStockUserAndAlbum() {
        // Ensure stock user
        User stockUser = getUser("stock");
        if (stockUser == null) {
            stockUser = new User("stock");
            users.add(stockUser);
        }

        // Ensure stock album under stock user
        Album stockAlbum = stockUser.getAlbum("stock");
        if (stockAlbum == null) {
            stockAlbum = stockUser.addAlbum("stock");
        }

        // Populate that album with stock photos (if not already present)
        populateStockImagesAlbum(stockAlbum);
    }

    /**
     * Ensures every non-admin user has a "stock images" album with stock photos.
     */
    private void ensureStockImagesAlbumForAllUsers() {
        for (User u : users) {
            String uname = u.getUsername();
            if (uname.equalsIgnoreCase("admin")) {
                // per spec, admin does not use albums/photos
                continue;
            }

            Album stockImages = u.getAlbum("stock images");
            if (stockImages == null) {
                stockImages = u.addAlbum("stock images");
            }

            populateStockImagesAlbum(stockImages);
        }
    }

    /**
     * Populates a given album with the 7 stock images from /data
     * (adds missing ones only), with captions like "Red", "Green", etc.
     */
    private void populateStockImagesAlbum(Album album) {
        if (album == null) {
            return;
        }

        String[][] stockInfo = {
            { "stock_red.png",     "Red" },
            { "stock_green.png",   "Green" },
            { "stock_blue.png",    "Blue" },
            { "stock_yellow.png",  "Yellow" },
            { "stock_cyan.png",    "Cyan" },
            { "stock_magenta.png", "Magenta" },
            { "stock_gray.png",    "Gray" }
        };

        for (String[] info : stockInfo) {
            String fileName = info[0];
            String caption = info[1];

            File imageFile = new File("data", fileName);
            if (!imageFile.exists()) {
                // If the file isn't there, skip it
                continue;
            }

            String absPath = imageFile.getAbsolutePath();

            // Skip if album already has this photo path
            boolean alreadyThere = false;
            for (Photo p : album.getPhotos()) {
                if (p.getFilePath().equals(absPath)) {
                    alreadyThere = true;
                    break;
                }
            }

            if (!alreadyThere) {
                Photo p = new Photo(absPath);
                p.setCaption(caption);
                album.addPhoto(p);
            }
        }
    }
}
