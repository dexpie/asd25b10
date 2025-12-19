import javax.swing.*;
import java.awt.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class LobbyDialog extends JDialog {
    private DefaultListModel<String> clientListModel;
    private boolean started = false;
    private List<DigimonSetupUI.DigiDestinedData> connectedPlayers = new ArrayList<>();

    public LobbyDialog(JFrame parent, NetworkManager net, boolean isHost) {
        super(parent, isHost ? "Host Lobby" : "Client Lobby", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        
        JPanel header = new JPanel(new GridLayout(0, 1));
        header.setBackground(new Color(0, 50, 100));
        
        JLabel title = new JLabel(isHost ? "HOSTING GAME" : "CONNECTED TO LOBBY", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        header.add(title);
        
        if (isHost) {
            JLabel ipLabel = new JLabel("Your IP: " + net.getLocalIP() + " | Port: 12345", SwingConstants.CENTER);
            ipLabel.setForeground(Color.CYAN);
            header.add(ipLabel);
        }
        add(header, BorderLayout.NORTH);
        
        
        clientListModel = new DefaultListModel<>();
        if (isHost) clientListModel.addElement("Host (You)");
        else clientListModel.addElement("Connected to Host");
        
        JList<String> list = new JList<>(clientListModel);
        list.setBackground(new Color(20, 20, 40));
        list.setForeground(Color.GREEN);
        add(new JScrollPane(list), BorderLayout.CENTER);
        
        
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(new Color(0, 50, 100));
        
        if (isHost) {
            JButton startBtn = new JButton("START SETUP");
            startBtn.addActionListener(e -> {
                started = true;
                dispose();
            });
            btnPanel.add(startBtn);
            
            
            net.setOnClientConnected(socket -> {
                SwingUtilities.invokeLater(() -> {
                    clientListModel.addElement("Player connected from: " + socket.getInetAddress().getHostAddress());
                });
            });

            
            net.setOnMessageReceived(msg -> {
                if (msg.startsWith("CLIENT_INFO:")) {
                    String[] parts = msg.split(":");
                    if (parts.length >= 3) {
                        String pName = parts[1];
                        String pPartner = parts[2];
                        connectedPlayers.add(new DigimonSetupUI.DigiDestinedData(pName, pPartner));
                        SwingUtilities.invokeLater(() -> {
                            clientListModel.addElement(">> " + pName + " joined with " + pPartner);
                        });
                    }
                }
            });

        } else {
            JLabel waitLbl = new JLabel("Waiting for Host to start...");
            waitLbl.setForeground(Color.WHITE);
            btnPanel.add(waitLbl);
            
            
            
        }
        
        JButton cancelBtn = new JButton("CANCEL");
        cancelBtn.addActionListener(e -> {
            started = false;
            dispose();
        });
        btnPanel.add(cancelBtn);
        
        add(btnPanel, BorderLayout.SOUTH);
    }
    
    public boolean isStarted() { return started; }
    public List<DigimonSetupUI.DigiDestinedData> getConnectedPlayers() { return connectedPlayers; }
}
