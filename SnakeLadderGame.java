import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.Queue;
import javax.swing.Timer;


class GameLogic {

    public static void shuffleSecretPaths(BoardGraph board) {
        Random rand = new Random();

        // Reset Graph
        board.secretPaths.clear();
        board.adjacencyMap.clear();

        // Restore Normal Paths
        for (int i = 1; i < 64; i++) board.addEdge(i, i + 1);

        // Generate Secret Paths
        for (Tile t : board.tiles) {
            if (isPrime(t.id) && t.id < 60) {
                t.isPrime = true;
                // 30% Chance
                if (rand.nextDouble() < 0.3) {
                    int target;
                    do {
                        target = rand.nextInt(63) + 1;
                    } while (Math.abs(target - t.id) < 5 || target == t.id);

                    board.addEdge(t.id, target);
                    board.secretPaths.put(t.id, target);
                    System.out.println("Path: " + t.id + " -> " + target);
                }
            }
        }
    }

    public static List<Integer> findBestPath(BoardGraph board, int startNode, int steps, boolean forward) {
        // Jika masih di luar papan (0), langkah pertama dipakai untuk masuk ke Tile 1.
        if (startNode == 0) {
            startNode = 1;
            steps--; // Kurangi jatah langkah karena sudah dipakai untuk masuk ke 1
        }

        // Jika dadu 1 (steps jadi 0), maka dia cuma sampai di Tile 1
        if (steps < 0) return new ArrayList<>(Collections.singletonList(startNode));

        // --- BFS / DIJKSTRA ---
        Queue<PathNode> queue = new LinkedList<>();
        queue.add(new PathNode(startNode, new ArrayList<>(Collections.singletonList(startNode))));

        List<List<Integer>> candidates = new ArrayList<>();

        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            int currentDepth = current.path.size() - 1;

            // Target steps reached
            if (currentDepth == steps) {
                candidates.add(current.path);
                continue;
            }

            if (currentDepth > steps) continue;

            if (board.adjacencyMap.containsKey(current.id)) {
                for (int neighbor : board.adjacencyMap.get(current.id)) {

                    boolean isSecret = board.secretPaths.containsKey(current.id) && board.secretPaths.get(current.id) == neighbor;
                    boolean isNormal = (neighbor == current.id + 1);

                    // PASS-THROUGH RULE:
                    // Secret path hanya bisa dipakai jika kita BERHENTI/START di situ.
                    if (isSecret && current.id != startNode) {
                        continue;
                    }

                    if (isSecret || (isNormal && forward)) {
                        List<Integer> newPath = new ArrayList<>(current.path);
                        newPath.add(neighbor);
                        queue.add(new PathNode(neighbor, newPath));
                    }
                }
            }

            // Manual backward
            if (!forward && current.id > 1) {
                List<Integer> newPath = new ArrayList<>(current.path);
                newPath.add(current.id - 1);
                queue.add(new PathNode(current.id - 1, newPath));
            }
        }

        if (candidates.isEmpty()) return null;

        // Pick best path (Highest End Tile)
        candidates.sort((p1, p2) -> Integer.compare(p2.get(p2.size()-1), p1.get(p1.size()-1)));
        return candidates.get(0);
    }

    private static boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) if (n % i == 0) return false;
        return true;
    }

    static class PathNode {
        int id; List<Integer> path;
        public PathNode(int id, List<Integer> p) { this.id = id; this.path = p; }
    }
}

// --- ENGINE & VISUAL ---

class GameStyle {
    public static final Color BG_1 = new Color(240, 240, 240);
    public static final Color BG_2 = new Color(255, 255, 255);
    public static final Color PRIME = new Color(230, 230, 250);
    public static final Color STAR = new Color(255, 215, 0);
    public static final Color SECRET_PATH = new Color(138, 43, 226, 180);
    public static final Color[] P_COLORS = {
            new Color(231, 76, 60), new Color(52, 152, 219),
            new Color(241, 196, 15), new Color(46, 204, 113)
    };
    public static final Color GREEN = new Color(39, 174, 96);
    public static final Color RED = new Color(192, 57, 43);
    public static final Font FONT_INFO = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SLOT = new Font("Impact", Font.PLAIN, 32);
}

