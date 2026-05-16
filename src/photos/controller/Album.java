package photos.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single album belonging to a user.
 * Holds a list of Photo objects.
 */
public class Album implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private List<Photo> photos;

    public Album(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("album name cannot be null or empty");
        }
        this.name = name.trim();
        this.photos = new ArrayList<Photo>();
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("album name cannot be null or empty");
        }
        this.name = newName.trim();
    }

    public List<Photo> getPhotos() {
        return Collections.unmodifiableList(photos);
    }

    public int getPhotoCount() {
        return photos.size();
    }

    /**
     * Adds a photo to this album.
     * No duplicates of the same physical photo (same file path) in a single album.
     *
     * @param p photo to add
     * @return true if added, false if duplicate
     */
    public boolean addPhoto(Photo p) {
        if (p == null) {
            throw new IllegalArgumentException("photo cannot be null");
        }

        // prevent duplicate of same file path in this album
        for (Photo existing : photos) {
            if (existing.getFilePath().equals(p.getFilePath())) {
                return false;
            }
        }

        photos.add(p);
        return true;
    }

    /**
     * Removes a photo from this album.
     *
     * @param p photo to remove
     * @return true if removed, false if not found
     */
    public boolean removePhoto(Photo p) {
        if (p == null) {
            return false;
        }
        return photos.remove(p);
    }

    /**
     * Get the earliest photo date in this album, or null if no photos.
     */
    public LocalDate getEarliestDate() {
        if (photos.isEmpty()) {
            return null;
        }
        LocalDate earliest = null;
        for (Photo p : photos) {
            LocalDate d = p.getDateTime().toLocalDate();
            if (earliest == null || d.isBefore(earliest)) {
                earliest = d;
            }
        }
        return earliest;
    }

    /**
     * Get the latest photo date in this album, or null if no photos.
     */
    public LocalDate getLatestDate() {
        if (photos.isEmpty()) {
            return null;
        }
        LocalDate latest = null;
        for (Photo p : photos) {
            LocalDate d = p.getDateTime().toLocalDate();
            if (latest == null || d.isAfter(latest)) {
                latest = d;
            }
        }
        return latest;
    }

    @Override
    public String toString() {
        // Example: Vacation (5 photos, 2025-10-01 to 2025-10-15)
        // or: EmptyAlbum (0 photos, no photos)
        LocalDate earliest = getEarliestDate();
        LocalDate latest = getLatestDate();

        String range;
        if (earliest == null || latest == null) {
            range = "no photos";
        } else {
            range = earliest.toString() + " to " + latest.toString();
        }

        return name + " (" + getPhotoCount() + " photos, " + range + ")";
    }
}
