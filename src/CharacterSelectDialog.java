import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.*;

public class CharacterSelectDialog extends JDialog {
    private Player.DigimonPartner selectedPartner = Player.DigimonPartner.AGUMON;
    private boolean confirmed = false;

    public CharacterSelectDialog(Window parent) {
        super(parent, "Choose Your Partner", ModalityType.APPLICATION_MODAL);
        setSize(900, 550); // Sedikit diperlebar agar kartu terlihat lega
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.BLACK);

        // Title
        JLabel lblTitle = new JLabel("SELECT YOUR PARTNER DIGIMON", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Impact", Font.BOLD, 36));
        lblTitle.setForeground(Color.CYAN);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(lblTitle, BorderLayout.NORTH);

        // Cards Panel
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 15, 0));
        pnlCards.setBackground(Color.BLACK);
        pnlCards.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        pnlCards.add(createCard(Player.DigimonPartner.AGUMON, new Color(255, 100, 0)));
        pnlCards.add(createCard(Player.DigimonPartner.GABUMON, new Color(0, 100, 255)));
        pnlCards.add(createCard(Player.DigimonPartner.PATAMON, new Color(255, 200, 0)));
        pnlCards.add(createCard(Player.DigimonPartner.PALMON, new Color(0, 200, 100)));

        add(pnlCards, BorderLayout.CENTER);

        // Confirm Button
        JButton btnConfirm = new JButton("CONFIRM SELECTION");
        btnConfirm.setFont(new Font("Arial", Font.BOLD, 24));
        btnConfirm.setBackground(Color.DARK_GRAY);
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFocusPainted(false);
        btnConfirm.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        btnConfirm.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        add(btnConfirm, BorderLayout.SOUTH);
    }

    private JPanel createCard(Player.DigimonPartner partner, Color themeColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(30, 30, 30));
        // Default border (Gray)
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

        // 1. Name Label
        JLabel lblName = new JLabel(partner.name(), SwingConstants.CENTER);
        lblName.setFont(new Font("Arial", Font.BOLD, 20));
        lblName.setForeground(themeColor);
        lblName.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        card.add(lblName, BorderLayout.NORTH);

        // 2. Icon / Image Area
        JLabel lblIcon = new JLabel();
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);

        ImageIcon icon = loadResizedIcon(partner.name(), 120, 120);

        if (icon != null) {
            lblIcon.setIcon(icon);
        } else {
            lblIcon.setText(partner.name().substring(0, 1));
            lblIcon.setFont(new Font("Impact", Font.BOLD, 80));
            lblIcon.setForeground(Color.WHITE);
        }
        card.add(lblIcon, BorderLayout.CENTER);

        // 3. Description
        JTextArea txtDesc = new JTextArea(getAbilityDesc(partner));
        txtDesc.setFont(new Font("Arial", Font.PLAIN, 13));
        txtDesc.setForeground(Color.LIGHT_GRAY);
        txtDesc.setBackground(new Color(30, 30, 30));
        txtDesc.setWrapStyleWord(true);
        txtDesc.setLineWrap(true);
        txtDesc.setEditable(false);
        txtDesc.setBorder(BorderFactory.createEmptyBorder(10, 10, 15, 10));
        card.add(txtDesc, BorderLayout.SOUTH);

        // Selection Logic (Click Event)
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedPartner = partner;
                updateSelectionVisuals(card.getParent());
            }
        });

        return card;
    }

    private ImageIcon loadResizedIcon(String digimonName, int w, int h) {
        String filename = digimonName + ".png";
        Image img = null;

        try {
            File f = new File("resources/" + filename);
            if (f.exists()) {
                img = new ImageIcon(f.getAbsolutePath()).getImage();
            } else {
                java.net.URL url = getClass().getResource("/resources/" + filename);
                if (url != null) {
                    img = new ImageIcon(url).getImage();
                }
            }

            if (img != null) {
                Image resized = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                return new ImageIcon(resized);
            }
        } catch (Exception e) {
            System.out.println("Error loading image for " + digimonName);
        }
        return null;
    }

    private String getAbilityDesc(Player.DigimonPartner p) {
        return "Ready for battle!";
    }

    private void updateSelectionVisuals(Container parent) {
        for (Component c : parent.getComponents()) {
            if (c instanceof JPanel) {
                JPanel p = (JPanel) c;
                JLabel nameLbl = (JLabel) p.getComponent(0);

                if (nameLbl.getText().equals(selectedPartner.name())) {
                    p.setBorder(BorderFactory.createLineBorder(Color.CYAN, 4));
                    p.setBackground(new Color(50, 50, 60));
                    ((JTextArea)p.getComponent(2)).setBackground(new Color(50, 50, 60));
                } else {
                    p.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
                    p.setBackground(new Color(30, 30, 30));
                    ((JTextArea)p.getComponent(2)).setBackground(new Color(30, 30, 30));
                }
            }
        }
    }

    public Player.DigimonPartner getSelectedPartner() {
        return confirmed ? selectedPartner : Player.DigimonPartner.AGUMON;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