class Player {
    int id; String name; Color color; int position = 0;
    public Player(int id, String name, Color color) { this.id = id; this.name = name; this.color = color; }
}

class Tile {
    int id, x, y, size; boolean isPrime = false;
    public Tile(int id, int x, int y, int size) { this.id = id; this.x = x; this.y = y; this.size = size; }
    public Rectangle getBounds() { return new Rectangle(x, y, size, size); }
    public Point getCenter() { return new Point(x + size/2, y + size/2); }
}

class BoardGraph {
    List<Tile> tiles;
    Map<Integer, List<Integer>> adjacencyMap;
    Map<Integer, Integer> secretPaths;
    int rows = 8, cols = 8, tileSize = 70;

    public BoardGraph() {
        tiles = new ArrayList<>();
        adjacencyMap = new HashMap<>();
        secretPaths = new HashMap<>();
        buildBoard();
        // Init Normal Graph
        for (int i = 1; i < 64; i++) addEdge(i, i + 1);
    }

    private void buildBoard() {
        int offX = 40, offY = 40;
        for (int i = 0; i < rows * cols; i++) {
            int id = i + 1;
            int row = (id - 1) / cols;
            int col = (id - 1) % cols;
            int drawCol = (row % 2 == 0) ? col : (cols - 1 - col);
            int x = offX + drawCol * tileSize;
            int y = offY + (rows - 1 - row) * tileSize;

            Tile t = new Tile(id, x, y, tileSize);
            if(isPrime(id)) t.isPrime = true;
            tiles.add(t);
        }
    }

    public void addEdge(int s, int d) { adjacencyMap.computeIfAbsent(s, k -> new ArrayList<>()).add(d); }
    public Tile getTile(int id) { if (id < 1) return null; if (id > 64) return tiles.get(63); return tiles.get(id - 1); }
    public List<Tile> getTiles() { return tiles; }
    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) if (n % i == 0) return false;
        return true;
    }
}

class GamePanel extends JPanel {
    private BoardGraph board;
    private List<Player> allPlayers;
    private LinkedList<Player> turnQueue;
    private String statusMsg = "Setting up...";

    private boolean isOrdering = false;
    private int hlOrderIdx = 0;
    private boolean isDice = false;
    private int diceVal = 1;
    private boolean isChaos = false;
    private int slotY = 0;
    private boolean chaosRes = true;
    private boolean showChaos = false;

    public GamePanel(BoardGraph board, List<Player> players) {
        this.board = board; this.allPlayers = players;
        setPreferredSize(new Dimension(650, 650));
        setBackground(new Color(230, 230, 230));
    }

