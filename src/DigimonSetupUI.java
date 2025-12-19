import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * DigimonSetupUI - Professional Setup Wizard for Digimon Adventure Snake & Ladder Game.
 * Features:
 * - Custom styled UI with no JOptionPane input dialogs
 * - CardLayout for smooth phase transitions
 * - DigiButton for visible, themed buttons
 * - Proper GridBagLayout for centering
 */
public class DigimonSetupUI {

    // Partner Digimon options
    private static final String[] PARTNER_DIGIMON = {
            "Agumon", "Gabumon", "Biyomon", "Tentomon",
            "Palmon", "Gomamon", "Patamon", "Gatomon"
    };

    // Output Data Class
    public static class DigiDestinedData {
        public String name;
        public String partnerDigimon;
        public boolean isBot;

        public DigiDestinedData(String name, String partnerDigimon) {
            this(name, partnerDigimon, false);
        }
        
        public DigiDestinedData(String name, String partnerDigimon, boolean isBot) {
            this.name = name;
            this.partnerDigimon = partnerDigimon;
            this.isBot = isBot;
        }

        @Override
        public String toString() {
            return name + " [" + partnerDigimon + "]" + (isBot ? " [BOT]" : "");
        }
    }

    // Result storage
    private List<DigiDestinedData> result = null;
    private JDialog dialog;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JComboBox<Integer> playerCountBox;
    private int selectedPlayerCount = 2;

    // Phase 2 components
    private List<JTextField> nameFields = new ArrayList<>();
    private List<JComboBox<String>> partnerBoxes = new ArrayList<>();
    private List<JCheckBox> botCheckBoxes = new ArrayList<>();
    private JPanel phase2FormPanel;

