import java.awt.Color;
import java.awt.Font;

public class GameStyle {
    
    public static final Color BACKGROUND_START = new Color(25, 25, 35); 
    public static final Color BACKGROUND_END = new Color(15, 15, 25);   

    
    public static final Color UI_BG = new Color(40, 40, 60); 
    public static final Color UI_BORDER = new Color(200, 200, 200); 
    public static final Color UI_HIGHLIGHT = new Color(60, 60, 90);

    
    public static final Color CARD_BG = new Color(0, 0, 0, 0); 
    public static final Color CARD_SHADOW = new Color(0, 0, 0, 0); 
    
    
    public static final Color TILE_1 = new Color(255, 255, 255, 5); 
    public static final Color TILE_2 = new Color(255, 255, 255, 5); 
    public static final Color TILE_BORDER = new Color(255, 255, 255, 40); 
    public static final Color TILE_TEXT = new Color(255, 255, 255, 150); 
    
    
    public static final Color STAR_COLOR = new Color(255, 215, 0); 
    public static final Color STAR_GLOW = new Color(255, 215, 0, 50);
    
    
    public static final Color TELEPORT_UP = new Color(0, 255, 255); 
    public static final Color TELEPORT_DOWN = new Color(255, 50, 50); 
    
    public static final Color LADDER_RAIL = new Color(0, 0, 0, 0); 
    public static final Color LADDER_RUNG = new Color(0, 0, 0, 0); 

    public static final Color[] PLAYER_COLORS = {
            new Color(255, 80, 80),    
            new Color(80, 150, 255),   
            new Color(255, 220, 50),   // Rogue Gold
            new Color(80, 255, 100)    // Ranger Green
    };

    public static final Color GREEN_MOVE = new Color(50, 255, 100);
    public static final Color RED_MOVE = new Color(255, 80, 80);
    
    // UI accents
    public static final Color ACCENT = new Color(255, 140, 0); // Legendary Orange
    public static final Color BUTTON_TEXT = new Color(255, 255, 255);

    // New Fantasy Colors
    public static final Color COLOR_PARCHMENT = new Color(244, 228, 188); // Parchment
    public static final Color COLOR_WOOD = new Color(139, 69, 19); // Saddle Brown
    public static final Color COLOR_GOLD_BORDER = new Color(218, 165, 32); // Goldenrod

    // Fonts - Fantasy
    public static final Font FONT_FANTASY = new Font("Serif", Font.BOLD, 18); // Fallback to Serif
    public static final Font FONT_TITLE = new Font("Serif", Font.BOLD, 22);
    public static final Font FONT_INFO = new Font("Serif", Font.BOLD, 14);
    public static final Font FONT_TILE = new Font("Serif", Font.BOLD, 12); 
    public static final Font FONT_TILE_BIG = new Font("Serif", Font.BOLD, 16);
    public static final Font FONT_SLOT = new Font("Serif", Font.BOLD, 28);
    public static final Font FONT_SCORE = new Font("Serif", Font.BOLD, 12);
    public static final Font FONT_BODY = new Font("Serif", Font.PLAIN, 12);

    public static final Color SCORE_COLOR = new Color(255, 215, 0); // Gold
}
