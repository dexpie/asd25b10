import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * DigimonThemeFactory - Helper class for Colors, Fonts, and Image Loading
 * Provides consistent theming across all Digimon Adventure game UI components.
 */
public class DigimonThemeFactory {

    // ==================== COLOR PALETTE ====================
    public static final Color PRIMARY_ORANGE = new Color(0xFF, 0x99, 0x00);       // Digivice Orange #FF9900
    public static final Color SECONDARY_BLUE = new Color(0x44, 0x88, 0xFF);       // Lighter Digital Blue for Dark Mode
    public static final Color BACKGROUND_DARK = new Color(0x0A, 0x0A, 0x1E);      // Deep Cyber Blue/Black
    public static final Color BACKGROUND_WHITE = BACKGROUND_DARK;                 // Replaced White with Dark for Global Theme
    public static final Color CANCEL_RED = new Color(0xC8, 0x32, 0x32);           // Cancel Red
    public static final Color TEXT_LIGHT = new Color(0x00, 0xFF, 0xFF);           // Cyan Text
    public static final Color TEXT_DARK = TEXT_LIGHT;                             // Replaced Dark Text with Light for Dark Mode
    public static final Color HOVER_ORANGE = new Color(0xFF, 0xAA, 0x33);         // Lighter Orange for hover
    public static final Color PRESSED_ORANGE = new Color(0xCC, 0x77, 0x00);       // Darker Orange for pressed
    public static final Color BORDER_BLUE = new Color(0x00, 0xDD, 0xFF);          // Neon Cyan Border
    
    // ==================== PORTAL / NODE COLORS ====================
    public static final Color PORTAL_GLOW = new Color(0x00, 0xFF, 0xAA);          // Neon Green/Cyan for portals
    public static final Color PORTAL_GOLD = new Color(0xFF, 0xD7, 0x00);          // Gold for portal vortex
    public static final Color NODE_CYAN = new Color(0x00, 0xDD, 0xFF, 180);       // Hexagon node border
    public static final Color NODE_FILL = new Color(0x00, 0xAA, 0xCC, 80);        // Hexagon node fill
    
    // ==================== PLAYER TOKEN COLORS ====================
    public static final Color[] PLAYER_COLORS = {
        new Color(0xFF, 0x44, 0x44), // Red
        new Color(0x44, 0x88, 0xFF), // Blue
        new Color(0xFF, 0xDD, 0x00), // Gold
        new Color(0x44, 0xFF, 0x66)  // Green
    };

    // ==================== FONTS ====================
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_BUTTON_LARGE = new Font("SansSerif", Font.BOLD, 16);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);

    // ==================== ASSET PATHS ====================
    public static final String ICON_PATH = "resources/digivice_icon.png";
    public static final String LOGO_PATH = "resources/digimon_logo.png";
    public static final String MAP_PATH = "resources/map.png";

    // ==================== IMAGE LOADING ====================

    public static void styleButton(JButton btn, DigiButton.ButtonStyle style) {
        // Basic styling for standard JButtons to match DigiButton look
        btn.setFocusPainted(false);
        btn.setFont(FONT_BUTTON);
        btn.setForeground(Color.BLACK);
        
        Color bg = PRIMARY_ORANGE;
        if (style == DigiButton.ButtonStyle.SECONDARY) bg = Color.LIGHT_GRAY;
        else if (style == DigiButton.ButtonStyle.DANGER) bg = CANCEL_RED;
        
        btn.setBackground(bg);
        btn.setBorder(BorderFactory.createLineBorder(BORDER_BLUE, 1));
    }

    public static ImageIcon loadScaledImage(String path, int targetWidth, int targetHeight) {
        try {
            BufferedImage img = null;
            File file = new File(path);

            if (file.exists()) {
                img = ImageIO.read(file);
            } else {
                // Try loading from classpath
                java.net.URL url = DigimonThemeFactory.class.getClassLoader().getResource(path);
                if (url != null) {
                    img = ImageIO.read(url);
                }
            }

            if (img != null) {
                int originalWidth = img.getWidth();
                int originalHeight = img.getHeight();

                // Calculate dimensions maintaining aspect ratio
                double ratio = Math.min(
                        (double) targetWidth / originalWidth,
                        (double) targetHeight / originalHeight
                );
                int newWidth = (int) (originalWidth * ratio);
                int newHeight = (int) (originalHeight * ratio);

                Image scaled = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load image at " + path + " - " + e.getMessage());
        }
        return null;
    }

    public static ImageIcon createPlaceholder(int width, int height, Color color) {
        BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = placeholder.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillRoundRect(0, 0, width, height, 10, 10);
        g2.setColor(Color.WHITE);
        g2.setFont(FONT_SMALL);
        FontMetrics fm = g2.getFontMetrics();
        String text = "Image N/A";
        int textX = (width - fm.stringWidth(text)) / 2;
        int textY = (height + fm.getAscent()) / 2 - 2;
        g2.drawString(text, textX, textY);
        g2.dispose();
        return new ImageIcon(placeholder);
    }

    // Loads image with fallback to placeholder if loading fails.
    
    public static ImageIcon loadImageOrPlaceholder(String path, int width, int height) {
        ImageIcon icon = loadScaledImage(path, width, height);
        if (icon == null) {
            return createPlaceholder(width, height, SECONDARY_BLUE);
        }
        return icon;
    }

    // ==================== UI STYLING HELPERS ====================

    // Styles a JComboBox with the Digimon theme.
  
    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(BACKGROUND_WHITE);
        comboBox.setForeground(SECONDARY_BLUE);
        comboBox.setFont(FONT_BODY_BOLD);
        comboBox.setBorder(BorderFactory.createLineBorder(SECONDARY_BLUE, 1));
    }

    // Styles a JTextField with the Digimon theme.
    
    public static void styleTextField(JTextField textField) {
        textField.setBackground(BACKGROUND_WHITE);
        textField.setForeground(TEXT_DARK);
        textField.setFont(FONT_BODY);
        textField.setCaretColor(SECONDARY_BLUE);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SECONDARY_BLUE, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    // Styles a JLabel as a title.
    
    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_TITLE);
        label.setForeground(SECONDARY_BLUE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    // Styles a JLabel as a subtitle.
    
    public static JLabel createSubtitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SUBTITLE);
        label.setForeground(SECONDARY_BLUE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    // Styles a JLabel as body text.
    
    public static JLabel createBodyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_DARK);
        return label;
    }

    // Creates a themed panel with white background.
  
    public static JPanel createThemedPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BACKGROUND_WHITE);
        return panel;
    }

    // Creates a themed panel with BoxLayout (Y_AXIS).
  
    public static JPanel createVerticalPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_WHITE);
        return panel;
    }

    // Creates a themed panel with BoxLayout (X_AXIS).
    
    public static JPanel createHorizontalPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(BACKGROUND_WHITE);
        return panel;
    }

    // Apply global UI defaults for consistency.
    public static void applyGlobalTheme() {
        UIManager.put("Panel.background", BACKGROUND_WHITE);
        UIManager.put("OptionPane.background", BACKGROUND_WHITE);
        UIManager.put("Label.foreground", TEXT_DARK);
        UIManager.put("ComboBox.background", BACKGROUND_WHITE);
        UIManager.put("ComboBox.foreground", SECONDARY_BLUE);
        UIManager.put("TextField.background", BACKGROUND_WHITE);
        UIManager.put("TextField.foreground", TEXT_DARK);
    }
}
