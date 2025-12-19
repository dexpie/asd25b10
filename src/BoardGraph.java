import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BoardGraph {
    List<Tile> tiles;
    Map<Integer, Integer> connections; // Start -> End
    
    private Map<Integer, Integer> tileScores = new HashMap<>();
    private Set<Integer> collectedScores = new HashSet<>();
    private Set<Integer> cardTiles = new HashSet<>();

    int rows = 8, cols = 8, tileSize = 70;
    
    // Manual coordinates captured from MapMaker (64 points)
    
    // Points captured: 64
    private final int[][] islandCoords = {
        { 216, 548 }, // 1
        { 280, 518 }, // 2
        { 310, 578 }, // 3
        { 352, 529 }, // 4
        { 387, 578 }, // 5
        { 454, 609 }, // 6
        { 536, 585 }, // 7
        { 615, 543 }, // 8
        { 692, 504 }, // 9
        { 633, 456 }, // 10
        { 554, 481 }, // 11
        { 485, 521 }, // 12
        { 414, 477 }, // 13
        { 335, 453 }, // 14
        { 267, 437 }, // 15
        { 183, 422 }, // 16
        { 151, 356 }, // 17
        { 241, 309 }, // 18
        { 226, 224 }, // 19
        { 261, 150 }, // 20
        { 337, 197 }, // 21
        { 331, 271 }, // 22
        { 312, 349 }, // 23
        { 386, 390 }, // 24
        { 462, 427 }, // 25
        { 551, 407 }, // 26
        { 647, 389 }, // 27
        { 721, 434 }, // 28
        { 797, 389 }, // 29
        { 827, 307 }, // 30
        { 818, 238 }, // 31
        { 819, 153 }, // 32
        { 761, 195 }, // 33
        { 751, 255 }, // 34
        { 738, 329 }, // 35
        { 664, 328 }, // 36
        { 683, 250 }, // 37
        { 691, 155 }, // 38
        { 655, 59 }, // 39
        { 630, 129 }, // 40
        { 612, 217 }, // 41
        { 587, 313 }, // 42
        { 487, 346 }, // 43
        { 411, 321 }, // 44
        { 399, 214 }, // 45
        { 395, 128 }, // 46
        { 319, 101 }, // 47
        { 406, 57 }, // 48
        { 483, 77 }, // 49
        { 561, 101 }, // 50
        { 635, 169 }, // 51
        { 570, 149 }, // 52
        { 553, 213 }, // 53
        { 582, 269 }, // 54
        { 546, 333 }, // 55
        { 515, 269 }, // 56
        { 450, 278 }, // 57
        { 413, 261 }, // 58
        { 470, 209 }, // 59
        { 434, 153 }, // 60
        { 466, 102 }, // 61
        { 511, 122 }, // 62
        { 513, 181 }, // 63
        { 517, 223 }, // 64
    };

    // Optional normalized coordinates (0..1) computed from a reference image size.
    
    private double[][] islandNorm = null;
    private int mapRefWidth = -1, mapRefHeight = -1;

    /**
     * Compute normalized coordinates (0..1) from the integer `islandCoords`
     * using the reference image/panel size that was used when creating the
     * points (for example, the MapMaker panel size).
     */
    public void computeNormalizedFromReference(int refWidth, int refHeight) {
        if (islandCoords == null || islandCoords.length == 0) return;
        if (refWidth <= 0 || refHeight <= 0) return;
        mapRefWidth = refWidth;
        mapRefHeight = refHeight;
        islandNorm = new double[islandCoords.length][2];
        for (int i = 0; i < islandCoords.length; i++) {
            islandNorm[i][0] = islandCoords[i][0] / (double) refWidth;
            islandNorm[i][1] = islandCoords[i][1] / (double) refHeight;
        }
    }

    /**
     * Rebuilds the `tiles` list using normalized coordinates scaled to the
     * provided map display size. If normalized coordinates are not available
     * this method will fall back to using the raw `islandCoords` values.
     */
    public void updateTilesForMapSize(int mapWidth, int mapHeight, int tilePx) {
        tiles.clear();
        if (islandNorm != null && islandNorm.length >= 64) {
            for (int i = 0; i < 64; i++) {
                int id = i + 1;
                int cx = (int) Math.round(islandNorm[i][0] * mapWidth);
                int cy = (int) Math.round(islandNorm[i][1] * mapHeight);
                int x = cx - tilePx / 2;
                int y = cy - tilePx / 2;
                Tile t = new Tile(id, x, y, tilePx);
        
                if (tileScores.containsKey(id)) {
                    t.setScore(tileScores.get(id));
                    if (collectedScores.contains(id)) {
                        t.collectScore(); // Mark as collected
                    }
                }
                tiles.add(t);
            }
        } else if (islandCoords != null && islandCoords.length >= 64) {
            // If no normalized coords present, scale raw coords proportionally
            // if a reference size exists; otherwise use raw pixel coords directly.
            if (mapRefWidth > 0 && mapRefHeight > 0) {
                double sx = mapWidth / (double) mapRefWidth;
                double sy = mapHeight / (double) mapRefHeight;
                for (int i = 0; i < 64; i++) {
                    int id = i + 1;
                    int cx = (int) Math.round(islandCoords[i][0] * sx);
                    int cy = (int) Math.round(islandCoords[i][1] * sy);
                    int x = cx - tilePx / 2;
                    int y = cy - tilePx / 2;
                    Tile t = new Tile(id, x, y, tilePx);
                    if (tileScores.containsKey(id)) {
                        t.setScore(tileScores.get(id));
                        if (collectedScores.contains(id)) {
                            t.collectScore();
                        }
                    }
                    tiles.add(t);
                }
            } else {
                // No reference: place tiles using raw captured coords as-is.
                for (int i = 0; i < 64; i++) {
                    int id = i + 1;
                    int cx = islandCoords[i][0];
                    int cy = islandCoords[i][1];
                    int x = cx - tilePx / 2;
                    int y = cy - tilePx / 2;
                    Tile t = new Tile(id, x, y, tilePx);
                    if (tileScores.containsKey(id)) {
                        t.setScore(tileScores.get(id));
                        if (collectedScores.contains(id)) {
                            t.collectScore();
                        }
                    }
                    tiles.add(t);
                }
            }
        } else {
            // Fallback to grid builder
            buildBoard();
        }
    }

    public BoardGraph() {
        tiles = new ArrayList<>();
        connections = new HashMap<>();
        // Default reference size (can be updated later)
        this.mapRefWidth = 1000;
        this.mapRefHeight = 760;
        
        buildBoardFromCoords();
        generateRandomConnections();
        generateRandomScores();
    }

    private void generateRandomConnections() {
        while (connections.size() < 5) {
            int start = (int)(Math.random() * 60) + 2; // 2..61
            int end = (int)(Math.random() * (63 - start)) + start + 1; // start+1 .. 63
            
            // Ensure Start < End (LADDER ONLY)
            if (start >= end) continue; 

            if (connections.containsKey(start)) continue;
            if (connections.containsKey(end)) continue; 
            
            connections.put(start, end);
        }
    }

    private void buildBoard() {
        // kept for compatibility but not used when islandCoords present
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

    private void buildBoardFromCoords() {
        // Create tiles from islandCoords (center-based coords). If not enough points, fallback to grid builder.
        int size = 48;
        if (islandCoords != null && islandCoords.length >= 64) {
            for (int i = 0; i < 64; i++) {
                int id = i + 1;
                int cx = islandCoords[i][0];
                int cy = islandCoords[i][1];
                int x = cx - size / 2;
                int y = cy - size / 2;
                tiles.add(new Tile(id, x, y, size));
            }
        } else {
            buildBoard();
        }
    }
    public Tile getTile(int id) {
        if (id < 1) return null;
        if (id > tiles.size()) return tiles.get(tiles.size() - 1);
        return tiles.get(id - 1);
    }
    public List<Tile> getTiles() { return tiles; }
    public Map<Integer, Integer> getConnections() { return connections; }
    /**
     * For the visual (map) layout we consider neighbors to be previous/next along the path.
     */
    public List<Integer> getVisualNeighbors(int id) {
        List<Integer> neighbors = new ArrayList<>();
        if (id < 1 || id > tiles.size()) return neighbors;
        if (id > 1) neighbors.add(id - 1);
        if (id < tiles.size()) neighbors.add(id + 1);
        return neighbors;
    }

    private void generateRandomScores() {
        tileScores.clear();
        collectedScores.clear();
        // Assign random scores to 10 random tiles
    
        for (int i = 0; i < 10; i++) {
            int id = (int)(Math.random() * 62) + 2; // 2..63
            if (!connections.containsKey(id)) { // Don't put score on ladder start
                int score = ((int)(Math.random() * 5) + 1) * 10; // 10, 20, 30, 40, 50
                tileScores.put(id, score);
                
                // Also update current tiles if they exist
                Tile t = getTile(id);
                if (t != null) t.setScore(score);
            }
        }
    }

    public void resetConnections() {
        connections.clear();
        generateRandomConnections();
        resetScores();
    }
    public void resetScores() {
        tileScores.clear();
        collectedScores.clear();
        for(Tile t : tiles) {
            t.setScore(0);
            
        }
        generateRandomScores();
    }
}
