import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

// --- CONFIG & STYLE ---
class GameStyle {
    public static final Color BG_BOARD_1 = new Color(240, 240, 240);
    public static final Color BG_BOARD_2 = new Color(255, 255, 255);
    public static final Color STAR_COLOR = new Color(255, 215, 0); // Emas

    public static final Color[] PLAYER_COLORS = {
            new Color(231, 76, 60),  // Merah
            new Color(52, 152, 219), // Biru
            new Color(241, 196, 15), // Kuning
            new Color(46, 204, 113)  // Hijau
    };

    public static final Color GREEN_MOVE = new Color(39, 174, 96);
    public static final Color RED_MOVE = new Color(192, 57, 43);
    public static final Font FONT_SLOT = new Font("Impact", Font.PLAIN, 32);
    public static final Font FONT_INFO = new Font("Segoe UI", Font.BOLD, 14);
}

// --- DATA MODEL ---

class Player {
    int id;
    String name;
    Color color;
    int position = 0;
    boolean primePowerMode = false;

    public Player(int id, String name, Color color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }
}

class Tile {
    int id, x, y, size;
    public Tile(int id, int x, int y, int size) { this.id = id; this.x = x; this.y = y; this.size = size; }
    public Rectangle getBounds() { return new Rectangle(x, y, size, size); }
}

class BoardGraph {
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

// --- VISUALIZATION PANEL ---

class GamePanel extends JPanel {
    private BoardGraph board;
    private List<Player> allPlayers; // Untuk menggambar semua player
    private LinkedList<Player> turnQueue; // Antrian giliran (Queue)

    private String statusMsg = "Setting up game...";

    // States
    private boolean isDeterminingOrder = false;
    private int highlightedOrderIdx = 0;
    private boolean isRollingDice = false;
    private int displayDiceVal = 1;
    private boolean isRollingChaos = false;
    private int slotScrollY = 0;
    private boolean finalChaosResult = true;
    private boolean showFinalChaos = false;

    public GamePanel(BoardGraph board, List<Player> allPlayers) {
        this.board = board; this.allPlayers = allPlayers;
        setPreferredSize(new Dimension(650, 650));
        setBackground(new Color(230, 230, 230));
    }

