package photos.model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a photo on disk.
 * Date/time is taken from the file's last modification time.
 */
public class Photo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String filePath;
    private String caption;
    // "date taken" = last modification date/time of the file
    private LocalDateTime dateTime;
    private List<Tag> tags = new ArrayList<>();

    public Photo(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("filePath cannot be null/empty");
        }
        this.filePath = filePath;
        this.caption = "";
        this.dateTime = computeLastModified(filePath);
    }

    private LocalDateTime computeLastModified(String path) {
        try {
            File f = new File(path);
            FileTime ft = Files.getLastModifiedTime(f.toPath());
            Instant inst = ft.toInstant();
            // Drop nanos so equality works nicely
            return LocalDateTime.ofInstant(inst, ZoneId.systemDefault()).withNano(0);
        } catch (IOException e) {
            // Fallback: now (with 0 nanos)
            return LocalDateTime.now().withNano(0);
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = (caption == null) ? "" : caption;
    }

    /**
     * Date/time of the photo (last modified time of the file).
     * There is intentionally NO setter: date cannot be edited in the app.
     */
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public List<Tag> getTags() {
        return Collections.unmodifiableList(tags);
    }

    /**
     * Add a tag if not already present (by name+value).
     *
     * @return true if added, false if duplicate
     */
    public boolean addTag(Tag t) {
        if (t == null) {
            return false;
        }
        if (tags.contains(t)) {
            return false;
        }
        tags.add(t);
        return true;
    }

    public boolean removeTag(Tag t) {
        if (t == null) {
            return false;
        }
        return tags.remove(t);
    }

    @Override
    public String toString() {
        // For list views: show caption if present, else file name
        String name = new File(filePath).getName();
        if (caption != null && !caption.isBlank()) {
            return caption + " (" + name + ")";
        }
        return name;
    }
}
