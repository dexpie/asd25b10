import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardGraph {
    List<Tile> tiles;
    Map<Integer, Integer> connections; // Start -> End
    int rows = 8, cols = 8, tileSize = 70;
    
    public BoardGraph() { 
        tiles = new ArrayList<>(); 
        connections = new HashMap<>();
        buildBoard(); 
        generateRandomConnections();
    }

    private void generateRandomConnections() {
        while (connections.size() < 5) {
            int start = (int)(Math.random() * 62) + 2; // 2..63
            int end = (int)(Math.random() * 62) + 2;   // 2..63
            if (start == end) continue;
            if (connections.containsKey(start)) continue;
            // Avoid immediate loops or chains for simplicity
            if (connections.containsKey(end)) continue; 
            
            connections.put(start, end);
        }
    }

    private void buildBoard() {
        int offsetX = 40, offsetY = 40;
        for (int i = 0; i < rows * cols; i++) {
            int id = i + 1;
            int row = (id - 1) / cols;
            int col = (id - 1) % cols;
            int drawCol = (row % 2 == 0) ? col : (cols - 1 - col);
            int x = offsetX + drawCol * tileSize;
            int y = offsetY + (rows - 1 - row) * tileSize;
            tiles.add(new Tile(id, x, y, tileSize));
        }
    }
    public Tile getTile(int id) {
        if (id < 1) return null;
        if (id > 64) return tiles.get(63);
        return tiles.get(id - 1);
    }
    public List<Tile> getTiles() { return tiles; }
    public Map<Integer, Integer> getConnections() { return connections; }
    public List<Integer> getVisualNeighbors(int id) {
        List<Integer> neighbors = new ArrayList<>();
        if (id < 1 || id > 64) return neighbors;

        int row = (id - 1) / cols;
        int col = (id - 1) % cols;
        int drawCol = (row % 2 == 0) ? col : (cols - 1 - col);

        int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (int[] d : dirs) {
            int nr = row + d[0];
            int nc = drawCol + d[1];

            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                // Convert back to ID
                int originalCol = (nr % 2 == 0) ? nc : (cols - 1 - nc);
                int nid = nr * cols + originalCol + 1;
                if (nid >= 1 && nid <= 64) {
                    neighbors.add(nid);
                }
            }
        }
        return neighbors;
    }
}