    // Setters
    public void setTurnQueue(LinkedList<Player> queue) { this.turnQueue = queue; }
    public void setStatus(String s) { this.statusMsg = s; repaint(); }
    public void setOrderAnimState(boolean active, int highlightIdx) { this.isDeterminingOrder = active; this.highlightedOrderIdx = highlightIdx; repaint(); }
    public void setDiceState(boolean active, int val) { this.isRollingDice = active; this.displayDiceVal = val; repaint(); }
    public void setChaosState(boolean active, int scrollY) { this.isRollingChaos = active; this.slotScrollY = scrollY; this.showFinalChaos = false; repaint(); }
    public void setChaosResult(boolean result) { this.isRollingChaos = true; this.showFinalChaos = true; this.finalChaosResult = result; repaint(); }
    public void hideOverlay() { this.isRollingDice = false; this.isRollingChaos = false; repaint(); }
    public void refresh() { repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Draw Board Tiles
        for (Tile tile : board.getTiles()) {
            int row = (tile.id - 1) / 8;
            int col = (tile.id - 1) % 8;
            boolean isDark = (row % 2 == 0) ? (col % 2 != 0) : (col % 2 == 0);
            g2.setColor(isDark ? GameStyle.BG_BOARD_1 : GameStyle.BG_BOARD_2);
            g2.fill(tile.getBounds());
            g2.setColor(new Color(220,220,220));
            g2.draw(tile.getBounds());

            // --- LOGIC STAR TILE (Kelipatan 5) ---
            if (tile.id % 5 == 0) {
                drawStar(g2, tile.x + tile.size/2, tile.y + tile.size/2, 25);
            }

            g2.setColor(Color.GRAY);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString(String.valueOf(tile.id), tile.x + 5, tile.y + 15);
        }

        // 1.5 Draw Connections (Snakes & Ladders)
        Map<Integer, Integer> conns = board.getConnections();
        for (Map.Entry<Integer, Integer> entry : conns.entrySet()) {
            int start = entry.getKey();
            int end = entry.getValue();
            Tile t1 = board.getTile(start);
            Tile t2 = board.getTile(end);
            if (t1 != null && t2 != null) {
                int x1 = t1.x + t1.size/2;
                int y1 = t1.y + t1.size/2;
                int x2 = t2.x + t2.size/2;
                int y2 = t2.y + t2.size/2;

                g2.setStroke(new BasicStroke(4));
                if (start < end) {
                    g2.setColor(new Color(46, 204, 113, 180)); // Green for Ladder
                } else {
                    g2.setColor(new Color(231, 76, 60, 180)); // Red for Snake
                }
                g2.drawLine(x1, y1, x2, y2);
                
                // Arrow head
                double angle = Math.atan2(y2 - y1, x2 - x1);
                int arrowSize = 10;
                g2.fillPolygon(new int[]{
                    x2, 
                    (int)(x2 - arrowSize * Math.cos(angle - Math.PI/6)), 
                    (int)(x2 - arrowSize * Math.cos(angle + Math.PI/6))
                }, new int[]{
                    y2, 
                    (int)(y2 - arrowSize * Math.sin(angle - Math.PI/6)), 
                    (int)(y2 - arrowSize * Math.sin(angle + Math.PI/6))
                }, 3);
            }
        }

        // 2. Draw Players
        Map<Integer, List<Player>> tileOccupants = new HashMap<>();
        for (Player p : allPlayers) {
            if (!tileOccupants.containsKey(p.position)) tileOccupants.put(p.position, new ArrayList<>());
            tileOccupants.get(p.position).add(p);
        }

        for (Map.Entry<Integer, List<Player>> entry : tileOccupants.entrySet()) {
            int pos = entry.getKey();
            List<Player> occupants = entry.getValue();
            if (pos == 0) {
                for (int i = 0; i < occupants.size(); i++) drawPlayer(g2, 40 + (i * 35), 620, occupants.get(i).color, 30);
            } else {
                Tile t = board.getTile(pos);
                int cx = t.x + t.size / 2; int cy = t.y + t.size / 2;
                if (occupants.size() == 1) {
                    drawPlayer(g2, cx, cy, occupants.get(0).color, 30);
                } else {
                    int[][] offsets = {{-15, -15}, {15, 15}, {15, -15}, {-15, 15}};
                    for (int i = 0; i < occupants.size(); i++) {
                        int ox = (i < 4) ? offsets[i][0] : 0; int oy = (i < 4) ? offsets[i][1] : 0;
                        drawPlayer(g2, cx + ox, cy + oy, occupants.get(i).color, 20);
                    }
                }
            }
        }
        g2.setColor(Color.BLACK); g2.drawString("START", 20, 640);

        // 3. Overlays
        drawOverlays(g2);

        // 4. Status Bar (Ambil nama dari HEAD Queue)
        if (!isDeterminingOrder && turnQueue != null && !turnQueue.isEmpty()) {
            Player p = turnQueue.peek(); // Lihat siapa yg paling depan
            g2.setColor(p.color);
            g2.fillRect(150, 5, 350, 30);
            g2.setColor(Color.BLACK); g2.setStroke(new BasicStroke(1)); g2.drawRect(150, 5, 350, 30);
            g2.setColor(Color.WHITE); g2.setFont(GameStyle.FONT_INFO);
            FontMetrics fm = g2.getFontMetrics();

            String fullStatus = p.name + "'s Turn | " + statusMsg;
            g2.drawString(fullStatus, 325 - fm.stringWidth(fullStatus)/2, 25);
        }
    }

