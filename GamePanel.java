import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class GamePanel extends JPanel {
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

        // 0. Background Gradient
        GradientPaint gp = new GradientPaint(0, 0, GameStyle.PANEL_BG_START, 0, getHeight(), GameStyle.PANEL_BG_END);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // 1. Draw Board Tiles
        for (Tile tile : board.getTiles()) {
            int row = (tile.id - 1) / 8;
            int col = (tile.id - 1) % 8;
            boolean isDark = (row % 2 == 0) ? (col % 2 != 0) : (col % 2 == 0);
            
            // Tile Background
            g2.setColor(isDark ? GameStyle.BG_BOARD_1 : GameStyle.BG_BOARD_2);
            g2.fillRoundRect(tile.x, tile.y, tile.size, tile.size, 10, 10);
            
            // Tile Border
            g2.setColor(new Color(200, 200, 200));
            g2.drawRoundRect(tile.x, tile.y, tile.size, tile.size, 10, 10);

            // --- LOGIC STAR TILE (Kelipatan 5) ---
            if (tile.id % 5 == 0) {
                drawStar(g2, tile.x + tile.size/2, tile.y + tile.size/2, 25);
            }

            // Tile Number
            g2.setColor(new Color(100, 100, 100));
            g2.setFont(GameStyle.FONT_TILE);
            g2.drawString(String.valueOf(tile.id), tile.x + 8, tile.y + 20);
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

                if (start < end) {
                    // LADDER
                    drawLadder(g2, x1, y1, x2, y2);
                } else {
                    // SNAKE
                    drawSnake(g2, x1, y1, x2, y2);
                }
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
        g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI", Font.BOLD, 16)); g2.drawString("START", 20, 640);

        // 3. Overlays
        drawOverlays(g2);

        // 4. Status Bar (Ambil nama dari HEAD Queue)
        if (!isDeterminingOrder && turnQueue != null && !turnQueue.isEmpty()) {
            Player p = turnQueue.peek(); // Lihat siapa yg paling depan
            
            // Status Bar Background
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRoundRect(150, 5, 350, 35, 20, 20);
            
            // Player Color Indicator
            g2.setColor(p.color);
            g2.fillOval(160, 10, 25, 25);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(160, 10, 25, 25);

            g2.setColor(Color.WHITE); g2.setFont(GameStyle.FONT_INFO);
            FontMetrics fm = g2.getFontMetrics();

            String fullStatus = p.name + "'s Turn | " + statusMsg;
            g2.drawString(fullStatus, 325 - fm.stringWidth(fullStatus)/2, 28);
        }
    }

    private void drawLadder(Graphics2D g2, int x1, int y1, int x2, int y2) {
        g2.setColor(new Color(139, 69, 19)); // Wood color
        g2.setStroke(new BasicStroke(5));
        
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double perpAngle = angle + Math.PI / 2;
        int width = 10;
        
        int lx1 = (int)(x1 + width * Math.cos(perpAngle));
        int ly1 = (int)(y1 + width * Math.sin(perpAngle));
        int lx2 = (int)(x2 + width * Math.cos(perpAngle));
        int ly2 = (int)(y2 + width * Math.sin(perpAngle));
        
        int rx1 = (int)(x1 - width * Math.cos(perpAngle));
        int ry1 = (int)(y1 - width * Math.sin(perpAngle));
        int rx2 = (int)(x2 - width * Math.cos(perpAngle));
        int ry2 = (int)(y2 - width * Math.sin(perpAngle));
        
        g2.drawLine(lx1, ly1, lx2, ly2);
        g2.drawLine(rx1, ry1, rx2, ry2);
        
        // Rungs
        int steps = 10;
        for (int i = 1; i < steps; i++) {
            double ratio = (double)i / steps;
            int mx1 = (int)(lx1 + (lx2 - lx1) * ratio);
            int my1 = (int)(ly1 + (ly2 - ly1) * ratio);
            int mx2 = (int)(rx1 + (rx2 - rx1) * ratio);
            int my2 = (int)(ry1 + (ry2 - ry1) * ratio);
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(mx1, my1, mx2, my2);
        }
    }

    private void drawSnake(Graphics2D g2, int x1, int y1, int x2, int y2) {
        g2.setColor(new Color(231, 76, 60));
        g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        QuadCurve2D q = new QuadCurve2D.Float();
        // Control point for curve
        int cx = (x1 + x2) / 2 + (Math.random() > 0.5 ? 30 : -30);
        int cy = (y1 + y2) / 2;
        q.setCurve(x1, y1, cx, cy, x2, y2);
        g2.draw(q);
        
        // Head
        g2.setColor(new Color(192, 57, 43));
        g2.fillOval(x2 - 8, y2 - 8, 16, 16);
        g2.setColor(Color.YELLOW);
        g2.fillOval(x2 - 3, y2 - 3, 6, 6); // Eye
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
            
            // Modern Card Style
            g2.setColor(Color.WHITE); 
            g2.fillRoundRect(cx - 150, cy - 120, 300, 240, 20, 20);
            g2.setColor(new Color(200, 200, 200)); 
            g2.setStroke(new BasicStroke(1)); 
            g2.drawRoundRect(cx - 150, cy - 120, 300, 240, 20, 20);
            
            g2.setColor(new Color(50, 50, 50));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 20)); 
            FontMetrics fm = g2.getFontMetrics();
            String title = "WHO GOES FIRST?";
            g2.drawString(title, cx - fm.stringWidth(title)/2, cy - 80);
            
            int startY = cy - 40;
            for (int i = 0; i < allPlayers.size(); i++) {
                if (i == highlightedOrderIdx) {
                    g2.setColor(new Color(52, 152, 219, 50)); 
                    g2.fillRoundRect(cx - 130, startY + (i*35) - 25, 260, 30, 10, 10);
                    g2.setColor(new Color(52, 152, 219)); 
                    g2.setStroke(new BasicStroke(2)); 
                    g2.drawRoundRect(cx - 130, startY + (i*35) - 25, 260, 30, 10, 10);
                }
                g2.setColor(allPlayers.get(i).color); 
                g2.fillOval(cx - 120, startY + (i*35) - 20, 20, 20);
                
                g2.setColor(Color.BLACK); 
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                g2.drawString(allPlayers.get(i).name, cx - 90, startY + (i*35) - 5);
            }
        } else if ((isRollingDice || isRollingChaos) && turnQueue != null && !turnQueue.isEmpty()) {
            drawOverlayBg(g2);
            int cx = getWidth()/2, cy = getHeight()/2;
            
            // Glassmorphism-like background
            g2.setColor(new Color(255, 255, 255, 240)); 
            g2.fillRoundRect(cx - 120, cy - 120, 240, 240, 30, 30);
            g2.setStroke(new BasicStroke(5));
            g2.setColor(turnQueue.peek().color); 
            g2.drawRoundRect(cx - 120, cy - 120, 240, 240, 30, 30);

            if (isRollingDice) {
                drawDiceFace(g2, cx - 60, cy - 70, 120, displayDiceVal);
                g2.setColor(Color.BLACK); g2.setFont(GameStyle.FONT_INFO);
                String txt = turnQueue.peek().name + " Rolling...";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(txt, cx - fm.stringWidth(txt)/2, cy + 80);
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
                
                // Slot Machine Frame
                g2.setColor(new Color(80, 80, 80)); 
                g2.setStroke(new BasicStroke(8)); 
                g2.drawRect(cx - 100, cy - 80, 200, 160);
                
                // Pointers
                g2.setColor(Color.ORANGE);
                g2.fillPolygon(new int[]{cx-110, cx-130, cx-130}, new int[]{cy, cy-15, cy+15}, 3);
                g2.fillPolygon(new int[]{cx+110, cx+130, cx+130}, new int[]{cy, cy-15, cy+15}, 3);
            }
        }
    }

    private void drawOverlayBg(Graphics2D g2) { g2.setColor(new Color(0,0,0,150)); g2.fillRect(0, 0, getWidth(), getHeight()); }
    
    private void drawPlayer(Graphics2D g2, int cx, int cy, Color c, int diameter) {
        int r = diameter / 2;
        
        // Shadow
        g2.setColor(new Color(0,0,0,80)); 
        g2.fillOval(cx - r + 3, cy - r + 5, diameter, diameter);
        
        // 3D Marble Effect
        Point2D center = new Point2D.Float(cx - r/3, cy - r/3);
        float radius = diameter/1.2f;
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {Color.WHITE, c};
        RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);
        g2.setPaint(p);
        g2.fillOval(cx - r, cy - r, diameter, diameter);
        
        // Outline
        g2.setColor(c.darker()); 
        g2.setStroke(new BasicStroke(1)); 
        g2.drawOval(cx - r, cy - r, diameter, diameter);
    }
    
    private void drawDiceFace(Graphics2D g2, int x, int y, int size, int val) {
        // 3D Dice Look
        g2.setColor(new Color(245, 245, 245)); 
        g2.fillRoundRect(x, y, size, size, 20, 20);
        g2.setColor(new Color(200, 200, 200));
        g2.setStroke(new BasicStroke(3)); 
        g2.drawRoundRect(x, y, size, size, 20, 20);
        
        g2.setColor(Color.BLACK);
        int dot = size/5, c = size/2, l = size/4, r = size*3/4;
        if(val%2!=0) fillDot(g2,x+c,y+c,dot);
        if(val>1){fillDot(g2,x+l,y+l,dot); fillDot(g2,x+r,y+r,dot);}
        if(val>3){fillDot(g2,x+r,y+l,dot); fillDot(g2,x+l,y+r,dot);}
        if(val==6){fillDot(g2,x+l,y+c,dot); fillDot(g2,x+r,y+c,dot);}
    }
    private void fillDot(Graphics2D g2, int x, int y, int s){
        g2.fillOval(x-s/2,y-s/2,s,s);
    }
    private void drawSlotItem(Graphics2D g2, int x, int y, String t, Color c){
        g2.setColor(c); 
        g2.fillRoundRect(x-90, y-35, 180, 70, 15, 15);
        g2.setColor(Color.WHITE); 
        g2.setFont(GameStyle.FONT_SLOT);
        FontMetrics fm=g2.getFontMetrics(); 
        g2.drawString(t, x-fm.stringWidth(t)/2, y+10);
    }
}
