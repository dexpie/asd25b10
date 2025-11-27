import java.awt.Color;
import java.awt.Font;

public class GameStyle {
    // Modern Dark Theme Palette
    public static final Color BACKGROUND_START = new Color(43, 50, 178); // Deep Blue
    public static final Color BACKGROUND_END = new Color(20, 136, 204);   // Lighter Blue

    // Board Colors
    public static final Color BOARD_SHADOW = new Color(0, 0, 0, 60);
    public static final Color TILE_1 = new Color(255, 255, 255);
    public static final Color TILE_2 = new Color(240, 248, 255); // Alice Blue
    public static final Color TILE_TEXT = new Color(100, 110, 120);
    
    // Elements
    public static final Color STAR_COLOR = new Color(255, 200, 0);
    public static final Color STAR_GLOW = new Color(255, 220, 100, 100);
    
    public static final Color SNAKE_BODY = new Color(231, 76, 60);
    public static final Color SNAKE_PATTERN = new Color(192, 57, 43);
    
    public static final Color LADDER_RAIL = new Color(211, 84, 0); // Dark Orange/Wood
    public static final Color LADDER_RUNG = new Color(230, 126, 34);

    public static final Color[] PLAYER_COLORS = {
            new Color(255, 82, 82),    // Neon Red
            new Color(52, 172, 224),   // Neon Blue
            new Color(255, 177, 66),   // Neon Orange
            new Color(33, 140, 116)    // Neon Teal
    };

    public static final Color GREEN_MOVE = new Color(46, 213, 115);
    public static final Color RED_MOVE = new Color(255, 71, 87);
    
    // Fonts
    public static final Font FONT_SLOT = new Font("Arial Black", Font.BOLD, 28);
    public static final Font FONT_INFO = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_TILE = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_TILE_BIG = new Font("Segoe UI", Font.BOLD, 16);
    public static final Color SCORE_COLOR = new Color(241, 196, 15); // Gold
    public static final Font FONT_SCORE = new Font("Segoe UI", Font.BOLD, 10);
}
