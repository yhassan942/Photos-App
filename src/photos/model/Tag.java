package photos.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Simple tag: name=value.
 * Example: ("person","Alice"), ("location","New Brunswick")
 */
public class Tag implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String value;

    public Tag(String name, String value) {
        if (name == null || name.trim().isEmpty()
                || value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("tag name/value cannot be empty");
        }
        this.name = name.trim();
        this.value = value.trim();
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    // Equality: case-insensitive name+value
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag other = (Tag) o;
        return name.equalsIgnoreCase(other.name)
            && value.equalsIgnoreCase(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase(), value.toLowerCase());
    }

    @Override
    public String toString() {
        return name + "=" + value;
    }
}