    // Helper Gambar Bintang
    private void drawStar(Graphics2D g2, int cx, int cy, int size) {
        int points = 5;
        double innerRadius = size / 2.5;
        double outerRadius = size / 1.0;
        GeneralPath star = new GeneralPath();

        for (int i = 0; i < points * 2; i++) {
            double angle = (i * Math.PI) / points - Math.PI / 2;
            double r = (i % 2 == 0) ? outerRadius : innerRadius;
            double x = cx + Math.cos(angle) * r * 0.5; // Scale down bit
            double y = cy + Math.sin(angle) * r * 0.5;
            if (i == 0) star.moveTo(x, y);
            else star.lineTo(x, y);
        }
        star.closePath();

        g2.setColor(GameStyle.STAR_COLOR);
        g2.fill(star);
        g2.setColor(Color.ORANGE);
        g2.setStroke(new BasicStroke(1));
        g2.draw(star);
    }

    private void drawOverlays(Graphics2D g2) {
        if (isDeterminingOrder) {
            drawOverlayBg(g2);
            int cx = getWidth()/2, cy = getHeight()/2;
            g2.setColor(Color.WHITE); g2.fillRoundRect(cx - 150, cy - 100, 300, 200, 20, 20);
            g2.setColor(Color.BLACK); g2.setStroke(new BasicStroke(2)); g2.drawRoundRect(cx - 150, cy - 100, 300, 200, 20, 20);
            g2.setFont(GameStyle.FONT_INFO); g2.drawString("WHO GOES FIRST?", cx - 60, cy - 70);
            int startY = cy - 30;
            for (int i = 0; i < allPlayers.size(); i++) {
                if (i == highlightedOrderIdx) {
                    g2.setColor(new Color(52, 152, 219, 50)); g2.fillRect(cx - 130, startY + (i*30) - 20, 260, 25);
                    g2.setColor(new Color(52, 152, 219)); g2.setStroke(new BasicStroke(3)); g2.drawRect(cx - 130, startY + (i*30) - 20, 260, 25);
                }
                g2.setColor(allPlayers.get(i).color); g2.fillOval(cx - 120, startY + (i*30) - 15, 15, 15);
                g2.setColor(Color.BLACK); g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                g2.drawString(allPlayers.get(i).name, cx - 90, startY + (i*30));
            }
        } else if ((isRollingDice || isRollingChaos) && turnQueue != null && !turnQueue.isEmpty()) {
            drawOverlayBg(g2);
            int cx = getWidth()/2, cy = getHeight()/2;
            g2.setColor(Color.WHITE); g2.fillRoundRect(cx - 110, cy - 110, 220, 220, 30, 30);
            g2.setStroke(new BasicStroke(5));
            g2.setColor(turnQueue.peek().color); g2.drawRoundRect(cx - 110, cy - 110, 220, 220, 30, 30);

            if (isRollingDice) {
                drawDiceFace(g2, cx - 60, cy - 70, 120, displayDiceVal);
                g2.setColor(Color.BLACK); g2.setFont(GameStyle.FONT_INFO);
                g2.drawString(turnQueue.peek().name + " Rolling...", cx - 45, cy + 80);
            } else if (isRollingChaos) {
                Shape clip = g2.getClip();
                g2.setClip(cx - 100, cy - 80, 200, 160);
                if (showFinalChaos) {
                    drawSlotItem(g2, cx, cy, finalChaosResult ? "FORWARD" : "BACKWARD", finalChaosResult ? GameStyle.GREEN_MOVE : GameStyle.RED_MOVE);
                } else {
                    int offset = slotScrollY % 160;
                    for(int i=-1; i<3; i++) {
                        boolean fwd = (i%2==0);
                        drawSlotItem(g2, cx, cy+(i*80)+offset, fwd?"BACKWARD":"FORWARD", fwd?GameStyle.RED_MOVE:GameStyle.GREEN_MOVE);
                    }
                }
                g2.setClip(clip);
                g2.setColor(Color.GRAY); g2.setStroke(new BasicStroke(3)); g2.drawRect(cx - 100, cy - 80, 200, 160);
                g2.setColor(Color.ORANGE);
                g2.fillPolygon(new int[]{cx-105, cx-120, cx-120}, new int[]{cy, cy-10, cy+10}, 3);
                g2.fillPolygon(new int[]{cx+105, cx+120, cx+120}, new int[]{cy, cy-10, cy+10}, 3);
            }
        }
    }