    public void setTurnQueue(LinkedList<Player> q) { this.turnQueue = q; }
    public void setStatus(String s) { this.statusMsg = s; repaint(); }
    public void setOrderAnim(boolean on, int idx) { this.isOrdering = on; this.hlOrderIdx = idx; repaint(); }
    public void setDiceAnim(boolean on, int val) { this.isDice = on; this.diceVal = val; repaint(); }
    public void setChaosAnim(boolean on, int y) { this.isChaos = on; this.slotY = y; this.showChaos = false; repaint(); }
    public void setChaosRes(boolean res) { this.isChaos = true; this.showChaos = true; this.chaosRes = res; repaint(); }
    public void hideOverlay() { this.isDice = false; this.isChaos = false; repaint(); }
    public void refresh() { repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Tiles
        for (Tile t : board.getTiles()) {
            boolean isDark = ((t.id - 1) / 8 % 2 == 0) ? ((t.id - 1) % 8 % 2 != 0) : ((t.id - 1) % 8 % 2 == 0);
            g2.setColor(t.isPrime ? GameStyle.PRIME : (isDark ? GameStyle.BG_1 : GameStyle.BG_2));
            g2.fill(t.getBounds());
            g2.setColor(new Color(200,200,200)); g2.draw(t.getBounds());

            if (t.id % 5 == 0) drawStar(g2, t.x + t.size/2, t.y + t.size/2, 25);

            g2.setColor(Color.GRAY);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString(String.valueOf(t.id), t.x + 5, t.y + 15);
            if(t.isPrime) {
                g2.setFont(new Font("Arial", Font.ITALIC, 10));
                g2.setColor(new Color(100,100,150));
                g2.drawString("P", t.x + 55, t.y + 15);
            }
        }

        // 1.5 Secret Paths
        Stroke def = g2.getStroke();
        g2.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
        g2.setColor(GameStyle.SECRET_PATH);
        for(Map.Entry<Integer, Integer> path : board.secretPaths.entrySet()) {
            Point p1 = board.getTile(path.getKey()).getCenter();
            Point p2 = board.getTile(path.getValue()).getCenter();
            QuadCurve2D q = new QuadCurve2D.Float();
            q.setCurve(p1.x, p1.y, (p1.x+p2.x)/2, Math.min(p1.y,p2.y)-50, p2.x, p2.y);
            g2.draw(q);
            g2.fillOval(p2.x-5, p2.y-5, 10, 10);
        }
        g2.setStroke(def);

        // 2. Players
        Map<Integer, List<Player>> map = new HashMap<>();
        for (Player p : allPlayers) map.computeIfAbsent(p.position, k->new ArrayList<>()).add(p);

        for (Map.Entry<Integer, List<Player>> entry : map.entrySet()) {
            int pos = entry.getKey(); List<Player> list = entry.getValue();
            if (pos == 0) {
                for (int i=0; i<list.size(); i++) drawP(g2, 40+(i*35), 620, list.get(i).color, 30);
            } else {
                Tile t = board.getTile(pos);
                int cx = t.x + t.size/2, cy = t.y + t.size/2;
                if (list.size() == 1) drawP(g2, cx, cy, list.get(0).color, 30);
                else {
                    int[][] offs = {{-15,-15},{15,15},{15,-15},{-15,15}};
                    for (int i=0; i<list.size(); i++) {
                        int ox = (i<4)?offs[i][0]:0, oy = (i<4)?offs[i][1]:0;
                        drawP(g2, cx+ox, cy+oy, list.get(i).color, 20);
                    }
                }
            }
        }
        g2.setColor(Color.BLACK); g2.drawString("START", 20, 640);

        drawOverlays(g2);

        if (!isOrdering && turnQueue != null && !turnQueue.isEmpty()) {
            Player p = turnQueue.peek();
            g2.setColor(p.color); g2.fillRect(150, 5, 350, 30);
            g2.setColor(Color.BLACK); g2.setStroke(new BasicStroke(1)); g2.drawRect(150, 5, 350, 30);
            g2.setColor(Color.WHITE); g2.setFont(GameStyle.FONT_INFO);
            FontMetrics fm = g2.getFontMetrics();
            String txt = p.name + "'s Turn | " + statusMsg;
            g2.drawString(txt, 325 - fm.stringWidth(txt)/2, 25);
        }
    }

