import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class DigiDialog extends JDialog {
    private static final Color BG_COLOR = new Color(10, 15, 30, 245); 
    private static final Color BORDER_COLOR = new Color(0, 255, 255); 
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Font FONT_TITLE = new Font("Consolas", Font.BOLD, 20);
    private static final Font FONT_MSG = new Font("Consolas", Font.PLAIN, 14);

    public DigiDialog(Window owner, String title, String message, boolean isConfirm) {
        super(owner, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setBackground(new Color(0,0,0,0)); // Transparent for custom painting

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2.setColor(BG_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Tech Grid
                g2.setColor(new Color(0, 255, 255, 20));
                for(int i=0; i<getWidth(); i+=20) g2.drawLine(i, 0, i, getHeight());
                for(int i=0; i<getHeight(); i+=20) g2.drawLine(0, i, getWidth(), i);

                // Border
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 20, 20);
                
                // Header Line
                g2.drawLine(20, 40, getWidth()-20, 40);
                
                g2.dispose();
            }
        };
        content.setLayout(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Title
        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(new Color(0, 255, 255));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        content.add(lblTitle, BorderLayout.NORTH);

        // Message
        JTextArea txtMsg = new JTextArea(message);
        txtMsg.setFont(FONT_MSG);
        txtMsg.setForeground(TEXT_COLOR);
        txtMsg.setOpaque(false);
        txtMsg.setEditable(false);
        txtMsg.setLineWrap(true);
        txtMsg.setWrapStyleWord(true);
        txtMsg.setMargin(new Insets(20, 10, 20, 10));
        content.add(txtMsg, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);
        
        if (isConfirm) {
            DigiButton btnYes = new DigiButton("CONFIRM");
            btnYes.addActionListener(e -> {
                firePropertyChange("result", -1, JOptionPane.YES_OPTION);
                dispose();
            });
            
            DigiButton btnNo = DigiButton.createDanger("CANCEL");
            btnNo.addActionListener(e -> {
                firePropertyChange("result", -1, JOptionPane.NO_OPTION);
                dispose();
            });
            
            btnPanel.add(btnYes);
            btnPanel.add(btnNo);
        } else {
            DigiButton btnOk = new DigiButton("ACKNOWLEDGE");
            btnOk.addActionListener(e -> dispose());
            btnPanel.add(btnOk);
        }
        
        content.add(btnPanel, BorderLayout.SOUTH);
        
        setContentPane(content);
        
        // Size calculation
        int w = 400;
        int h = 200 + (message.length() / 40) * 20;
        setSize(w, Math.min(h, 400));
        setLocationRelativeTo(owner);
    }

    public static void showMessage(Component parent, String title, String message) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        new DigiDialog(window, title, message, false).setVisible(true);
    }

    public static int showConfirm(Component parent, String title, String message) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        final int[] result = {JOptionPane.NO_OPTION};
        
        DigiDialog dlg = new DigiDialog(window, title, message, true);
        dlg.addPropertyChangeListener("result", evt -> result[0] = (int) evt.getNewValue());
        dlg.setVisible(true);
        
        return result[0];
    }
}
