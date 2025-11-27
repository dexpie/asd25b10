import java.awt.Rectangle;

public class Tile {
    int id, x, y, size;
    public Tile(int id, int x, int y, int size) { this.id = id; this.x = x; this.y = y; this.size = size; }
    public Rectangle getBounds() { return new Rectangle(x, y, size, size); }
}