    private void drawOverlays(Graphics2D g2) {
        int cx = getWidth()/2, cy = getHeight()/2;
        if (isOrdering) {
            drawBg(g2);
            g2.setColor(Color.WHITE); g2.fillRoundRect(cx-150, cy-100, 300, 200, 20, 20);
            g2.setColor(Color.BLACK); g2.setStroke(new BasicStroke(2)); g2.drawRoundRect(cx-150, cy-100, 300, 200, 20, 20);
            g2.setFont(GameStyle.FONT_INFO); g2.drawString("WHO GOES FIRST?", cx-60, cy-70);
            int y = cy - 30;
            for (int i=0; i<allPlayers.size(); i++) {
                if (i == hlOrderIdx) {
                    g2.setColor(new Color(52, 152, 219, 50)); g2.fillRect(cx-130, y+(i*30)-20, 260, 25);
                    g2.setColor(new Color(52, 152, 219)); g2.setStroke(new BasicStroke(3)); g2.drawRect(cx-130, y+(i*30)-20, 260, 25);
                }
                g2.setColor(allPlayers.get(i).color); g2.fillOval(cx-120, y+(i*30)-15, 15, 15);
                g2.setColor(Color.BLACK); g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                g2.drawString(allPlayers.get(i).name, cx-90, y+(i*30));
            }
        } else if ((isDice || isChaos) && turnQueue != null && !turnQueue.isEmpty()) {
            drawBg(g2);
            g2.setColor(Color.WHITE); g2.fillRoundRect(cx-110, cy-110, 220, 220, 30, 30);
            g2.setStroke(new BasicStroke(5));
            g2.setColor(turnQueue.peek().color); g2.drawRoundRect(cx-110, cy-110, 220, 220, 30, 30);

            if (isDice) {
                drawDice(g2, cx-60, cy-70, 120, diceVal);
                g2.setColor(Color.BLACK); g2.setFont(GameStyle.FONT_INFO);
                g2.drawString(turnQueue.peek().name + " Rolling...", cx-45, cy+80);
            } else if (isChaos) {
                Shape clip = g2.getClip();
                g2.setClip(cx-100, cy-80, 200, 160);
                if (showChaos) drawSlot(g2, cx, cy, chaosRes ? "FORWARD" : "BACKWARD", chaosRes ? GameStyle.GREEN : GameStyle.RED);
                else {
                    int off = slotY % 160;
                    for(int i=-1; i<3; i++) {
                        boolean fwd = (i%2==0);
                        drawSlot(g2, cx, cy+(i*80)+off, fwd?"BACKWARD":"FORWARD", fwd?GameStyle.RED:GameStyle.GREEN);
                    }
                }
                g2.setClip(clip);
                g2.setColor(Color.GRAY); g2.setStroke(new BasicStroke(3)); g2.drawRect(cx-100, cy-80, 200, 160);
                g2.setColor(Color.ORANGE);
                g2.fillPolygon(new int[]{cx-105, cx-120, cx-120}, new int[]{cy, cy-10, cy+10}, 3);
                g2.fillPolygon(new int[]{cx+105, cx+120, cx+120}, new int[]{cy, cy-10, cy+10}, 3);
            }
        }
    }

    private void drawBg(Graphics2D g2) { g2.setColor(new Color(0,0,0,150)); g2.fillRect(0, 0, getWidth(), getHeight()); }
    private void drawP(Graphics2D g2, int x, int y, Color c, int d) {
        int r = d/2;
        g2.setColor(new Color(0,0,0,50)); g2.fillOval(x-r+3, y-r+3, d, d);
        g2.setColor(c); g2.fillOval(x-r, y-r, d, d);
        g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(2)); g2.drawOval(x-r, y-r, d, d);
    }
    private void drawDice(Graphics2D g2, int x, int y, int s, int v) {
        g2.setColor(new Color(245,245,245)); g2.fillRoundRect(x, y, s, s, 20, 20);
        g2.setColor(Color.BLACK); g2.setStroke(new BasicStroke(2)); g2.drawRoundRect(x, y, s, s, 20, 20);
        int d=s/5, c=s/2, l=s/4, r=s*3/4;
        if(v%2!=0) fillD(g2,x+c,y+c,d);
        if(v>1){fillD(g2,x+l,y+l,d); fillD(g2,x+r,y+r,d);}
        if(v>3){fillD(g2,x+r,y+l,d); fillD(g2,x+l,y+r,d);}
        if(v==6){fillD(g2,x+l,y+c,d); fillD(g2,x+r,y+c,d);}
    }
    private void fillD(Graphics2D g2, int x, int y, int s){g2.fillOval(x-s/2,y-s/2,s,s);}
    private void drawSlot(Graphics2D g2, int x, int y, String t, Color c){
        g2.setColor(c); g2.fillRect(x-90, y-35, 180, 70);
        g2.setColor(Color.WHITE); g2.setFont(GameStyle.FONT_SLOT);
        FontMetrics fm=g2.getFontMetrics(); g2.drawString(t, x-fm.stringWidth(t)/2, y+10);
    }
    private void drawStar(Graphics2D g2, int cx, int cy, int size) {
        int pts=5; double in=size/2.5, out=size/1.0;
        GeneralPath s = new GeneralPath();
        for(int i=0; i<pts*2; i++) {
            double ang = (i*Math.PI)/pts - Math.PI/2;
            double r = (i%2==0)?out:in;
            double x = cx + Math.cos(ang)*r*0.5;
            double y = cy + Math.sin(ang)*r*0.5;
            if(i==0) s.moveTo(x,y); else s.lineTo(x,y);
        }
        s.closePath();
        g2.setColor(GameStyle.STAR); g2.fill(s);
        g2.setColor(Color.ORANGE); g2.setStroke(new BasicStroke(1)); g2.draw(s);
    }
}

