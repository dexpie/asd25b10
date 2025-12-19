import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class SnakeLadderGame extends JFrame {
    private static JTextArea logArea;

    public static void log(String msg) {
        if (logArea != null) {
            SwingUtilities.invokeLater(() -> {
                logArea.append("> " + msg + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            });
        }
    }

    public SnakeLadderGame(boolean isOnlineMode, Player.DigimonPartner localPartner) {
        SoundManager.init();
        SoundManager.playLoop(SoundManager.BGM);
        setTitle("Digimon Adventure - Snake & Ladder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        ImageIcon appIcon = DigimonThemeFactory.loadScaledImage(DigimonThemeFactory.ICON_PATH, 64, 64);
        if (appIcon != null) {
            setIconImage(appIcon.getImage());
        }
        
        boolean isOnline = isOnlineMode;
        NetworkManager net = new NetworkManager();
        List<DigimonSetupUI.DigiDestinedData> connectedPlayers = null;
        
        if (isOnline) {
            OnlineSetupUI onlineUI = new OnlineSetupUI(localPartner);
            isOnline = onlineUI.showModeSelection(this);
            net = onlineUI.getNetworkManager();
            connectedPlayers = onlineUI.getConnectedPlayers();
        }
        
        List<DigimonSetupUI.DigiDestinedData> setupData = null;
        
        if (isOnline && !net.isHost()) {
            
            JDialog waitingDialog = new JDialog(this, "Connecting...", true);
            waitingDialog.setLayout(new BorderLayout());
            JLabel msg = new JLabel("Waiting for Host to start game...", SwingConstants.CENTER);
            msg.setFont(new Font("Arial", Font.BOLD, 16));
            msg.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            waitingDialog.add(msg, BorderLayout.CENTER);
            waitingDialog.setSize(400, 150);
            waitingDialog.setLocationRelativeTo(null);
            waitingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            
            final List<DigimonSetupUI.DigiDestinedData>[] receivedData = new List[1];
            net.setOnMessageReceived(message -> {
                if (message.startsWith("SETUP_DATA:")) {
                    receivedData[0] = parseSetupData(message.substring(11));
                    waitingDialog.dispose();
                }
            });
            
            net.sendMessage("REQUEST_SETUP " + localPartner.name());
            waitingDialog.setVisible(true);
            
            setupData = receivedData[0];
            if (setupData == null) {
                JOptionPane.showMessageDialog(this, "Failed to get game data from host.");
                System.exit(0);
            }
            
        } else {
            DigimonSetupUI setupUI = new DigimonSetupUI();
            setupData = setupUI.showSetupWizard(null, localPartner, connectedPlayers);
        }

        if (setupData == null) {
            System.exit(0);
        }

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < setupData.size(); i++) {
            DigimonSetupUI.DigiDestinedData data = setupData.get(i);
            Player p = new Player(i, data.name, data.partnerDigimon, GameStyle.PLAYER_COLORS[i]);
            p.setBot(data.isBot);
            players.add(p);
        }

        BoardGraph board;
        final String[] mapDataFromHost = new String[1];

        if (isOnline && !net.isHost()) {
            // CLIENT: tunggu SETUP_DATA dan MAP_DATA dari host
            final Object mapLock = new Object();
            final Object setupLock = new Object();
            final boolean[] gotSetup = {false};
            final boolean[] gotMap = {false};
            final List<DigimonSetupUI.DigiDestinedData>[] receivedData = new List[1];
            net.setOnMessageReceived(message -> {
                if (message.startsWith("SETUP_DATA:")) {
                    receivedData[0] = parseSetupData(message.substring(11));
                    synchronized (setupLock) { gotSetup[0] = true; setupLock.notifyAll(); }
                } else if (message.startsWith("MAP_DATA:")) {
                    mapDataFromHost[0] = message.substring(9);
                    synchronized (mapLock) { gotMap[0] = true; mapLock.notifyAll(); }
                }
            });
            // Request data (optional, host bisa push langsung)
            net.sendMessage("REQUEST_SETUP " + localPartner.name());
            net.sendMessage("REQUEST_MAP");
            // Tunggu sampai SETUP_DATA diterima
            synchronized (setupLock) {
                while (!gotSetup[0]) {
                    try { setupLock.wait(100); } catch (InterruptedException e) {}
                }
            }
            setupData = receivedData[0];
            // Tunggu sampai MAP_DATA diterima
            synchronized (mapLock) {
                while (!gotMap[0]) {
                    try { mapLock.wait(100); } catch (InterruptedException e) {}
                }
            }
            board = new BoardGraph();
            board.deserializeMap(mapDataFromHost[0]);
        } else {
            board = new BoardGraph();
        }

        GamePanel panel = new GamePanel(board, players);
        GameController controller = new GameController(board, panel, players);

        if (isOnline) {
            controller.setNetworkManager(net);
            if (net.isHost()) {
                setTitle(getTitle() + " [HOST] - IP: " + net.getLocalIP());
                
                
                
                StringBuilder sb = new StringBuilder("SETUP_DATA:");
                for (DigimonSetupUI.DigiDestinedData d : setupData) {
                    sb.append(d.name).append(",").append(d.partnerDigimon).append(";");
                }
                String setupStr = sb.toString();
                
                
                
                // Kirim SETUP_DATA dan MAP_DATA ke semua client
                String mapStr = board.serializeMap();
                for (java.net.Socket s : net.getConnectedSockets()) {
                    try {
                        java.io.PrintWriter out = new java.io.PrintWriter(s.getOutputStream(), true);
                        out.println(setupStr);
                        out.println("MAP_DATA:" + mapStr);
                    } catch (Exception e) { e.printStackTrace(); }
                }

                net.setOnClientConnected(socket -> {
                    try {
                        java.io.PrintWriter out = new java.io.PrintWriter(socket.getOutputStream(), true);
                        out.println(setupStr);
                        out.println("MAP_DATA:" + mapStr);
                    } catch (Exception e) { e.printStackTrace(); }
                });

                // Auto-launch a local client JVM for demo on the same machine
                try {
                    String javaBin = System.getProperty("java.home") + java.io.File.separator + "bin" + java.io.File.separator + "java";
                    String classpath = System.getProperty("java.class.path");
                    ProcessBuilder pb = new ProcessBuilder(
                        javaBin, "-cp", classpath, "-DautoClient=1", "-DhostIP=127.0.0.1", "SnakeLadderGame"
                    );
                    pb.start();
                } catch (Exception ex) {
                    System.out.println("Failed to auto-launch local client: " + ex.getMessage());
                }
                
            } else {
                setTitle(getTitle() + " [CLIENT]");
            }
        }

        add(panel, BorderLayout.CENTER);

        
        
        JPanel rightPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                g.setColor(new Color(10, 10, 30));
                g.fillRect(0, 0, getWidth(), getHeight());
                
                
                g.setColor(new Color(0, 50, 100));
                for (int i = 0; i < getHeight(); i += 20) {
                    g.drawLine(0, i, getWidth(), i);
                }
                
                
                g.setColor(new Color(0, 255, 255));
                g.drawRect(0, 0, getWidth()-1, getHeight()-1);
            }
        };
        rightPanel.setPreferredSize(new Dimension(280, 0));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        
        JLabel titleLbl = new JLabel("SERVER STATUS");
        titleLbl.setFont(new Font("Consolas", Font.BOLD, 24));
        titleLbl.setForeground(new Color(0, 255, 0)); 
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(titleLbl);
        rightPanel.add(Box.createVerticalStrut(20));

        
        JPanel playerListPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int y = 10;
                for (Player p : players) {
                    
                        
                    g2.setColor(p.color);
                    g2.fillRect(10, y, 15, 15);
                    g2.setColor(Color.WHITE);
                    g2.drawRect(10, y, 15, 15);
                    
                    
                    
                    g2.setFont(new Font("Consolas", Font.BOLD, 14));
                    g2.drawString(p.name, 35, y + 12);
                    g2.setFont(new Font("Consolas", Font.PLAIN, 12));
                    g2.setColor(new Color(200, 200, 200));
                    g2.drawString("[" + p.partnerDigimon + "]", 35, y + 28);
                    
                    
                    y += 45;
                }
            }
        };
        playerListPanel.setOpaque(false); 
        playerListPanel.setPreferredSize(new Dimension(250, players.size() * 50));
        playerListPanel.setMaximumSize(new Dimension(250, players.size() * 50));
        playerListPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        rightPanel.add(playerListPanel);
        rightPanel.add(Box.createVerticalStrut(30));

        
        
        JLabel lbTitle = new JLabel("SYSTEM LOGS");
        lbTitle.setFont(new Font("Consolas", Font.BOLD, 16));
        lbTitle.setForeground(new Color(0, 255, 255)); 
        lbTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(lbTitle);
        rightPanel.add(Box.createVerticalStrut(10));

        
        
        JTextArea leaderboardArea = new JTextArea();
        leaderboardArea.setEditable(false);
        leaderboardArea.setOpaque(false); 
        leaderboardArea.setForeground(new Color(0, 255, 0)); 
        leaderboardArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        leaderboardArea.setMargin(new Insets(5, 10, 5, 10));
        leaderboardArea.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 100, 0)), "LIVE STATS", TitledBorder.LEFT, TitledBorder.TOP, new Font("Consolas", Font.BOLD, 10), Color.GRAY));
        
        rightPanel.add(leaderboardArea);
        rightPanel.add(Box.createVerticalStrut(10));

        
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(10, 10, 20));
        logArea.setForeground(new Color(200, 200, 200));
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(250, 200));
        logScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 100, 100)), "EVENT LOG", TitledBorder.LEFT, TitledBorder.TOP, new Font("Consolas", Font.BOLD, 10), Color.CYAN));
        logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        rightPanel.add(logScroll);

        
        
        new Timer(500, e -> {
            
            playerListPanel.repaint();
            
            StringBuilder sb = new StringBuilder();
            
            
            players.stream()
                .sorted((p1, p2) -> {
                    if (p2.getScore() != p1.getScore()) return Integer.compare(p2.getScore(), p1.getScore());
                    return Integer.compare(p2.position, p1.position);
                })
                .forEach(p -> {
                    sb.append(String.format(" [%s] %s\n", p.partnerDigimon, p.name));
                    sb.append(String.format("  > LOC: Tile %-3d | PTS: %d\n", p.position, p.getScore()));
                    sb.append(" --------------------------\n");
                });
            
            leaderboardArea.setText(sb.toString());
        }).start();

        add(rightPanel, BorderLayout.EAST);

        
    
        JPanel btnPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(20, 20, 20));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(0, 255, 255));
                g.fillRect(0, 0, getWidth(), 2);
            }
        };
        btnPanel.setPreferredSize(new Dimension(600, 80));
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 15));

        DigiButton rollBtn = new DigiButton("EXECUTE ROLL");
        rollBtn.setPreferredSize(new Dimension(240, 50));
        rollBtn.addActionListener(e -> controller.startTurn());
        btnPanel.add(rollBtn);

        // --- Volume Control ---
        JPanel volumePanel = new JPanel();
        volumePanel.setOpaque(false);
        volumePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JLabel volumeLabel = new JLabel("Volume");
        volumeLabel.setForeground(new Color(0,255,255));
        JSlider volumeSlider = new JSlider(0, 100, (int)(SoundManager.getVolume()*100));
        volumeSlider.setPreferredSize(new Dimension(120, 30));
        volumeSlider.addChangeListener(e -> {
            float v = volumeSlider.getValue() / 100f;
            SoundManager.setVolume(v);
        });

        JButton muteBtn = new JButton() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                setText(SoundManager.isMuted() ? "Unmute" : "Mute");
            }
        };
        muteBtn.setPreferredSize(new Dimension(70, 30));
        muteBtn.addActionListener(e -> {
            SoundManager.setMuted(!SoundManager.isMuted());
            muteBtn.repaint();
        });

        volumePanel.add(volumeLabel);
        volumePanel.add(volumeSlider);
        volumePanel.add(muteBtn);
        btnPanel.add(volumePanel);

        add(btnPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        SwingUtilities.invokeLater(controller::startOrderRoulette);
    }

    /**
     * Overloaded constructor used for auto-launching a local client window.
     * If autoConnect is true this instance will try to connect immediately to the given hostIP.
     */
    public SnakeLadderGame(boolean isOnlineMode, Player.DigimonPartner localPartner, boolean autoConnect, String hostIP) {
        this(isOnlineMode, localPartner);
        if (autoConnect) {
            // Try to connect in background so UI remains responsive
            new Thread(() -> {
                try {
                    // small delay to allow host server to fully start
                    Thread.sleep(300);
                    NetworkManager net = new NetworkManager();
                    try {
                        net.connectToHost(hostIP, 12345);
                    } catch (Exception e) {
                        System.out.println("Auto-client failed to connect: " + e.getMessage());
                    }
                    // request setup from host using this net instance
                    try {
                        net.sendMessage("REQUEST_SETUP " + localPartner.name());
                    } catch (Exception ex) {
                        System.out.println("Failed to request setup: " + ex.getMessage());
                    }
                } catch (InterruptedException ignored) {}
            }).start();
        }
    }

    
    public static Map<String, Integer> playerWins = new HashMap<>();
    public static Map<String, Integer> playerHighScores = new HashMap<>();

    public static void updateWinStats(String winnerName) {
        playerWins.put(winnerName, playerWins.getOrDefault(winnerName, 0) + 1);
    }

    public static void updateScoreStats(String playerName, int score) {
        
        if (score > playerHighScores.getOrDefault(playerName, 0)) {
            playerHighScores.put(playerName, score);
        }
    }

    

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        // Auto-client mode (spawned by host for demo)
        String auto = System.getProperty("autoClient");
        if (auto != null && !auto.isEmpty()) {
            String hostIP = System.getProperty("hostIP", "127.0.0.1");
            SwingUtilities.invokeLater(() -> runAutoClient(hostIP));
            return;
        }

        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }

    private static void runAutoClient(String hostIP) {
        try { SoundManager.init(); SoundManager.playLoop(SoundManager.BGM); } catch (Exception ignored) {}

        NetworkManager net = new NetworkManager();
        try {
            net.connectToHost(hostIP, 12345);
        } catch (Exception e) {
            System.out.println("Auto-client: failed to connect to host " + hostIP + " : " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Auto-client failed to connect to host " + hostIP + "\n" + e.getMessage());
            return;
        }

        final StringBuilder setupBuf = new StringBuilder();
        final StringBuilder mapBuf = new StringBuilder();
        final Object lock = new Object();

        net.setOnMessageReceived(msg -> {
            if (msg.startsWith("SETUP_DATA:")) {
                setupBuf.append(msg.substring(11));
                synchronized (lock) { lock.notifyAll(); }
            } else if (msg.startsWith("MAP_DATA:")) {
                mapBuf.append(msg.substring(9));
                synchronized (lock) { lock.notifyAll(); }
            }
        });

        // Request data in case host expects it
        net.sendMessage("REQUEST_SETUP AutoClient");
        net.sendMessage("REQUEST_MAP");

        // Wait for both setup and map
        long start = System.currentTimeMillis();
        while ((setupBuf.length() == 0 || mapBuf.length() == 0) && System.currentTimeMillis() - start < 10000) {
            synchronized (lock) { try { lock.wait(200); } catch (InterruptedException ignored) {} }
        }

        if (setupBuf.length() == 0) {
            System.out.println("Auto-client: did not receive setup data in time.");
            JOptionPane.showMessageDialog(null, "Auto-client: did not receive setup data from host.");
            return;
        }

        // Parse setup data and build players
        List<Player> players = new ArrayList<>();
        String[] parts = setupBuf.toString().split(";");
        int idx = 0;
        for (String p : parts) {
            if (p == null) continue;
            p = p.trim();
            if (p.isEmpty()) continue;
            String[] f = p.split(",");
            if (f.length >= 2) {
                String name = f[0].trim();
                String partnerStr = f[1].trim();
                Player.DigimonPartner partner = parsePartnerSafe(partnerStr);
                Player pl = new Player(idx, name, partner.name(), GameStyle.PLAYER_COLORS[idx % GameStyle.PLAYER_COLORS.length]);
                players.add(pl);
                idx++;
            }
        }

        // Create board and apply map data
        BoardGraph board = new BoardGraph();
        if (mapBuf.length() > 0) board.deserializeMap(mapBuf.toString());

        // Build minimal client UI
        GamePanel panel = new GamePanel(board, players);
        GameController controller = new GameController(board, panel, players);
        controller.setNetworkManager(net);

        JFrame f = new JFrame("Digimon Adventure - Snake & Ladder [CLIENT]");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLayout(new BorderLayout());
        f.add(panel, BorderLayout.CENTER);

        JPanel btn = new JPanel();
        DigiButton roll = new DigiButton("EXECUTE ROLL");
        roll.addActionListener(e -> controller.startTurn());
        btn.add(roll);
        f.add(btn, BorderLayout.SOUTH);

        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);

        // Periodically refresh panel to reflect host updates
        new Timer(250, e -> panel.refresh()).start();
    }

    private static Player.DigimonPartner parsePartnerSafe(String s) {
        if (s == null) return Player.DigimonPartner.AGUMON;
        String raw = s.trim();
        try {
            return Player.DigimonPartner.valueOf(raw);
        } catch (Exception e) {
            // try case-insensitive match
            for (Player.DigimonPartner p : Player.DigimonPartner.values()) {
                if (p.name().equalsIgnoreCase(raw)) return p;
            }
            // try stripping non-letters
            String cleaned = raw.replaceAll("[^A-Za-z_]", "");
            try {
                return Player.DigimonPartner.valueOf(cleaned);
            } catch (Exception ex) {
                System.out.println("Warning: unknown partner '" + s + "', defaulting to AGUMON");
                return Player.DigimonPartner.AGUMON;
            }
        }
    }

    private List<DigimonSetupUI.DigiDestinedData> parseSetupData(String data) {
        List<DigimonSetupUI.DigiDestinedData> list = new ArrayList<>();
        String[] parts = data.split(";");
        for (String p : parts) {
            if (p.trim().isEmpty()) continue;
            String[] fields = p.split(",");
            if (fields.length >= 2) {
                list.add(new DigimonSetupUI.DigiDestinedData(fields[0], fields[1]));
            }
        }
        return list;
    }
}
