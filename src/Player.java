import java.awt.Color;
import java.awt.Image;
import java.util.Stack;
import javax.swing.ImageIcon;

public class Player {
    int id;
    String name;
    String partnerDigimon;
    Color color;
    int position = 0;
    double currentVisualPos = 0.0;
    boolean primePowerMode = false;
    Stack<Integer> moveHistory = new Stack<>();
    int score = 0;
    Image avatar;

    boolean isBot = false;

    public enum DigimonPartner { AGUMON, GABUMON, PATAMON, PALMON }
    DigimonPartner partnerType = DigimonPartner.AGUMON;

    public Player(int id, String name, String partnerDigimon, Color color) {
        this.id = id;
        this.name = name;
        this.partnerDigimon = partnerDigimon;
        this.color = color;
        this.moveHistory.push(0);
        

        try {
            this.partnerType = DigimonPartner.valueOf(partnerDigimon.toUpperCase());
        } catch (Exception e) {
            this.partnerType = DigimonPartner.AGUMON;
        }
        
        loadAvatar();
    }
    
    public void setPartner(DigimonPartner p) {
        this.partnerType = p;
        this.partnerDigimon = p.name();
        loadAvatar();
    }
    
    public void setBot(boolean bot) { this.isBot = bot; }
    
    private void loadAvatar() {

        try {
            java.net.URL url = getClass().getResource("/resources/" + partnerDigimon + ".png");
            if (url != null) {
                this.avatar = new ImageIcon(url).getImage();
            } else {

                java.io.File f = new java.io.File("resources/" + partnerDigimon + ".png");
                if (f.exists()) {
                    this.avatar = new ImageIcon(f.getAbsolutePath()).getImage();
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to load avatar for " + partnerDigimon);
        }
    }

    public void addScore(int s) { this.score += s; }
    public int getScore() { return score; }
}