    /**
     * Main entry point - shows the setup wizard and returns player data.
     * @param parent Parent frame (can be null)
     * @param localPartner Pre-selected partner for Player 1 (Host/Local)
     * @param remotePlayers List of pre-connected remote players (for Host)
     * @return List of DigiDestinedData, or null if cancelled
     */
    public List<DigiDestinedData> showSetupWizard(Frame parent, Player.DigimonPartner localPartner, List<DigiDestinedData> remotePlayers) {
        DigimonThemeFactory.applyGlobalTheme();

        // Create modal dialog
        dialog = new JDialog(parent, "Digimon Adventure - Player Setup", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        // Set icon
        ImageIcon icon = DigimonThemeFactory.loadScaledImage(DigimonThemeFactory.ICON_PATH, 64, 64);
        if (icon != null) {
            dialog.setIconImage(icon.getImage());
        }

        // Main panel with CardLayout
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(DigimonThemeFactory.BACKGROUND_WHITE);

        // Build phases
        cardPanel.add(buildPhase1Panel(), "PHASE1");
        cardPanel.add(buildPhase2Panel(localPartner, remotePlayers), "PHASE2");

        dialog.setContentPane(cardPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return result;
    }

    // Overload for backward compatibility
    public List<DigiDestinedData> showSetupWizard(Frame parent, Player.DigimonPartner localPartner) {
        return showSetupWizard(parent, localPartner, null);
    }

    // Overload for backward compatibility
    public List<DigiDestinedData> showSetupWizard(Frame parent) {
        return showSetupWizard(parent, Player.DigimonPartner.AGUMON, null);
    }

    /**
     * Phase 1: Select number of players
     */
    private JPanel buildPhase1Panel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(DigimonThemeFactory.BACKGROUND_WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Logo
        ImageIcon logo = DigimonThemeFactory.loadImageOrPlaceholder(
                DigimonThemeFactory.LOGO_PATH, 320, 160);
        JLabel logoLabel = new JLabel(logo);
        panel.add(logoLabel, gbc);

        // Welcome Title
        gbc.gridy++;
        gbc.insets = new Insets(20, 10, 5, 10);
        JLabel titleLabel = DigimonThemeFactory.createTitleLabel("Welcome to the Digital World!");
        panel.add(titleLabel, gbc);

        // Subtitle
        gbc.gridy++;
        gbc.insets = new Insets(0, 10, 20, 10);
        JLabel subLabel = DigimonThemeFactory.createBodyLabel("How many DigiDestined will embark on this adventure?");
        subLabel.setForeground(DigimonThemeFactory.SECONDARY_BLUE);
        panel.add(subLabel, gbc);

        // Player count selector
        gbc.gridy++;
        gbc.insets = new Insets(10, 10, 10, 10);
        JPanel selectorPanel = DigimonThemeFactory.createHorizontalPanel();
        
        JLabel selectLabel = new JLabel("Number of Players: ");
        selectLabel.setFont(DigimonThemeFactory.FONT_BODY_BOLD);
        selectLabel.setForeground(DigimonThemeFactory.SECONDARY_BLUE);
        
        playerCountBox = new JComboBox<>(new Integer[]{2, 3, 4});
        DigimonThemeFactory.styleComboBox(playerCountBox);
        playerCountBox.setPreferredSize(new Dimension(80, 35));
        
        selectorPanel.add(selectLabel);
        selectorPanel.add(Box.createHorizontalStrut(10));
        selectorPanel.add(playerCountBox);
        panel.add(selectorPanel, gbc);

        // Sound Setting
        gbc.gridy++;
        JCheckBox chkSound = new JCheckBox("Disable Sound");
        chkSound.setFont(DigimonThemeFactory.FONT_BODY);
        chkSound.setForeground(DigimonThemeFactory.SECONDARY_BLUE);
        chkSound.setOpaque(false);
        chkSound.addActionListener(e -> SoundManager.setMuted(chkSound.isSelected()));
        panel.add(chkSound, gbc);

        // Buttons
        gbc.gridy++;
        gbc.insets = new Insets(30, 10, 10, 10);
        JPanel buttonPanel = DigimonThemeFactory.createHorizontalPanel();
        
        DigiButton btnNext = DigiButton.createLarge("NEXT →", DigiButton.ButtonStyle.PRIMARY);
        DigiButton btnCancel = DigiButton.createLarge("EXIT", DigiButton.ButtonStyle.DANGER);

        btnNext.addActionListener(e -> {
            selectedPlayerCount = (Integer) playerCountBox.getSelectedItem();
            buildPhase2Form();
            cardLayout.show(cardPanel, "PHASE2");
            dialog.pack();
            dialog.setLocationRelativeTo(dialog.getParent());
        });

        btnCancel.addActionListener(e -> {
            result = null;
            dialog.dispose();
        });

        buttonPanel.add(btnNext);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btnCancel);
        panel.add(buttonPanel, gbc);

        return panel;
    }

    /**
     * Phase 2: Player registration form
     */
    private JPanel buildPhase2Panel(Player.DigimonPartner localPartner, List<DigiDestinedData> remotePlayers) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(DigimonThemeFactory.BACKGROUND_WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Header
        JPanel headerPanel = DigimonThemeFactory.createVerticalPanel();
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = DigimonThemeFactory.createSubtitleLabel("DigiDestined Registration");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        
        JLabel subLabel = DigimonThemeFactory.createBodyLabel("Enter your names and choose your partner Digimon!");
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subLabel.setForeground(DigimonThemeFactory.SECONDARY_BLUE);
        headerPanel.add(subLabel);
        
        panel.add(headerPanel, BorderLayout.NORTH);

        // Form container (will be populated dynamically)
        phase2FormPanel = DigimonThemeFactory.createVerticalPanel();
        phase2FormPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(phase2FormPanel);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(450, 280));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = DigimonThemeFactory.createHorizontalPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        DigiButton btnBack = DigiButton.createSecondary("← BACK");
        DigiButton btnStart = DigiButton.createLarge("START ADVENTURE!", DigiButton.ButtonStyle.PRIMARY);
        DigiButton btnCancel = DigiButton.createDanger("EXIT");

        btnBack.addActionListener(e -> {
            cardLayout.show(cardPanel, "PHASE1");
            dialog.pack();
            dialog.setLocationRelativeTo(dialog.getParent());
        });

        btnStart.addActionListener(e -> {
            collectPlayerData();
            dialog.dispose();
        });

        btnCancel.addActionListener(e -> {
            result = null;
            dialog.dispose();
        });

        buttonPanel.add(btnBack);
        buttonPanel.add(Box.createHorizontalStrut(15));
        buttonPanel.add(btnStart);
        buttonPanel.add(Box.createHorizontalStrut(15));
        buttonPanel.add(btnCancel);
        
        JPanel buttonWrapper = DigimonThemeFactory.createThemedPanel();
        buttonWrapper.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.add(buttonPanel);
        panel.add(buttonWrapper, BorderLayout.SOUTH);
        
        // Store local partner and remote players
        this.preselectedPartner = localPartner;
        this.preConnectedPlayers = remotePlayers;

        return panel;
    }

    /**
     * Dynamically builds the registration form based on player count.
     */
    private void buildPhase2Form() {
        phase2FormPanel.removeAll();
        nameFields.clear();
        partnerBoxes.clear();
        botCheckBoxes.clear();

        for (int i = 0; i < selectedPlayerCount; i++) {
            JPanel playerCard = createPlayerCard(i + 1);
            phase2FormPanel.add(playerCard);
            phase2FormPanel.add(Box.createVerticalStrut(10));
        }

        phase2FormPanel.revalidate();
        phase2FormPanel.repaint();
    }
    