// --- CONTROLLER ---

class GameController {
    private BoardGraph board;
    private GamePanel panel;
    private List<Player> allPlayers;
    private LinkedList<Player> turnQueue;

    private boolean started = false;
    private boolean anim = false;
    private int finalDice;
    private boolean forward;

    public GameController(BoardGraph board, GamePanel panel, List<Player> players) {
        this.board = board; this.panel = panel; this.allPlayers = players;
        this.turnQueue = new LinkedList<>();
    }

    public void startOrderRoulette() {
        panel.setStatus("Determining first player...");
        Timer t = new Timer(100, null);
        final long start = System.currentTimeMillis();
        final int[] idx = {0};

        t.addActionListener(e -> {
            if (System.currentTimeMillis() - start < 3000) {
                panel.setOrderAnim(true, idx[0]);
                idx[0] = (idx[0] + 1) % allPlayers.size();
            } else {
                ((Timer)e.getSource()).stop();
                int winner = (int)(Math.random() * allPlayers.size());
                panel.setOrderAnim(true, winner);
                Timer p = new Timer(1500, ev -> setupQueue(winner));
                p.setRepeats(false); p.start();
            }
        });
        t.start();
    }

    private void setupQueue(int winnerIdx) {
        Collections.rotate(allPlayers, -winnerIdx);
        turnQueue.clear();
        turnQueue.addAll(allPlayers);
        panel.setTurnQueue(turnQueue);
        Player first = turnQueue.peek();
        panel.setOrderAnim(false, 0);
        panel.setStatus("Ready! " + first.name + " first.");
        started = true;
        panel.refresh();
        JOptionPane.showMessageDialog(panel, first.name + " starts first!");
    }

    public void startTurn() {
        if (!started || anim) return;
        if (turnQueue.peek().position >= 64) return;
        anim = true;
        startDice();
    }

    private void startDice() {
        finalDice = (int)(Math.random() * 6) + 1;
        Timer t = new Timer(80, null);
        final long start = System.currentTimeMillis();
        t.addActionListener(e -> {
            if (System.currentTimeMillis() - start < 1000) {
                panel.setDiceAnim(true, (int)(Math.random() * 6) + 1);
            } else {
                ((Timer)e.getSource()).stop();
                panel.setDiceAnim(true, finalDice);
                Timer p = new Timer(600, ev -> {
                    panel.setDiceAnim(false, 1); startChaos();
                });
                p.setRepeats(false); p.start();
            }
        });
        t.start();
    }

    private void startChaos() {
        double prob = Math.random();
        forward = (prob <= 0.80);
        if (turnQueue.peek().position == 0) forward = true;

        Timer t = new Timer(20, null);
        final long start = System.currentTimeMillis();
        final int[] y = {0};
        t.addActionListener(e -> {
            if (System.currentTimeMillis() - start < 1500) {
                y[0] += 25; panel.setChaosAnim(true, y[0]);
            } else {
                ((Timer)e.getSource()).stop();
                panel.setChaosRes(forward);
                Timer p = new Timer(1000, ev -> {
                    panel.hideOverlay(); calcMove();
                });
                p.setRepeats(false); p.start();
            }
        });
        t.start();
    }

