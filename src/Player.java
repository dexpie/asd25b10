import java.awt.Color;
import java.util.Stack;

public class Player {
    int id;
    String name;
    Color color;
    int position = 0;
    boolean primePowerMode = false;
    Stack<Integer> moveHistory = new Stack<>();
    int score = 0;

    public Player(int id, String name, Color color) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.moveHistory.push(0);
    }

    public void addScore(int s) { this.score += s; }
    public int getScore() { return score; }
}