    private Player.DigimonPartner preselectedPartner;
    private List<DigiDestinedData> preConnectedPlayers;

    /**
     * Creates a styled card for a single player's input.
     */
    private JPanel createPlayerCard(int playerNumber) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(DigimonThemeFactory.BACKGROUND_WHITE);
        
        // Styled border
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(DigimonThemeFactory.PRIMARY_ORANGE, 2),
                "DigiDestined #" + playerNumber
        );
        border.setTitleColor(DigimonThemeFactory.SECONDARY_BLUE);
        border.setTitleFont(DigimonThemeFactory.FONT_BODY_BOLD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                border
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Name label
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(DigimonThemeFactory.FONT_BODY_BOLD);
        nameLabel.setForeground(DigimonThemeFactory.SECONDARY_BLUE);
        card.add(nameLabel, gbc);

        // Name field
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField nameField = new JTextField(15);
        DigimonThemeFactory.styleTextField(nameField);
        nameField.setText("Player " + playerNumber);
        nameFields.add(nameField);
        card.add(nameField, gbc);
        
        // Bot Checkbox
        gbc.gridx = 2;
        gbc.weightx = 0;
        JCheckBox botCheck = new JCheckBox("CPU");
        botCheck.setBackground(DigimonThemeFactory.BACKGROUND_WHITE);
        botCheck.setFont(DigimonThemeFactory.FONT_SMALL);
        botCheckBoxes.add(botCheck);
        card.add(botCheck, gbc);

        // Partner label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel partnerLabel = new JLabel("Partner:");
        partnerLabel.setFont(DigimonThemeFactory.FONT_BODY_BOLD);
        partnerLabel.setForeground(DigimonThemeFactory.SECONDARY_BLUE);
        card.add(partnerLabel, gbc);

        // Partner Selection UI (Button or Locked Label)
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // We use a hidden ComboBox to store the value for compatibility with collectPlayerData()
        JComboBox<String> partnerBox = new JComboBox<>(PARTNER_DIGIMON);
        partnerBox.setVisible(false); 
        partnerBoxes.add(partnerBox);

        // Custom Selection Panel
        JPanel selectionPanel = new JPanel(new BorderLayout(5, 0));
        selectionPanel.setOpaque(false);
        
        JLabel selectedLabel = new JLabel("Agumon", SwingConstants.CENTER);
        selectedLabel.setFont(DigimonThemeFactory.FONT_BODY_BOLD);
        selectedLabel.setForeground(DigimonThemeFactory.PRIMARY_ORANGE);
        selectedLabel.setBorder(BorderFactory.createLineBorder(DigimonThemeFactory.BORDER_BLUE));
        selectedLabel.setPreferredSize(new Dimension(100, 30));
        
        JButton selectBtn = new JButton("Choose");
        DigimonThemeFactory.styleButton(selectBtn, DigiButton.ButtonStyle.SECONDARY);
        selectBtn.setPreferredSize(new Dimension(80, 30));
        
        selectionPanel.add(selectedLabel, BorderLayout.CENTER);
        selectionPanel.add(selectBtn, BorderLayout.EAST);
        
        card.add(selectionPanel, gbc);

        // Logic for Pre-selection vs Manual Selection
        boolean isLocked = false;
        String initialPartner = PARTNER_DIGIMON[(playerNumber - 1) % PARTNER_DIGIMON.length];

        if (playerNumber == 1 && preselectedPartner != null) {
            initialPartner = preselectedPartner.name();
            isLocked = true;
        } 
        else if (preConnectedPlayers != null && playerNumber - 2 < preConnectedPlayers.size() && playerNumber > 1) {
            DigiDestinedData remoteData = preConnectedPlayers.get(playerNumber - 2);
            nameField.setText(remoteData.name);
            nameField.setEditable(false);
            initialPartner = remoteData.partnerDigimon;
            isLocked = true;
            botCheck.setEnabled(false);
        }

        // Apply Initial State
        final String finalInitial = initialPartner;
        final boolean lockedStatus = isLocked;
        
        // Normalize string case
        String display = initialPartner.substring(0, 1).toUpperCase() + initialPartner.substring(1).toLowerCase();
        selectedLabel.setText(display);
        partnerBox.setSelectedItem(display); // Try match
        // Fallback loop
        for(int k=0; k<partnerBox.getItemCount(); k++) {
            if(partnerBox.getItemAt(k).equalsIgnoreCase(finalInitial)) {
                partnerBox.setSelectedIndex(k);
                selectedLabel.setText(partnerBox.getItemAt(k));
                break;
            }
        }

        if (isLocked) {
            selectBtn.setEnabled(false);
            selectBtn.setText("Locked");
        } else {
            selectBtn.addActionListener(e -> {
                Window parentWin = SwingUtilities.getWindowAncestor(card);
                CharacterSelectDialog csd = new CharacterSelectDialog(parentWin);
                csd.setVisible(true);
                if (csd.isConfirmed()) {
                    Player.DigimonPartner p = csd.getSelectedPartner();
                    String pName = p.name().substring(0, 1).toUpperCase() + p.name().substring(1).toLowerCase();
                    selectedLabel.setText(pName);
                    
                    // Update hidden box
                    for(int k=0; k<partnerBox.getItemCount(); k++) {
                        if(partnerBox.getItemAt(k).equalsIgnoreCase(pName)) {
                            partnerBox.setSelectedIndex(k);
                            break;
                        }
                    }
                }
            });
        }
        
        // Disable selection if Bot is checked
        botCheck.addActionListener(e -> {
            selectBtn.setEnabled(!botCheck.isSelected() && !lockedStatus);
            if (botCheck.isSelected()) {
                // Randomize for bot? Or keep last selection.
                // Let's keep it simple.
            }
        });

        return card;
    }

    /**
     * Collects data from the form and stores in result.
     */
    private void collectPlayerData() {
        result = new ArrayList<>();
        for (int i = 0; i < selectedPlayerCount; i++) {
            String name = nameFields.get(i).getText().trim();
            if (name.isEmpty()) {
                name = "DigiDestined " + (i + 1);
            }
            String partner = (String) partnerBoxes.get(i).getSelectedItem();
            boolean isBot = botCheckBoxes.get(i).isSelected();
            result.add(new DigiDestinedData(name, partner, isBot));
        }
    }

    // ==================== STATIC HELPER DIALOGS ====================

    /**
     * Shows a themed "Who Goes First" dialog.
     */
    public static void showTurnOrderDialog(Frame parent, List<String> playerOrder) {
        JDialog dialog = new JDialog(parent, "Turn Order", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        ImageIcon icon = DigimonThemeFactory.loadScaledImage(DigimonThemeFactory.ICON_PATH, 48, 48);
        if (icon != null) {
            dialog.setIconImage(icon.getImage());
        }

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(DigimonThemeFactory.BACKGROUND_WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(8, 10, 8, 10);

        // Title
        JLabel titleLabel = DigimonThemeFactory.createSubtitleLabel("⚔ Turn Order Decided! ⚔");
        panel.add(titleLabel, gbc);

        // Player order list
        gbc.gridy++;
        gbc.insets = new Insets(15, 10, 15, 10);
        JPanel orderPanel = DigimonThemeFactory.createVerticalPanel();
        orderPanel.setBorder(BorderFactory.createLineBorder(DigimonThemeFactory.PRIMARY_ORANGE, 2));
        orderPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DigimonThemeFactory.PRIMARY_ORANGE, 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        for (int i = 0; i < playerOrder.size(); i++) {
            JLabel orderLabel = new JLabel((i + 1) + ". " + playerOrder.get(i));
            orderLabel.setFont(DigimonThemeFactory.FONT_BODY_BOLD);
            orderLabel.setForeground(i == 0 ? DigimonThemeFactory.PRIMARY_ORANGE : DigimonThemeFactory.TEXT_DARK);
            orderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            orderPanel.add(orderLabel);
            orderPanel.add(Box.createVerticalStrut(5));
        }
        panel.add(orderPanel, gbc);

        // First player highlight
        gbc.gridy++;
        gbc.insets = new Insets(10, 10, 15, 10);
        JLabel firstLabel = new JLabel("\"" + playerOrder.get(0) + "\" goes first!");
        firstLabel.setFont(DigimonThemeFactory.FONT_BODY_BOLD);
        firstLabel.setForeground(DigimonThemeFactory.SECONDARY_BLUE);
        panel.add(firstLabel, gbc);

        // OK Button
        gbc.gridy++;
        gbc.insets = new Insets(10, 10, 5, 10);
        DigiButton btnOk = DigiButton.createPrimary("LET'S GO!");
        btnOk.addActionListener(e -> dialog.dispose());
        panel.add(btnOk, gbc);

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /**
     * Creates a themed "Cast Die" button for the main game.
     */
    public static DigiButton createCastDieButton() {
        DigiButton btn = DigiButton.createLarge("🎲 CAST DIE!", DigiButton.ButtonStyle.PRIMARY);
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));
        return btn;
    }
}
