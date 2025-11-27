import java.awt.Rectangle;

public class Tile {
    int id, x, y, size;
    int score = 0; // Score on this tile
    boolean isScoreCollected = false;

    public Tile(int id, int x, int y, int size) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }

    public void setScore(int s) {
        this.score = s;
    }

    public int getScore() {
        return score;
    }

    public boolean hasScore() {
        return score > 0 && !isScoreCollected;
    }

    public int collectScore() {
        if (!isScoreCollected) {
            isScoreCollected = true;
            return score;
        }
        return 0;
    }
}