    private void drawOverlayBg(Graphics2D g2) { g2.setColor(new Color(0,0,0,150)); g2.fillRect(0, 0, getWidth(), getHeight()); }
    private void drawPlayer(Graphics2D g2, int cx, int cy, Color c, int diameter) {
        int r = diameter / 2;
        g2.setColor(new Color(0,0,0,50)); g2.fillOval(cx - r + 3, cy - r + 3, diameter, diameter);
        g2.setColor(c); g2.fillOval(cx - r, cy - r, diameter, diameter);
        g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(2)); g2.drawOval(cx - r, cy - r, diameter, diameter);
    }
    private void drawDiceFace(Graphics2D g2, int x, int y, int size, int val) {
        g2.setColor(new Color(245, 245, 245)); g2.fillRoundRect(x, y, size, size, 20, 20);
        g2.setColor(Color.BLACK); g2.setStroke(new BasicStroke(2)); g2.drawRoundRect(x, y, size, size, 20, 20);
        int dot = size/5, c = size/2, l = size/4, r = size*3/4;
        if(val%2!=0) fillDot(g2,x+c,y+c,dot);
        if(val>1){fillDot(g2,x+l,y+l,dot); fillDot(g2,x+r,y+r,dot);}
        if(val>3){fillDot(g2,x+r,y+l,dot); fillDot(g2,x+l,y+r,dot);}
        if(val==6){fillDot(g2,x+l,y+c,dot); fillDot(g2,x+r,y+c,dot);}
    }
    private void fillDot(Graphics2D g2, int x, int y, int s){g2.fillOval(x-s/2,y-s/2,s,s);}
    private void drawSlotItem(Graphics2D g2, int x, int y, String t, Color c){
        g2.setColor(c); g2.fillRect(x-90, y-35, 180, 70);
        g2.setColor(Color.WHITE); g2.setFont(GameStyle.FONT_SLOT);
        FontMetrics fm=g2.getFontMetrics(); g2.drawString(t, x-fm.stringWidth(t)/2, y+10);
    }
}

// --- LOGIC CONTROLLER ---

class GameController {
    private BoardGraph board;
    private GamePanel panel;
    private List<Player> allPlayers;
    private LinkedList<Player> turnQueue; // Menggunakan Queue sesuai permintaan

    private boolean isGameStarted = false;
    private boolean isAnimating = false;
    private int finalDiceValue;
    private boolean moveForward;

    public GameController(BoardGraph board, GamePanel panel, List<Player> players) {
        this.board = board; this.panel = panel; this.allPlayers = players;
        this.turnQueue = new LinkedList<>();
    }

    public void startOrderRoulette() {
        panel.setStatus("Determining who goes first...");
        Timer rouletteTimer = new Timer(100, null);
        final long startTime = System.currentTimeMillis();
        final int[] currentIndex = {0};

        rouletteTimer.addActionListener(e -> {
            if (System.currentTimeMillis() - startTime < 3000) {
                panel.setOrderAnimState(true, currentIndex[0]);
                currentIndex[0] = (currentIndex[0] + 1) % allPlayers.size();
            } else {
                ((Timer)e.getSource()).stop();
                int winnerIdx = (int)(Math.random() * allPlayers.size());
                panel.setOrderAnimState(true, winnerIdx);
                Timer pause = new Timer(1500, evt -> setupQueue(winnerIdx));
                pause.setRepeats(false); pause.start();
            }
        });
        rouletteTimer.start();
    }

    private void setupQueue(int winnerIdx) {
        Collections.rotate(allPlayers, -winnerIdx);
        turnQueue.clear();
        turnQueue.addAll(allPlayers);
        panel.setTurnQueue(turnQueue);

        Player first = turnQueue.peek();
        panel.setOrderAnimState(false, 0);
        panel.setStatus("Ready! " + first.name + " goes first.");
        isGameStarted = true;
        panel.refresh();
        JOptionPane.showMessageDialog(panel, first.name + " starts first!");
    }

