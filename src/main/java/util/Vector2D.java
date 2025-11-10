package util;

// Simple but incredibly useful 2D vector class used to track positions, velocity, and sizes
import java.util.Objects;

public class Vector2D {
    // Fields are public for greatly increased ease of use
    public double x;
    public double y;

    public Vector2D() {
        this.x = 0;
        this.y = 0;
    }

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Vector2D v) {
        this.x = v.x;
        this.y = v.y;
    }

    public Vector2D add(Vector2D v) {
        return new Vector2D(x + v.x, y + v.y);
    }

    public Vector2D sub(Vector2D v) {
        return new Vector2D(x - v.x, y - v.y);
    }

    // The following 2 overrides allow the Vector2D to work as a key in HashMaps, useful for TileMap position tracking
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;  // Reference check
        if (o == null || getClass() != o.getClass()) return false;  // Class Check
        Vector2D vector2D = (Vector2D) o;
        return x == vector2D.x && y == vector2D.y;  // Field check
    }

}
