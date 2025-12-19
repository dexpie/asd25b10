import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class GamePanel extends JPanel {
    private BoardGraph board;
    private List<Player> allPlayers; // Untuk menggambar semua player
    private LinkedList<Player> turnQueue; // Antrian giliran (Queue)

    private String statusMsg = "Setting up game...";
    private Image mapImage;
    private Image[] diceImages = new Image[6]; // Array to hold dice images

    // --- PHYSICS DICE SYSTEM ---
    private List<RollingDice> activeDice = new ArrayList<>();

    private class RollingDice {
        double x, y;
        double vx, vy;
        double angle = 0;
        double vAngle = 0;
        int size = 60;
        int currentValue = 1;
        int finalValue;
        boolean stopped = false;
        int lifeTime = 0; // Safety timer to prevent infinite rolling

        public RollingDice(int finalVal, int startX, int startY) {
            this.finalValue = finalVal;
            this.x = startX;
            this.y = startY;
            // Stronger initial throw
            this.vx = (Math.random() * 20) - 10; // -10 to 10
            this.vy = -(Math.random() * 10 + 20); // -20 to -30 (High arc)
            this.vAngle = (Math.random() * 0.8) - 0.4; // Fast spin
        }

        public void update() {
            if (stopped) return;
            lifeTime++;

            // SAFETY: Force stop after ~2.5 seconds (150 frames)
            if (lifeTime > 150) {
                stopped = true;
                currentValue = finalValue;
                return;
            }

            x += vx;
            y += vy;
            angle += vAngle;
            vy += 0.9; // Gravity

            // Air Resistance (Drag)
            vx *= 0.99;
            vAngle *= 0.99;

            // Floor Interaction
            int floor = getHeight() - 150;
            if (y + size > floor) {
                y = floor - size;

                // Bounce Logic
                vy = -vy * 0.5; // Heavy object (low bounce)

                // Ground Friction (Very high)
                vx *= 0.85;
                vAngle *= 0.8;

                // Stop Threshold (Aggressive)
                if (Math.abs(vy) < 1.0 && Math.abs(vx) < 0.5 && Math.abs(vAngle) < 0.1) {
                    stopped = true;
                    currentValue = finalValue;
                }
            }

            // Wall Bounce
            if (x < 0) {
                x = 0;
                vx = -vx * 0.6;
                vAngle *= 0.8; // Wall friction
            }
            if (x + size > getWidth()) {
                x = getWidth() - size;
                vx = -vx * 0.6;
                vAngle *= 0.8;
            }

            // Face Randomization Logic
            if (!stopped) {
                // If moving fast or spinning fast, randomize
                if (Math.abs(vx) > 2.0 || Math.abs(vy) > 2.0 || Math.abs(vAngle) > 0.15) {
                    if (animFrame % 3 == 0) currentValue = (int)(Math.random() * 6) + 1;
                } else {
                    // If slowing down significantly, show final value
                    currentValue = finalValue;
                }
            }
        }
    }

    // --- PARTICLE SYSTEM ---
    private List<Particle> particles = new ArrayList<>();
    private List<FloatingText> floatingTexts = new ArrayList<>();

    private class FloatingText {
        double x, y;
        String text;
        Color color;
        int life;
        int maxLife = 60;

        public FloatingText(double x, double y, String text, Color color) {
            this.x = x;
            this.y = y;
            this.text = text;
            this.color = color;
            this.life = maxLife;
        }

        public boolean update() {
            y -= 1.0; // Float up
            life--;
            return life > 0;
        }

        public void draw(Graphics2D g2) {
            float alpha = (float)life / maxLife;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(Color.BLACK); // Shadow
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            g2.drawString(text, (int)x + 2, (int)y + 2);
            g2.setColor(color);
            g2.drawString(text, (int)x, (int)y);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }

    private class Particle {
        double x, y, vx, vy;
        int life;
        int maxLife;
        Color color;
        int size;

        public Particle(int x, int y, Color c) {
            this.x = x;
            this.y = y;
            this.color = c;
            double angle = Math.random() * Math.PI * 2;
            double speed = Math.random() * 3 + 1;
            this.vx = Math.cos(angle) * speed;
            this.vy = Math.sin(angle) * speed;
            this.maxLife = (int)(Math.random() * 30) + 20;
            this.life = maxLife;
            this.size = (int)(Math.random() * 4) + 2;
        }

        public boolean update() {
            x += vx;
            y += vy;
            vy += 0.1; // Gravity
            life--;
            return life > 0;
        }

        public void draw(Graphics2D g2) {
            int alpha = (int)(255.0 * life / maxLife);
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            g2.fillRect((int)x, (int)y, size, size);
        }
    }

    public void spawnParticles(int x, int y, Color c, int count) {
        for(int i=0; i<count; i++) {
            particles.add(new Particle(x, y, c));
        }
    }

    public void spawnFloatingText(int x, int y, String text, Color c) {
        floatingTexts.add(new FloatingText(x, y, text, c));
    }

    public void spawnFloatingTextAtPlayer(Player p, String text, Color c) {
        int idx = p.position;
        if (idx < 1) idx = 1;
        if (idx > 64) idx = 64;
        Tile t = board.getTile(idx);
        if (t != null) {
            int x = t.x + t.size/2 + mapOffsetX;
            int y = t.y + t.size/2 + mapOffsetY;
            spawnFloatingText(x, y - 30, text, c);
        }
    }

    public void spawnParticlesAtPlayer(Player p, Color c, int count) {
        int idx = p.position;
        if (idx < 1) idx = 1;
        if (idx > 64) idx = 64;
        Tile t = board.getTile(idx);
        if (t != null) {
            int x = t.x + t.size/2 + mapOffsetX;
            int y = t.y + t.size/2 + mapOffsetY;
            spawnParticles(x, y, c, count);
        }
    }

    public void throwDice(int finalVal) {
        activeDice.clear();
        isRollingDice = true;
        // Start from bottom center
        activeDice.add(new RollingDice(finalVal, getWidth() / 2 - 30, getHeight() + 50));
    }

    public boolean isDiceAnimationComplete() {
        if (activeDice.isEmpty()) return true;
        for (RollingDice d : activeDice) {
            if (!d.stopped) return false;
        }
        return true;
    }

    // States
    private boolean isDeterminingOrder = false;
    private int highlightedOrderIdx = 0;
    private boolean isRollingDice = false;
    private int displayDiceVal = 1;
    private boolean isRollingChaos = false;
    private int slotScrollY = 0;
    private boolean finalChaosResult = true;
    private boolean showFinalChaos = false;
    // Debug helpers
    private boolean debugShowCenters = false; // toggle to show tile centers and ids

    // Animation
    private int animFrame = 0;
    private javax.swing.Timer animTimer;

    // Map display bounds for debug clicker
    private int mapOffsetX = 0, mapOffsetY = 0;
    private int drawMapW = 0, drawMapH = 0;

    public GamePanel(BoardGraph board, List<Player> allPlayers) {
        this.board = board; this.allPlayers = allPlayers;
        setPreferredSize(new Dimension(650, 650));
        setBackground(new Color(10, 10, 20)); // Darker background for cyber effect

        // Animation Timer (approx 60fps)
        animTimer = new javax.swing.Timer(16, e -> {
            animFrame++;
            // Update Physics Dice
            if (isRollingDice && !activeDice.isEmpty()) {
                for (RollingDice d : activeDice) d.update();
            }

            // Update Particles
            Iterator<Particle> it = particles.iterator();
            while(it.hasNext()) {
                if (!it.next().update()) it.remove();
            }

            // Update Floating Text
            Iterator<FloatingText> itText = floatingTexts.iterator();
            while(itText.hasNext()) {
                if (!itText.next().update()) itText.remove();
            }

            // Update Player Animation (Smooth Movement)
            for (Player p : allPlayers) {
                double diff = p.position - p.currentVisualPos;
                if (Math.abs(diff) > 0.05) {
                    // Speed depends on distance (faster for long jumps like snakes/ladders)
                    double speed = 0.15;
                    if (Math.abs(diff) > 5) speed = 0.5; // Fast slide for jumps

                    if (diff > 0) p.currentVisualPos += speed;
                    else p.currentVisualPos -= speed;

                    // Snap if close
                    if (Math.abs(p.position - p.currentVisualPos) < speed) {
                        p.currentVisualPos = p.position;
                    }
                } else {
                    p.currentVisualPos = p.position;
                }
            }

            repaint();
        });
        animTimer.start();

        // DEBUG CLICKER - Prints X,Y coordinates when clicking on the map
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Calculate relative position within the map image
                int relX = e.getX() - mapOffsetX;
                int relY = e.getY() - mapOffsetY;
                // Normalize to 0-1000 range for consistency
                double normX = drawMapW > 0 ? (double) relX / drawMapW * 1000 : 0;
                double normY = drawMapH > 0 ? (double) relY / drawMapH * 1000 : 0;
                System.out.println("DEBUG CLICK: Screen(" + e.getX() + ", " + e.getY() +
                        ") | MapRelative(" + relX + ", " + relY +
                        ") | Normalized(" + (int)normX + ", " + (int)normY + ")");
            }
        });

        // Load Dice Images
        for (int i = 0; i < 6; i++) {
            try {
                java.net.URL dUrl = getClass().getResource("/resources/dice" + (i+1) + ".png");
                if (dUrl != null) {
                    diceImages[i] = new ImageIcon(dUrl).getImage();
                } else {
                    java.io.File df = new java.io.File("resources/dice" + (i+1) + ".png");
                    if (df.exists()) {
                        diceImages[i] = new ImageIcon(df.getAbsolutePath()).getImage();
                    }
                }
            } catch (Exception e) {
                System.out.println("Failed to load dice image " + (i+1));
            }
        }

        try {
            java.net.URL url = getClass().getResource("/resources/map.png");
            System.out.println("DEBUG: resource URL = " + url);
            if (url != null) {
                mapImage = new ImageIcon(url).getImage();
            } else {
                java.io.File f = new java.io.File("resources/map.png");
                if (f.exists()) {
                    System.out.println("DEBUG: loading background from project resources folder: " + f.getAbsolutePath());
                    mapImage = new ImageIcon(f.getAbsolutePath()).getImage();
                } else {
                    System.out.println("DEBUG: background not found in classpath or resources folder. Will prompt user to pick an image.");
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        try {
                            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
                            fc.setDialogTitle("Select background image (optional)");
                            int rv = fc.showOpenDialog(this);
                            if (rv == javax.swing.JFileChooser.APPROVE_OPTION) {
                                java.io.File sel = fc.getSelectedFile();
                                try {
                                    mapImage = new ImageIcon(sel.getAbsolutePath()).getImage();
                                    repaint();
                                } catch (Exception ex) {
                                    System.out.println("Failed to load selected image: " + ex.getMessage());
                                }
                            }
                        } catch (Exception ex) {
                            System.out.println("Error opening file chooser: " + ex.getMessage());
                        }
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Gambar peta tidak ditemukan! " + e.getMessage());
        }
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

        // 0. Cyber Grid Background
        drawCyberGrid(g2);

        // 0.5 Board Container (Card look)
        int boardMargin = 20;
        g2.setColor(new Color(0, 0, 0, 100)); // Semi-transparent shadow
        g2.fillRoundRect(boardMargin + 6, boardMargin + 6, getWidth() - 2*boardMargin, getHeight() - 100, 20, 20);
        g2.setColor(new Color(255, 255, 255, 20)); // Glassy surface
        g2.fillRoundRect(boardMargin, boardMargin, getWidth() - 2*boardMargin, getHeight() - 100, 20, 20);

        // Draw background map image inside the card area (if available)
        int bx = boardMargin;
        int by = boardMargin;
        int bw = getWidth() - 2 * boardMargin;
        int bh = getHeight() - 100;
        // mapOffsetX/Y represent the top-left corner where the map is actually
        // drawn; they default to the card origin and are adjusted if we center
        // the image to preserve aspect ratio.
        // Use instance variables for debug clicker access
        this.mapOffsetX = bx;
        this.mapOffsetY = by;
        this.drawMapW = bw;
        this.drawMapH = bh;
        if (mapImage != null) {
            java.awt.Composite oldComp = g2.getComposite();
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.92f));

            // Preserve aspect ratio: scale image to fit inside (bw x bh)
            int refW = mapImage.getWidth(this);
            int refH = mapImage.getHeight(this);
            if (refW > 0 && refH > 0) {
                double scale = Math.min((double)bw / refW, (double)bh / refH);
                this.drawMapW = (int) Math.round(refW * scale);
                this.drawMapH = (int) Math.round(refH * scale);
                int dx = (bw - drawMapW) / 2;
                int dy = (bh - drawMapH) / 2;
                this.mapOffsetX = bx + dx;
                this.mapOffsetY = by + dy;

                g2.drawImage(mapImage, mapOffsetX, mapOffsetY, drawMapW, drawMapH, this);

                // Normalize using intrinsic image size. MapMaker used 720x540 in your screenshot,
                // so force that reference to ensure coordinates match the capture.
                board.computeNormalizedFromReference(refW, refH);
                // Force MapMaker reference size (helps when MapMaker capture used 720x540)
                board.computeNormalizedFromReference(1000, 760);
                // adapt tile pixel size to map width so markers scale with map
                // SMALLER TILES: Reduced multiplier
                int tilePx = Math.max(20, Math.min(40, drawMapW / 25));
                board.updateTilesForMapSize(drawMapW, drawMapH, tilePx);
            } else {
                // If we can't read intrinsic size, fall back to stretched draw.
                g2.drawImage(mapImage, bx, by, bw, bh, this);
                int tilePx = 32; // Smaller default
                board.updateTilesForMapSize(bw, bh, tilePx);
            }
            g2.setComposite(oldComp);
        } else {
            // Fallback if no image
            g2.setColor(new Color(50, 50, 50));
            g2.fillRect(bx, by, bw, bh);
            g2.setColor(Color.WHITE);
            g2.drawString("Map Image Not Found", bx + bw/2 - 50, by + bh/2);
        }

        // Chat Log removed (moved to side panel)

        // 1.4 Draw Path (Dashed Line) - Treasure Map Style
        Stroke oldStroke = g2.getStroke();
        g2.setColor(new Color(139, 69, 19, 180)); // Saddle Brown
        float[] dash = {10.0f};
        g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));

        List<Tile> tiles = board.getTiles();
        // Sort tiles by ID just in case
        tiles.sort(Comparator.comparingInt(t -> t.id));

        if (tiles.size() > 1) {
            GeneralPath path = new GeneralPath();
            Tile first = tiles.get(0);
            path.moveTo(first.x + first.size/2 + mapOffsetX, first.y + first.size/2 + mapOffsetY);

            for (int i = 1; i < tiles.size(); i++) {
                Tile t = tiles.get(i);
                path.lineTo(t.x + t.size/2 + mapOffsetX, t.y + t.size/2 + mapOffsetY);
            }
            g2.draw(path);
        }
        g2.setStroke(oldStroke);

        // 1.5 Draw Connections (Teleporters) - Glowing Data Stream Portals
        Map<Integer, Integer> conns = board.getConnections();
        for (Map.Entry<Integer, Integer> entry : conns.entrySet()) {
            int start = entry.getKey();
            int end = entry.getValue();
            Tile t1 = board.getTile(start);
            Tile t2 = board.getTile(end);
            if (t1 != null && t2 != null) {
                int x1 = t1.x + t1.size/2 + mapOffsetX;
                int y1 = t1.y + t1.size/2 + mapOffsetY;
                int x2 = t2.x + t2.size/2 + mapOffsetX;
                int y2 = t2.y + t2.size/2 + mapOffsetY;

                // Draw glowing data stream line between portals
                drawPortalLine(g2, x1, y1, x2, y2);

                // Draw Teleport Pads with vortex effect
                boolean isLadder = start < end;
                Color portalColor = isLadder ? DigimonThemeFactory.PORTAL_GOLD : DigimonThemeFactory.PORTAL_GLOW;

                drawTeleport(g2, x1, y1, portalColor, true);  // Entry point
                drawTeleport(g2, x2, y2, portalColor, false); // Exit point
            }
        }

        // 1. Draw Board Tiles as HEXAGON NODES
        for (Tile tile : board.getTiles()) {
            int cx = tile.x + tile.size/2 + mapOffsetX;
            int cy = tile.y + tile.size/2 + mapOffsetY;

            // Draw hexagon node (includes prime glow and star tile highlight)
            drawHexagonNode(g2, cx, cy, tile.id, 18);

            // --- SCORE TILE ---
            if (tile.hasScore()) {
                // Smaller Score Bubble
                int bubbleSize = 12;
                int bubbleX = cx + 8;
                int bubbleY = cy - 14;

                g2.setColor(GameStyle.SCORE_COLOR);
                g2.fillOval(bubbleX, bubbleY, bubbleSize, bubbleSize);

                // Border
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawOval(bubbleX, bubbleY, bubbleSize, bubbleSize);

                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.BOLD, 9)); // Smaller Font
                String sc = String.valueOf(tile.getScore());
                FontMetrics fmScore = g2.getFontMetrics();
                g2.drawString(sc, bubbleX + (bubbleSize - fmScore.stringWidth(sc))/2, bubbleY + (bubbleSize - fmScore.getHeight())/2 + fmScore.getAscent());
            }
        }

        // Debug overlay: show tile centers and ids (useful to fine-tune normalization)
        if (debugShowCenters) {
            g2.setColor(new Color(255, 0, 0, 200));
            for (Tile tile : board.getTiles()) {
                int cx = tile.x + tile.size/2 + mapOffsetX;
                int cy = tile.y + tile.size/2 + mapOffsetY;
                g2.fillOval(cx - 4, cy - 4, 8, 8);
                g2.setColor(new Color(0,0,0,180));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                String id = String.valueOf(tile.id);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(id, cx - fm.stringWidth(id)/2, cy - 8);
                g2.setColor(new Color(255, 0, 0, 200));
            }
        }

        // 2. Draw Players (Smooth Animation)
        for (int i = 0; i < allPlayers.size(); i++) {
            Player p = allPlayers.get(i);
            int px, py;

            if (p.currentVisualPos <= 0.1) {
                // Start Position
                px = 40 + (i * 30);
                py = 610;
            } else {
                // Interpolate
                int idx1 = (int) p.currentVisualPos;
                int idx2 = idx1 + 1;
                if (idx2 > 64) idx2 = 64;

                double fraction = p.currentVisualPos - idx1;

                // Get Tile 1
                Tile t1 = board.getTile(idx1);
                if (t1 == null) t1 = board.getTile(1); // Safety

                int x1 = t1.x + t1.size/2 + mapOffsetX;
                int y1 = t1.y + t1.size/2 + mapOffsetY;

                // Get Tile 2 (if fraction > 0)
                int x2 = x1, y2 = y1;
                if (fraction > 0.01 && idx2 != idx1) {
                    Tile t2 = board.getTile(idx2);
                    if (t2 != null) {
                        x2 = t2.x + t2.size/2 + mapOffsetX;
                        y2 = t2.y + t2.size/2 + mapOffsetY;
                    }
                }

                px = (int) (x1 + (x2 - x1) * fraction);
                py = (int) (y1 + (y2 - y1) * fraction);
            }

            // Draw Player
            if (p.avatar != null) {
                g2.drawImage(p.avatar, px - 12, py - 12, 24, 24, this);
                g2.setColor(p.color);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(px - 12, py - 12, 24, 24);
            } else {
                drawGlossyToken(g2, px, py, 12, p.color, p.primePowerMode);
            }

            // Draw Name (Small)
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            g2.setColor(Color.WHITE);
            g2.drawString(p.name, px - 10, py - 15);
        }

        // Start Label
        g2.setColor(Color.WHITE);
        g2.setFont(GameStyle.FONT_TILE_BIG);
        g2.drawString("START", 20, 635);

        // Draw Particles
        for (Particle p : particles) {
            p.draw(g2);
        }

        // Draw Floating Text
        for (FloatingText ft : floatingTexts) {
            ft.draw(g2);
        }

        // 3. Overlays
        drawOverlays(g2);

        // 4. Status Bar (Floating HUD)
        if (!isDeterminingOrder && turnQueue != null && !turnQueue.isEmpty()) {
            Player p = turnQueue.peek();

            int hudW = 400;
            int hudH = 50;
            int hudX = (getWidth() - hudW) / 2;
            int hudY = 10;

            // HUD Shadow
            g2.setColor(new Color(0,0,0,50));
            g2.fillRoundRect(hudX + 4, hudY + 4, hudW, hudH, 40, 40);

            // HUD Body (Dark Glass)
            g2.setColor(new Color(20, 20, 40, 230)); // Dark Blue/Black
            g2.fillRoundRect(hudX, hudY, hudW, hudH, 40, 40);
            g2.setColor(new Color(0, 255, 255)); // Cyan Border
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(hudX, hudY, hudW, hudH, 40, 40);

            // Player Indicator
            g2.setColor(p.color);
            g2.fillOval(hudX + 15, hudY + 10, 30, 30);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(hudX + 15, hudY + 10, 30, 30);

            // Text
            g2.setColor(Color.WHITE); // White Text for Dark Background
            g2.setFont(GameStyle.FONT_INFO);
            FontMetrics fm = g2.getFontMetrics();
            String fullStatus = p.name + " (Score: " + p.getScore() + ") | " + statusMsg;
            g2.drawString(fullStatus, hudX + 60, hudY + 30);
        }
    }

    private void drawCyberGrid(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        // Dark Blue Background
        g2.setColor(new Color(10, 10, 30));
        g2.fillRect(0, 0, w, h);

        // Moving Grid
        g2.setColor(new Color(0, 255, 255, 30)); // Cyan low opacity
        int gridSize = 40;
        int offset = (animFrame % gridSize);

        // Vertical Lines
        for (int x = -offset; x < w; x += gridSize) {
            g2.drawLine(x, 0, x, h);
        }

        // Horizontal Lines (Perspective effect?)
        for (int y = -offset; y < h; y += gridSize) {
            g2.drawLine(0, y, w, y);
        }

        // Random Binary Code
        g2.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2.setColor(new Color(0, 255, 0, 50));
        if (animFrame % 5 == 0) {
            for (int i = 0; i < 10; i++) {
                int rx = (int)(Math.random() * w);
                int ry = (int)(Math.random() * h);
                g2.drawString(Math.random() > 0.5 ? "1" : "0", rx, ry);
            }
        }
    }

    /**
     * Draws a glowing "Data Stream" portal connection.
     * Features: outer glow, inner glow, animated dashed core line, vortex effects at endpoints.
     */
    private void drawTeleport(Graphics2D g2, int x, int y, Color c, boolean isStart) {
        // This method is called for each endpoint. We'll draw the vortex effect here.
        drawVortex(g2, x, y, isStart ? 14 : 12, isStart ? DigimonThemeFactory.PORTAL_GOLD : DigimonThemeFactory.PORTAL_GLOW);
    }

    /**
     * Draws a CURVED glowing data stream line between two portal points.
     * FIXED: Uses QuadCurve2D for curved line, Green/Gold colors.
     */
    private void drawPortalLine(Graphics2D g2, int x1, int y1, int x2, int y2) {
        float phase = (animFrame % 60) / 60f * 20f;

        // Calculate control point for curve (perpendicular offset)
        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;
        double dx = x2 - x1;
        double dy = y2 - y1;
        double len = Math.sqrt(dx*dx + dy*dy);
        // Perpendicular offset (curve amount = 30% of distance)
        double curveAmount = len * 0.3;
        int ctrlX = (int)(midX + (-dy / len) * curveAmount);
        int ctrlY = (int)(midY + (dx / len) * curveAmount);

        // Create curved path
        QuadCurve2D curve = new QuadCurve2D.Double(x1, y1, ctrlX, ctrlY, x2, y2);

        // Outer glow (GREEN)
        g2.setColor(new Color(0x00, 0xFF, 0x66, 50));
        g2.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(curve);

        // Inner glow (GOLD)
        g2.setColor(new Color(0xFF, 0xD7, 0x00, 80));
        g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(curve);

        // Animated dashed core (GREEN/CYAN)
        g2.setColor(new Color(0x00, 0xFF, 0xAA));
        float[] dash = {8f, 6f};
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, dash, phase));
        g2.draw(curve);
    }

    /**
     * Draws a pulsing vortex effect (expanding rings) at a portal point.
     */
    private void drawVortex(Graphics2D g2, int cx, int cy, int maxRadius, Color color) {
        int rings = 3;
        float pulsePhase = (animFrame % 40) / 40f;

        for (int i = 0; i < rings; i++) {
            float t = (pulsePhase + i * 0.33f) % 1f;
            int r = (int)(maxRadius * t);
            int alpha = (int)(180 * (1 - t));

            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        }
    }

    /**
     * Creates a hexagon polygon centered at (cx, cy) with given size.
     */
    private Polygon createHexagon(int cx, int cy, int size) {
        Polygon hex = new Polygon();
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 3 * i - Math.PI / 6;
            int x = cx + (int)(size * Math.cos(angle));
            int y = cy + (int)(size * Math.sin(angle));
            hex.addPoint(x, y);
        }
        return hex;
    }

    /**
     * Draws a hexagon node at the specified tile position.
     * FIXED: Smaller nodes (15px), HIGH TRANSPARENCY so map is visible, CYAN NEON border.
     */
    private void drawHexagonNode(Graphics2D g2, int cx, int cy, int tileId, int size) {
        // FIXED SIZE: Use 15px for small nodes that don't cover the map
        int nodeSize = 15;

        // Global Pulse for all nodes
        float pulse = (float)(0.5 + 0.5 * Math.sin(animFrame * 0.05));

        // Check Special Node Types
        boolean isSnakeStart = false;
        boolean isLadderStart = false;
        Map<Integer, Integer> conns = board.getConnections();
        if (conns.containsKey(tileId)) {
            int dest = conns.get(tileId);
            if (dest > tileId) isLadderStart = true;
            else isSnakeStart = true;
        }

        Polygon hex = createHexagon(cx, cy, nodeSize);

        if (isSnakeStart) {
            // VIRUS NODE (Red/Glitchy)
            g2.setColor(new Color(255, 50, 50, 80));
            g2.fill(hex);
            g2.setColor(new Color(255, 0, 0, 200));
            g2.setStroke(new BasicStroke(2));
            g2.draw(hex);

            // Glitch lines
            if (animFrame % 20 < 10) {
                g2.setColor(Color.RED);
                g2.drawLine(cx - 10, cy, cx + 10, cy);
            }
        } else if (isLadderStart) {
            // UPLOAD NODE (Gold/Upward)
            g2.setColor(new Color(255, 215, 0, 80));
            g2.fill(hex);
            g2.setColor(new Color(255, 215, 0, 200));
            g2.setStroke(new BasicStroke(2));
            g2.draw(hex);

            // Up Arrow
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            g2.drawString("▲", cx - 4, cy + 4);
        } else {
            // NORMAL NODE
            // Fill with HIGH TRANSPARENCY (Alpha 60) - can see map underneath
            g2.setColor(new Color(0x00, 0xAA, 0xCC, 60));
            g2.fill(hex);

            // CYAN NEON border (#00FFFF) with Pulse
            int alpha = 150 + (int)(100 * pulse);
            g2.setColor(new Color(0x00, 0xFF, 0xFF, alpha));
            g2.setStroke(new BasicStroke(2));
            g2.draw(hex);
        }

        // Prime number glow (purple)
        if (isPrime(tileId)) {
            g2.setColor(new Color(180, 100, 255, 120)); // Brighter purple glow
            Polygon outerHex = createHexagon(cx, cy, nodeSize + 4);
            g2.setStroke(new BasicStroke(2));
            g2.draw(outerHex);
        }

        // Star tile indicator (gold shimmer, not solid fill)
        if (tileId > 0 && tileId % 5 == 0) {
            float shimmer = (float)(0.5 + 0.5 * Math.sin(animFrame * 0.1 + tileId));
            g2.setColor(new Color(255, 215, 0, (int)(80 * shimmer)));
            g2.fill(hex);
        }

        // Node number - smaller font, centered
        if (!isLadderStart) { // Don't draw number over arrow
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            FontMetrics fm = g2.getFontMetrics();
            String num = String.valueOf(tileId);
            g2.drawString(num, cx - fm.stringWidth(num)/2, cy + fm.getAscent()/2 - 2);
        }
    }

    /**
     * Draws a glossy player token PERFECTLY CENTERED on the node.
     * FIXED: Player size = 80% of node, perfectly centered using x - size/2 math.
     * UPDATED: Uses Avatar Image if available.
     */
    private void drawGlossyToken(Graphics2D g2, int cx, int cy, int size, Color color, boolean primeActive) {
        // Player token should be 80% of node size (which is 15px), so ~12px
        int tokenSize = Math.max(10, (int)(size * 0.8));

        // PERFECT CENTERING: x = nodeX - (playerSize / 2)
        int drawX = cx - tokenSize / 2;
        int drawY = cy - tokenSize / 2;

        // Outer glow if prime active
        if (primeActive) {
            float pulse = (float)(0.5 + 0.5 * Math.sin(animFrame * 0.15));
            int glowSize = tokenSize + 10;
            g2.setColor(new Color(0xFF, 0xFF, 0x00, (int)(120 * pulse)));
            g2.fillOval(cx - glowSize/2, cy - glowSize/2, glowSize, glowSize);
        }

        // Find player with this color to get avatar
        Image avatar = null;
        for(Player p : allPlayers) {
            if(p.color.equals(color)) {
                avatar = p.avatar;
                break;
            }
        }

        if (avatar != null) {
            // Draw Avatar Icon
            g2.setClip(new Ellipse2D.Float(drawX, drawY, tokenSize, tokenSize));
            g2.drawImage(avatar, drawX, drawY, tokenSize, tokenSize, this);
            g2.setClip(null);

            // Border
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(drawX, drawY, tokenSize, tokenSize);
        } else {
            // Fallback to Glossy Sphere
            Color lighter = new Color(
                    Math.min(255, color.getRed() + 80),
                    Math.min(255, color.getGreen() + 80),
                    Math.min(255, color.getBlue() + 80)
            );
            Color darker = new Color(
                    Math.max(0, color.getRed() - 40),
                    Math.max(0, color.getGreen() - 40),
                    Math.max(0, color.getBlue() - 40)
            );

            GradientPaint gp = new GradientPaint(
                    drawX, drawY, lighter,
                    drawX + tokenSize, drawY + tokenSize, darker
            );
            g2.setPaint(gp);
            g2.fillOval(drawX, drawY, tokenSize, tokenSize);

            // Highlight (glass effect)
            g2.setColor(new Color(255, 255, 255, 140));
            int hlSize = tokenSize / 3;
            g2.fillOval(cx - hlSize/2, cy - tokenSize/3, hlSize, hlSize/2);

            // White border
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(drawX, drawY, tokenSize, tokenSize);
        }
    }

    private void drawItem(Graphics2D g2, int x, int y, int size) {
        // Gold Coin
        g2.setColor(new Color(255, 215, 0));
        g2.fillOval(x - size/2, y - size/2, size, size);
        g2.setColor(new Color(218, 165, 32));
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(x - size/2, y - size/2, size, size);
        g2.setColor(new Color(255, 255, 200));
        g2.fillOval(x - size/4, y - size/4, size/3, size/3); // Shine
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
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
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.draw(star);
    }

    private void drawOverlays(Graphics2D g2) {
        if (isDeterminingOrder) {
            drawOverlayBg(g2);
            int cx = getWidth()/2, cy = getHeight()/2;

            // Pixel Card Style
            g2.setColor(Color.WHITE);
            g2.fillRect(cx - 150, cy - 120, 300, 240);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(4));
            g2.drawRect(cx - 150, cy - 120, 300, 240);

            g2.setColor(Color.BLACK);
            g2.setFont(GameStyle.FONT_TITLE);
            FontMetrics fm = g2.getFontMetrics();
            String title = "WHO GOES FIRST?";
            g2.drawString(title, cx - fm.stringWidth(title)/2, cy - 80);

            int startY = cy - 40;
            for (int i = 0; i < allPlayers.size(); i++) {
                if (i == highlightedOrderIdx) {
                    g2.setColor(new Color(52, 152, 219, 50));
                    g2.fillRect(cx - 130, startY + (i*35) - 25, 260, 30);
                    g2.setColor(new Color(52, 152, 219));
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRect(cx - 130, startY + (i*35) - 25, 260, 30);
                }
                g2.setColor(allPlayers.get(i).color);
                g2.fillRect(cx - 120, startY + (i*35) - 20, 20, 20);

                g2.setColor(Color.BLACK);
                g2.setFont(GameStyle.FONT_BODY);
                g2.drawString(allPlayers.get(i).name, cx - 90, startY + (i*35) - 5);
            }
        } else if ((isRollingDice || isRollingChaos) && turnQueue != null && !turnQueue.isEmpty()) {
            int cx = getWidth()/2, cy = getHeight()/2;

            if (isRollingDice) {
                // NO OVERLAY BACKGROUND for Dice - Let it fly over the map!

                // Draw Physics Dice
                if (!activeDice.isEmpty()) {
                    for (RollingDice d : activeDice) {
                        // Shadow
                        g2.setColor(new Color(0, 0, 0, 50));
                        g2.fillOval((int)d.x + 10, (int)d.y + d.size + 5, d.size - 20, 10);

                        AffineTransform old = g2.getTransform();
                        g2.translate(d.x + d.size/2, d.y + d.size/2);
                        g2.rotate(d.angle);
                        g2.translate(-d.size/2, -d.size/2); // Center pivot

                        drawDiceFace(g2, 0, 0, d.size, d.currentValue);

                        g2.setTransform(old);
                    }
                } else {
                    // Fallback
                    drawDiceFace(g2, cx - 60, cy - 70, 120, displayDiceVal);
                }

                // Subtle Text at Top
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRoundRect(cx - 100, 50, 200, 30, 20, 20);
                g2.setColor(Color.WHITE);
                g2.setFont(GameStyle.FONT_BODY);
                String txt = turnQueue.peek().name + " Rolling...";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(txt, cx - fm.stringWidth(txt)/2, 70);

            } else if (isRollingChaos) {
                // Keep Overlay for Slot Machine
                drawOverlayBg(g2);

                // Pixel Box
                g2.setColor(new Color(255, 255, 255, 240));
                g2.fillRect(cx - 120, cy - 120, 240, 240);
                g2.setStroke(new BasicStroke(5));
                g2.setColor(turnQueue.peek().color);
                g2.drawRect(cx - 120, cy - 120, 240, 240);

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
        // Try to draw sprite, fallback to detailed icon
        // Since we can't load external images easily without files, we draw "Pixel Art" procedurally

        int size = diameter;

        // Shadow
        g2.setColor(new Color(0,0,0,80));
        g2.fillOval(cx - size/2 + 2, cy - size/2 + 4, size, size/2);

        // Body / Armor
        g2.setColor(c);
        g2.fillRect(cx - size/3, cy - size/3, size*2/3, size*2/3);

        // Helmet / Head
        g2.setColor(c.brighter());
        g2.fillRect(cx - size/4, cy - size/2, size/2, size/2);

        // Weapon (Sword/Staff)
        g2.setColor(Color.LIGHT_GRAY);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(cx + size/3, cy - size/2, cx + size/3, cy + size/3); // Staff/Sword handle

        // Outline
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1));
        g2.drawRect(cx - size/3, cy - size/3, size*2/3, size*2/3);
        g2.drawRect(cx - size/4, cy - size/2, size/2, size/2);
    }

    private void drawDiceFace(Graphics2D g2, int x, int y, int size, int val) {
        // Try to draw image asset first
        if (val >= 1 && val <= 6 && diceImages[val-1] != null) {
            g2.drawImage(diceImages[val-1], x, y, size, size, this);
            return;
        }

        // DIGIMON STYLE DIGITAL DICE (Fallback)

        // 1. Outer Glow (Holographic Blue)
        g2.setColor(new Color(0, 255, 255, 100));
        g2.fillRoundRect(x - 5, y - 5, size + 10, size + 10, 20, 20);

        // 2. Main Cube Body (Dark Tech Glass)
        GradientPaint gp = new GradientPaint(
                x, y, new Color(0, 20, 40, 220),
                x + size, y + size, new Color(0, 100, 150, 220)
        );
        g2.setPaint(gp);
        g2.fillRoundRect(x, y, size, size, 15, 15);

        // 3. Tech Grid / Circuit Lines
        g2.setColor(new Color(0, 255, 255, 50));
        g2.setStroke(new BasicStroke(1));
        for(int i=1; i<4; i++) {
            g2.drawLine(x, y + i*(size/4), x+size, y + i*(size/4)); // Horz
            g2.drawLine(x + i*(size/4), y, x + i*(size/4), y+size); // Vert
        }

        // 4. Border (Neon Cyan)
        g2.setColor(new Color(0, 255, 255));
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x, y, size, size, 15, 15);

        // 5. Corner Accents (Tech Brackets)
        g2.setStroke(new BasicStroke(4));
        int d = 15;
        g2.drawLine(x, y, x+d, y); g2.drawLine(x, y, x, y+d); // TL
        g2.drawLine(x+size-d, y, x+size, y); g2.drawLine(x+size, y, x+size, y+d); // TR
        g2.drawLine(x, y+size-d, x, y+size); g2.drawLine(x, y+size, x+d, y+size); // BL
        g2.drawLine(x+size-d, y+size, x+size, y+size); g2.drawLine(x+size, y+size-d, x+size, y+size); // BR

        // 6. The Number (Digital Font Style)
        g2.setColor(new Color(255, 255, 255));
        g2.setFont(new Font("Consolas", Font.BOLD, size / 2));
        FontMetrics fm = g2.getFontMetrics();
        String sVal = String.valueOf(val);

        // Text Glow
        int tx = x + (size - fm.stringWidth(sVal)) / 2;
        int ty = y + (size - fm.getHeight()) / 2 + fm.getAscent();

        g2.setColor(new Color(0, 255, 255, 150)); // Cyan Glow
        g2.drawString(sVal, tx+2, ty+2);
        g2.setColor(Color.WHITE); // Core
        g2.drawString(sVal, tx, ty);

        // 7. "DIGI-CODE" Label
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.setColor(new Color(0, 255, 255));
        String code = "010101";
        g2.drawString(code, x + (size - g2.getFontMetrics().stringWidth(code))/2, y + size - 8);
    }

    // Removed old fillDot method as it is no longer used
    private void drawSlotItem(Graphics2D g2, int x, int y, String t, Color c){
        g2.setColor(c);
        g2.fillRect(x-90, y-35, 180, 70);
        g2.setColor(Color.WHITE);
        g2.setFont(GameStyle.FONT_SLOT);
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(t, x-fm.stringWidth(t)/2, y+10);
    }
}
