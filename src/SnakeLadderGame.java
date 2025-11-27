import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SnakeLadderGame extends JFrame {
    public SnakeLadderGame() {
        SoundManager.init(); // Initialize Sound System
        SoundManager.playLoop(SoundManager.BGM); // Start Background Music
        setTitle("Snake Ladder: Star Queue Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        String input = JOptionPane.showInputDialog(this, "How many players? (2-4)", "2");
        if(input == null) System.exit(0);

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
        if (result != JOptionPane.OK_OPTION) System.exit(0);

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
        btnPanel.setBackground(GameStyle.BACKGROUND_END); // Match background
        
        JButton rollBtn = new JButton("ROLL & GACHA");
        rollBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        rollBtn.setBackground(GameStyle.STAR_COLOR); // Gold accent
        rollBtn.setForeground(new Color(50, 50, 50)); // Dark text for contrast
        rollBtn.setPreferredSize(new Dimension(220, 55));
        rollBtn.setFocusPainted(false);
        rollBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        // Hover Effect
        rollBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                rollBtn.setBackground(GameStyle.STAR_COLOR.brighter());
                rollBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                rollBtn.setBackground(GameStyle.STAR_COLOR);
            }
        });

        rollBtn.addActionListener(e -> controller.startTurn());

        JButton statsBtn = new JButton("STATS");
        statsBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statsBtn.setBackground(new Color(52, 152, 219));
        statsBtn.setForeground(Color.WHITE);
        statsBtn.setPreferredSize(new Dimension(100, 55));
        statsBtn.setFocusPainted(false);
        statsBtn.addActionListener(e -> showStatistics());

        btnPanel.add(rollBtn);
        btnPanel.add(statsBtn);
        add(btnPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        SwingUtilities.invokeLater(controller::startOrderRoulette);
    }

    // Statistics
    public static Map<String, Integer> playerWins = new HashMap<>();
    public static Map<String, Integer> playerHighScores = new HashMap<>();

    public static void updateWinStats(String winnerName) {
        playerWins.put(winnerName, playerWins.getOrDefault(winnerName, 0) + 1);
    }

    public static void updateScoreStats(String playerName, int score) {
        // Update High Score if higher
        if (score > playerHighScores.getOrDefault(playerName, 0)) {
            playerHighScores.put(playerName, score);
        }
    }

    public static void showStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== TOP WINNERS ===\n");
        playerWins.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(3)
            .forEach(e -> sb.append(e.getKey()).append(": ").append(e.getValue()).append(" Wins\n"));
            
        sb.append("\n=== TOP SCORES ===\n");
        playerHighScores.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(3)
            .forEach(e -> sb.append(e.getKey()).append(": ").append(e.getValue()).append(" Points\n"));
            
        JOptionPane.showMessageDialog(null, sb.toString(), "Game Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new SnakeLadderGame().setVisible(true));
    }
}
