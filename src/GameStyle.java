import java.awt.Color;
import java.awt.Font;

public class GameStyle {
    public static final Color BG_BOARD_1 = new Color(245, 245, 245);
    public static final Color BG_BOARD_2 = new Color(255, 255, 255);
    public static final Color STAR_COLOR = new Color(255, 215, 0); // Emas
    public static final Color BOARD_BORDER = new Color(60, 60, 60);
    public static final Color PANEL_BG_START = new Color(44, 62, 80);
    public static final Color PANEL_BG_END = new Color(52, 73, 94);

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
    public static final Font FONT_TILE = new Font("Segoe UI", Font.BOLD, 14);
}
