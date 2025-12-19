import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NetworkManager {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isHost;
    private List<Socket> clientSockets = new ArrayList<>(); 
    private List<PrintWriter> clientWriters = new ArrayList<>(); 
    private Consumer<String> onMessageReceived;
    private Consumer<Socket> onClientConnected;

    public void startHost(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        isHost = true;
        System.out.println("Server started on port " + port);
        
        
        new Thread(() -> {
            while (true) {
                try {
                    Socket client = serverSocket.accept();
                    synchronized (clientSockets) {
                        clientSockets.add(client);
                        PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
                        clientWriters.add(writer);
                    }
                    System.out.println("Client connected: " + client.getInetAddress());
                    
                    if (onClientConnected != null) {
                        onClientConnected.accept(client);
                    }
                    
                    
                    new Thread(() -> listenToSocket(client)).start();
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    public void setOnClientConnected(Consumer<Socket> listener) {
        this.onClientConnected = listener;
    }

    public void connectToHost(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        isHost = false;
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println("Connected to server " + ip + ":" + port);
        
        
        new Thread(() -> listenToSocket(clientSocket)).start();
    }

    private void listenToSocket(Socket socket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (onMessageReceived != null) {
                    onMessageReceived.accept(line);
                }
                
                
                if (isHost) {
                    broadcast(line, socket);
                }
            }
        } catch (IOException e) {
            System.out.println("Connection lost: " + e.getMessage());
        }
    }

    public void sendMessage(String msg) {
        if (isHost) {
            broadcast(msg, null); 
        } else {
            if (out != null) out.println(msg);
        }
    }

    private void broadcast(String msg, Socket excludeClient) {
        synchronized (clientSockets) {
            for (int i = 0; i < clientSockets.size(); i++) {
                Socket s = clientSockets.get(i);
                if (s != excludeClient) {
                    try {
                        clientWriters.get(i).println(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public List<Socket> getConnectedSockets() {
        return new ArrayList<>(clientSockets);
    }

    public void setOnMessageReceived(Consumer<String> listener) {
        this.onMessageReceived = listener;
    }
    
    public boolean isHost() { return isHost; }
    
    public String getLocalIP() {
        try {
            
            java.util.Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;

                java.util.Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "Unknown";
        }
    }
}
