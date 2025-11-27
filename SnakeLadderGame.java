import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SnakeLadderGame extends JFrame {
    public SnakeLadderGame() {
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
        btnPanel.setBackground(GameStyle.PANEL_BG_END); // Match background
        
        JButton rollBtn = new JButton("ROLL & GACHA");
        rollBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        rollBtn.setBackground(new Color(231, 76, 60)); // Red accent
        rollBtn.setForeground(Color.WHITE);
        rollBtn.setPreferredSize(new Dimension(220, 55));
        rollBtn.setFocusPainted(false);
        rollBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        // Hover Effect
        rollBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                rollBtn.setBackground(new Color(192, 57, 43));
                rollBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                rollBtn.setBackground(new Color(231, 76, 60));
            }
        });

        rollBtn.addActionListener(e -> controller.startTurn());

        btnPanel.add(rollBtn);
        add(btnPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        SwingUtilities.invokeLater(controller::startOrderRoulette);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new SnakeLadderGame().setVisible(true));
    }
}
