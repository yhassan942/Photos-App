package photos.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single user of the photo application.
 * A user has:
 *  - a unique username
 *  - a list of albums (no duplicate album names per user)
 *
 * Passwords are optional and can be added later if desired.
 *
 * @author YourName
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private List<Album> albums;

    public User(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("username cannot be null or empty");
        }
        this.username = username.trim();
        this.albums = new ArrayList<Album>();
    }

    public String getUsername() {
        return username;
    }

    public List<Album> getAlbums() {
        return Collections.unmodifiableList(albums);
    }

    public int getAlbumCount() {
        return albums.size();
    }

    /**
     * Get an album by name (case-sensitive).
     *
     * @param name album name
     * @return album or null if not found
     */
    public Album getAlbum(String name) {
        if (name == null) return null;
        for (Album a : albums) {
            if (a.getName().equals(name)) {
                return a;
            }
        }
        return null;
    }

    /**
     * Creates and adds a new album with the given name.
     * Fails if an album with that name already exists.
     *
     * @param name album name
     * @return the created Album; null if duplicate name
     */
    public Album addAlbum(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("album name cannot be null or empty");
        }
        name = name.trim();
        if (getAlbum(name) != null) {
            // duplicate album name not allowed
            return null;
        }
        Album a = new Album(name);
        albums.add(a);
        return a;
    }

    /**
     * Deletes an album by name.
     *
     * @param name album name
     * @return true if deleted, false if not found
     */
    public boolean deleteAlbum(String name) {
        Album a = getAlbum(name);
        if (a == null) return false;
        return albums.remove(a);
    }

    /**
     * Renames an existing album, as long as the new name is not
     * already used by another album.
     *
     * @param oldName current name
     * @param newName desired new name
     * @return true if rename successful, false otherwise
     */
    public boolean renameAlbum(String oldName, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("new album name cannot be null or empty");
        }
        newName = newName.trim();

        Album a = getAlbum(oldName);
        if (a == null) {
            return false; // album to rename not found
        }
        // check for conflict with some *other* album
        Album other = getAlbum(newName);
        if (other != null && other != a) {
            return false; // duplicate name would be created
        }
        a.setName(newName);
        return true;
    }

    @Override
    public String toString() {
        return "User[" + username + ", albums=" + getAlbumCount() + "]";
    }
}
