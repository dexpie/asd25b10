import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class OnlineSetupUI {
    private NetworkManager networkManager;
    private boolean isOnline = false;
    private Player.DigimonPartner localPartner;
    private List<DigimonSetupUI.DigiDestinedData> connectedPlayers = new ArrayList<>();

    public OnlineSetupUI(Player.DigimonPartner partner) {
        this.localPartner = partner;
    }

    public boolean showModeSelection(JFrame parent) {
        
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Select Game Mode", SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);
        
        JCheckBox muteBox = new JCheckBox("Disable Sound");
        muteBox.addActionListener(e -> SoundManager.setMuted(muteBox.isSelected()));
        panel.add(muteBox, BorderLayout.SOUTH);

        String[] options = {"Local Game", "Host Online Game", "Join Online Game"};
        int choice = JOptionPane.showOptionDialog(parent, 
            panel, 
            "Digimon Adventure Online", 
            JOptionPane.DEFAULT_OPTION, 
            JOptionPane.QUESTION_MESSAGE, 
            null, options, options[0]);

        if (choice == 0) return false; 

        networkManager = new NetworkManager();
        try {
            if (choice == 1) { 
                networkManager.startHost(12345);
                
                
                LobbyDialog lobby = new LobbyDialog(parent, networkManager, true);
                lobby.setVisible(true);
                
                if (!lobby.isStarted()) {
                    return false; 
                }
                connectedPlayers = lobby.getConnectedPlayers();
                isOnline = true;
                
            } else if (choice == 2) { 
                String ip = JOptionPane.showInputDialog(parent, "Enter Host IP:", "localhost");
                if (ip != null && !ip.isEmpty()) {
                    networkManager.connectToHost(ip, 12345);
                    
                    networkManager.sendMessage("CLIENT_INFO:Player:" + localPartner.name());
                    
                    JOptionPane.showMessageDialog(parent, "Connected to Host! Waiting for game to start...", "Success", JOptionPane.INFORMATION_MESSAGE);
                    isOnline = true;
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "Network Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return isOnline;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }
    
    public List<DigimonSetupUI.DigiDestinedData> getConnectedPlayers() {
        return connectedPlayers;
    }
}