    public void startTurn() {
        if (!isGameStarted || isAnimating) return;
        Player currentP = turnQueue.peek();
        if (currentP.position >= 64) return;
        isAnimating = true;
        startDiceAnimation();
    }

    private void startDiceAnimation() {
        finalDiceValue = (int)(Math.random() * 6) + 1;
        Timer diceTimer = new Timer(80, null);
        final long startTime = System.currentTimeMillis();
        diceTimer.addActionListener(e -> {
            if (System.currentTimeMillis() - startTime < 1000) {
                panel.setDiceState(true, (int)(Math.random() * 6) + 1);
            } else {
                ((Timer)e.getSource()).stop();
                panel.setDiceState(true, finalDiceValue);
                Timer pause = new Timer(600, evt -> {
                    panel.setDiceState(false, 1); startChaosSlotMachine();
                });
                pause.setRepeats(false); pause.start();
            }
        });
        diceTimer.start();
    }

    private void startChaosSlotMachine() {
        Player currentP = turnQueue.peek();
        double prob = Math.random();
        moveForward = (prob <= 0.80);
        if (currentP.position == 0) moveForward = true;

        Timer slotTimer = new Timer(20, null);
        final long startTime = System.currentTimeMillis();
        final int[] scrollY = {0};
        slotTimer.addActionListener(e -> {
            if (System.currentTimeMillis() - startTime < 1500) {
                scrollY[0] += 25; panel.setChaosState(true, scrollY[0]);
            } else {
                ((Timer)e.getSource()).stop();
                panel.setChaosResult(moveForward);
                Timer pause = new Timer(1000, evt -> {
                    panel.hideOverlay(); movePlayer();
                });
                pause.setRepeats(false); pause.start();
            }
        });
        slotTimer.start();
    }

    private void movePlayer() {
        Player currentP = turnQueue.peek();
        List<Integer> steps = new ArrayList<>();
        int simPos = currentP.position;

        if (currentP.primePowerMode) {
            List<Integer> path = getShortestPathToWin(simPos);
            int moves = Math.min(finalDiceValue, path.size());
            for (int i = 0; i < moves; i++) {
                steps.add(path.get(i));
            }
            currentP.primePowerMode = false;
            panel.setStatus("Shortest Path Move!");
        } else {
            boolean headingUp = moveForward;
            for (int i = 0; i < finalDiceValue; i++) {
                if (simPos == 64) headingUp = false;
                if (headingUp) { if (simPos < 64) simPos++; }
                else { if (simPos > 1) simPos--; }
                steps.add(simPos);
            }
        }

        panel.setStatus("Walking...");
        final int[] stepIdx = {0};
        Timer moveTimer = new Timer(300, e -> {
            if (stepIdx[0] < steps.size()) {
                currentP.position = steps.get(stepIdx[0]);
                panel.refresh();
                stepIdx[0]++;
            } else {
                ((Timer)e.getSource()).stop();
                finishTurn(currentP);
            }
        });
        moveTimer.start();
    }

