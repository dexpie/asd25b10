import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class DigiButton extends JButton {
    private Color normalBg = new Color(255, 140, 0); 
    private Color hoverBg = new Color(255, 165, 0);  
    private Color pressedBg = new Color(200, 100, 0); 
    private Color textColor = Color.BLACK; 

    public DigiButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(textColor);
        setFont(new Font("Consolas", Font.BOLD, 14));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(normalBg);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setBackground(pressedBg);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setBackground(hoverBg);
            }
        });
    }

    
    public enum ButtonStyle { PRIMARY, SECONDARY, DANGER }

    public static DigiButton createLarge(String text, ButtonStyle style) {
        DigiButton btn = new DigiButton(text);
        btn.setFont(new Font("Consolas", Font.BOLD, 18));
        btn.setPreferredSize(new Dimension(200, 50));
        return btn;
    }

    public static DigiButton createPrimary(String text) {
        return new DigiButton(text);
    }

    public static DigiButton createSecondary(String text) {
        DigiButton btn = new DigiButton(text);
        btn.normalBg = new Color(100, 100, 100);
        btn.hoverBg = new Color(120, 120, 120);
        btn.pressedBg = new Color(80, 80, 80);
        return btn;
    }

    public static DigiButton createDanger(String text) {
        DigiButton btn = new DigiButton(text);
        btn.normalBg = new Color(200, 50, 50);
        btn.hoverBg = new Color(220, 70, 70);
        btn.pressedBg = new Color(150, 30, 30);
        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (getModel().isPressed()) {
            g2.setColor(pressedBg);
        } else if (getModel().isRollover()) {
            g2.setColor(hoverBg);
        } else {
            g2.setColor(normalBg);
        }

        
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

        
        g2.setColor(new Color(255, 255, 255, 100));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);

        
        g2.setColor(textColor);
        FontMetrics fm = g2.getFontMetrics();
        Rectangle stringBounds = fm.getStringBounds(getText(), g2).getBounds();
        int textX = (getWidth() - stringBounds.width) / 2;
        int textY = (getHeight() - stringBounds.height) / 2 + fm.getAscent();
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }
}
