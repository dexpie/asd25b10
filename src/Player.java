import java.awt.Color;
import java.util.Stack;

public class Player {
    int id;
    String name;
    Color color;
    int position = 0;
    boolean primePowerMode = false;
    Stack<Integer> moveHistory = new Stack<>();

    public Player(int id, String name, Color color) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.moveHistory.push(0);
    }
}