    private void finishTurn(Player p) {
        isAnimating = false;

        // Check Connections (Snakes/Ladders)
        Map<Integer, Integer> conns = board.getConnections();
        if (conns.containsKey(p.position)) {
            int dest = conns.get(p.position);
            p.position = dest;
            panel.refresh();
            JOptionPane.showMessageDialog(panel, (dest > p.position ? "LADDER! Up to " : "SNAKE! Down to ") + dest);
        }

        if (p.position == 64) {
            JOptionPane.showMessageDialog(panel, p.name + " WINS THE GAME!");
            System.exit(0);
        }

        // Check Prime for Next Turn
        if (isPrime(p.position)) {
            p.primePowerMode = true;
            JOptionPane.showMessageDialog(panel, "PRIME NUMBER (" + p.position + ")! \nShortest Path Activated for Next Turn!");
        }

        // LOGIC GANTI GILIRAN (QUEUE)
        boolean isStarTile = (p.position > 0 && p.position % 5 == 0);

        if (isStarTile) {
            // EXTRA TURN: Hapus depan, masukkan depan lagi
            Player samePlayer = turnQueue.poll();
            turnQueue.addFirst(samePlayer);
            JOptionPane.showMessageDialog(panel, "⭐ STAR TILE! ⭐\n" + p.name + " gets an EXTRA TURN!");
            panel.setStatus("Extra Turn for " + p.name + "!");
        } else {
            // NORMAL TURN: Hapus depan, masukkan belakang
            Player donePlayer = turnQueue.poll();
            turnQueue.offer(donePlayer);
            Player nextPlayer = turnQueue.peek();
            panel.setStatus("Click Roll for " + nextPlayer.name);
        }
        panel.refresh();
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    private List<Integer> getShortestPathToWin(int startNode) {
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> parent = new HashMap<>();
        
        for (int i = 1; i <= 64; i++) dist.put(i, Integer.MAX_VALUE);
        
        dist.put(startNode, 0);
        pq.add(new int[]{startNode, 0});
        
        Map<Integer, Integer> conns = board.getConnections();

        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int u = cur[0];
            int d = cur[1];
            
            if (d > dist.get(u)) continue;
            if (u == 64) break;
            
            List<Integer> neighbors = board.getVisualNeighbors(u);
            
            for (int v : neighbors) {
                int target = v;
                // If neighbor is a connection start, we effectively land on the end
                if (conns.containsKey(v)) {
                    target = conns.get(v);
                }
                
                int newDist = d + 1;
                if (newDist < dist.get(target)) {
                    dist.put(target, newDist);
                    parent.put(target, u);
                    pq.add(new int[]{target, newDist});
                }
            }
        }
        
        List<Integer> path = new ArrayList<>();
        Integer curr = 64;
        if (dist.get(64) == Integer.MAX_VALUE) return path; // No path found

        while (curr != null && curr != startNode) {
            path.add(0, curr);
            curr = parent.get(curr);
        }
        return path;
    }
}

// --- MAIN ---
public class SnakeLadderGame extends JFrame {
    public SnakeLadderGame() {
        setTitle("Snake Ladder: Star Queue Edition");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Ubah ke Dispose agar tidak close semua
        setLayout(new BorderLayout());

        String input = JOptionPane.showInputDialog(this, "How many players? (2-4)", "2");
        if(input == null) return;

        int numPlayers = 2;
        try { numPlayers = Math.max(2, Math.min(4, Integer.parseInt(input))); } catch (Exception e) {}

        JPanel namePanel = new JPanel(new GridLayout(numPlayers, 2, 10, 10));
        JTextField[] nameFields = new JTextField[numPlayers];
        for(int i=0; i<numPlayers; i++) {
            namePanel.add(new JLabel("Player " + (i+1) + " Name:"));
            nameFields[i] = new JTextField("Player " + (i+1));
            namePanel.add(nameFields[i]);
        }

        int result = JOptionPane.showConfirmDialog(null, namePanel, "Enter Names", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        List<Player> players = new ArrayList<>();
        for(int i=0; i<numPlayers; i++) {
            String name = nameFields[i].getText().trim();
            if(name.isEmpty()) name = "Player " + (i+1);
            players.add(new Player(i, name, GameStyle.PLAYER_COLORS[i]));
        }

        BoardGraph board = new BoardGraph();
        GamePanel panel = new GamePanel(board, players);
        GameController controller = new GameController(board, panel, players);

        add(panel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        btnPanel.setPreferredSize(new Dimension(600, 80));
        btnPanel.setBackground(Color.WHITE);
        JButton rollBtn = new JButton("ROLL & GACHA");
        rollBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        rollBtn.setBackground(new Color(52, 73, 94));
        rollBtn.setForeground(Color.WHITE);
        rollBtn.setPreferredSize(new Dimension(200, 50));
        rollBtn.setFocusPainted(false);
        rollBtn.addActionListener(e -> controller.startTurn());

        btnPanel.add(rollBtn);
        add(btnPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        SwingUtilities.invokeLater(controller::startOrderRoulette);
    }

    // Main method opsional jika ingin run file ini saja
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new SnakeLadderGame().setVisible(true));
    }
}