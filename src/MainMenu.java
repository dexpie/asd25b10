import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenu extends JFrame {
    private Image bgImage;

    public MainMenu() {
        setTitle("Digimon Adventure: Digital Journey");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        
        bgImage = DigimonThemeFactory.loadImageOrPlaceholder("resources/menu_bg.png", 800, 600).getImage();

        
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                }
                
                g.setColor(new Color(0, 0, 0, 100));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 20, 0);

        
        JLabel title = new JLabel("DIGIMON ADVENTURE");
        title.setFont(new Font("Impact", Font.BOLD, 60));
        title.setForeground(Color.YELLOW);
        
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 50, 0));
        mainPanel.add(title, gbc);

        
        gbc.gridy++;
        mainPanel.add(createMenuButton("LOCAL GAME", () -> startGame(false)), gbc);

        gbc.gridy++;
        mainPanel.add(createMenuButton("ONLINE MULTIPLAYER", () -> startGame(true)), gbc);

        gbc.gridy++;
        mainPanel.add(createMenuButton("EXIT", () -> System.exit(0)), gbc);

        add(mainPanel);
    }

    private JButton createMenuButton(String text, Runnable action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                
                if (getModel().isPressed()) {
                    g2.setColor(new Color(0, 80, 160));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(0, 100, 200));
                } else {
                    g2.setColor(new Color(0, 50, 100));
                }
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(300, 60));
        btn.setFont(new Font("Arial", Font.BOLD, 24));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.CYAN, 2));
        btn.setContentAreaFilled(false);
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBorder(BorderFactory.createLineBorder(Color.CYAN, 2));
            }
        });

        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void startGame(boolean isOnline) {
        Player.DigimonPartner selectedPartner = null;

        
        if (isOnline) {
            CharacterSelectDialog charSelect = new CharacterSelectDialog(this);
            charSelect.setVisible(true);
            if (!charSelect.isConfirmed()) return; 
            selectedPartner = charSelect.getSelectedPartner();
        }

        this.dispose(); 
        
        final Player.DigimonPartner p = selectedPartner;
        
        SwingUtilities.invokeLater(() -> {
            new SnakeLadderGame(isOnline, p).setVisible(true);
        });
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }
}