    private void calcMove() {
        Player curr = turnQueue.peek();
        List<Integer> path = GameLogic.findBestPath(board, curr.position, finalDice, forward);

        if (path == null) {
            path = new ArrayList<>(); path.add(curr.position);
        } else if(!path.isEmpty() && path.get(0) == curr.position) {
            path.remove(0);
        }

        String type = "Normal";
        for(int i=0; i<path.size(); i++) {
            int prev = (i==0)?curr.position:path.get(i-1);
            if(Math.abs(path.get(i)-prev)>1) type = "SECRET PATH!";
        }
        panel.setStatus(type + " Move: " + finalDice);

        final List<Integer> finalPath = path;
        final int[] idx = {0};
        Timer t = new Timer(400, e -> {
            if (idx[0] < finalPath.size()) {
                curr.position = finalPath.get(idx[0]);
                panel.refresh(); idx[0]++;
            } else {
                ((Timer)e.getSource()).stop();
                finishTurn(curr);
            }
        });
        t.start();
    }

    private void finishTurn(Player p) {
        anim = false;
        if (p.position == 64) {
            JOptionPane.showMessageDialog(panel, p.name + " WINS!");
            System.exit(0);
        }

        // 1. TRIGGER SECRET PATH (Jika berhenti di Prime Tile)
        Tile t = board.getTile(p.position);
        if (t != null && t.isPrime) {
            GameLogic.shuffleSecretPaths(board);
            JOptionPane.showMessageDialog(panel,
                    "⚡ MYSTIC TRIGGER! ⚡\n" + p.name + " stopped on Prime Tile " + p.position + ".\n" +
                            "Secret paths have been reshuffled!");
            panel.refresh();
        }

        // 2. TRIGGER STAR TILE (Extra Turn)
        if (p.position > 0 && p.position % 5 == 0) {
            Player same = turnQueue.poll();
            turnQueue.addFirst(same);
            JOptionPane.showMessageDialog(panel, "⭐ STAR TILE! ⭐\n" + p.name + " gets an EXTRA TURN!");
            panel.setStatus("Extra Turn for " + p.name + "!");
        } else {
            Player done = turnQueue.poll();
            turnQueue.offer(done);
            panel.setStatus("Click Roll for " + turnQueue.peek().name);
        }

        panel.refresh();
    }
}

// --- MAIN ---
public class SnakeLadderGame extends JFrame {
    public SnakeLadderGame() {
        setTitle("Snake Ladder: Final Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        String inp = JOptionPane.showInputDialog(this, "How many players? (2-4)", "2");
        if(inp == null) System.exit(0);
        int n = 2;
        try { n = Math.max(2, Math.min(4, Integer.parseInt(inp))); } catch (Exception e) {}

        JPanel pn = new JPanel(new GridLayout(n, 2, 10, 10));
        JTextField[] f = new JTextField[n];
        for(int i=0; i<n; i++) {
            pn.add(new JLabel("Player "+(i+1)+":"));
            f[i] = new JTextField("Player "+(i+1)); pn.add(f[i]);
        }
        if (JOptionPane.showConfirmDialog(null, pn, "Names", 2) != 0) System.exit(0);

        List<Player> ps = new ArrayList<>();
        for(int i=0; i<n; i++) ps.add(new Player(i, f[i].getText().trim(), GameStyle.P_COLORS[i]));

        BoardGraph b = new BoardGraph();
        GamePanel p = new GamePanel(b, ps);
        GameController c = new GameController(b, p, ps);

        add(p, BorderLayout.CENTER);

        JPanel bp = new JPanel();
        bp.setBackground(Color.WHITE); bp.setPreferredSize(new Dimension(600, 80));
        JButton b1 = new JButton("ROLL & GACHA");
        b1.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b1.setBackground(new Color(52, 73, 94)); b1.setForeground(Color.WHITE);
        b1.setPreferredSize(new Dimension(200, 50)); b1.setFocusPainted(false);
        b1.addActionListener(e -> c.startTurn());

        bp.add(b1);
        add(bp, BorderLayout.SOUTH);

        pack(); setLocationRelativeTo(null);
        SwingUtilities.invokeLater(c::startOrderRoulette);
    }
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new SnakeLadderGame().setVisible(true));
    }
}
