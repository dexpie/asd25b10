import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Simple graph visualizer using Swing that renders an adjacency-matrix-based graph.
 * The code follows an object oriented structure with dedicated model, layout, and view components.
 */
public class GraphVisualizer {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Graph Visualizer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Create a simple initial graph
            List<String> labels = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                labels.add("Node " + i);
            }
            
            // Create some initial edges
            double[][] adjacency = new double[5][5];
            adjacency[0][1] = 10;
            adjacency[1][0] = 10;
            adjacency[1][2] = 5;
            adjacency[2][1] = 5;
            adjacency[2][3] = 20;
            adjacency[3][2] = 20;
            adjacency[3][4] = 15;
            adjacency[4][3] = 15;
            adjacency[4][0] = 8;
            adjacency[0][4] = 8;

            GraphModel model = GraphModel.fromAdjacencyMatrix(adjacency, labels);
            GraphLayout layout = new CircleLayout(200, new Point2D.Double(320, 320));

            GraphPanel graphPanel = new GraphPanel(model, layout);
            GraphControlPanel controlPanel = new GraphControlPanel(model, graphPanel);
            JScrollPane controlScroll = new JScrollPane(controlPanel);
            controlScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            controlScroll.getVerticalScrollBar().setUnitIncrement(16);
            controlScroll.setBorder(BorderFactory.createEmptyBorder());
            controlScroll.setPreferredSize(new Dimension(360, 700));

            frame.setLayout(new BorderLayout());
            frame.add(graphPanel, BorderLayout.CENTER);
            frame.add(controlScroll, BorderLayout.EAST);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

/** Represents the graph data parsed from an adjacency matrix. */
class GraphModel {
    private final List<Node> nodes;
    private final List<Edge> edges;

    private GraphModel(List<Node> nodes, List<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public static GraphModel fromAdjacencyMatrix(double[][] matrix, List<String> labels) {
        if (matrix == null || matrix.length == 0) {
            throw new IllegalArgumentException("Adjacency matrix must not be empty");
        }
        int n = matrix.length;
        if (labels.size() != n) {
            throw new IllegalArgumentException("Labels size must match matrix dimensions");
        }

        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            nodes.add(new Node(i, labels.get(i)));
        }

        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (matrix[i].length != n) {
                throw new IllegalArgumentException("Matrix must be square");
            }
            for (int j = i + 1; j < n; j++) {
                double weight = matrix[i][j];
                if (weight > 0) {
                    edges.add(new Edge(nodes.get(i), nodes.get(j), weight));
                }
            }
        }
        return new GraphModel(nodes, edges);
    }

    public void updateNodeLabel(Node node, String newLabel) {
        if (newLabel == null || newLabel.isBlank()) {
            return;
        }
        node.setLabel(newLabel.trim());
    }

    public void setEdgeWeight(int sourceId, int targetId, double weight) {
        if (sourceId == targetId) {
            return;
        }
        if (sourceId < 0 || sourceId >= nodes.size() || targetId < 0 || targetId >= nodes.size()) {
            return;
        }
        Node source = nodes.get(sourceId);
        Node target = nodes.get(targetId);
        Edge existing = findEdge(source, target);
        if (weight <= 0) {
            if (existing != null) {
                edges.remove(existing);
            }
            return;
        }
        if (existing == null) {
            edges.add(new Edge(source, target, weight));
        } else {
            existing.setWeight(weight);
        }
    }

    public Edge findEdge(Node a, Node b) {
        for (Edge edge : edges) {
            if (edge.connects(a, b)) {
                return edge;
            }
        }
        return null;
    }

    public List<Edge> getAdjacentEdges(Node node) {
        List<Edge> adjacent = new ArrayList<>();
        for (Edge edge : edges) {
            if (edge.isIncident(node)) {
                adjacent.add(edge);
            }
        }
        return adjacent;
    }

    public Node addNode(String label) {
        String safeLabel = (label == null || label.isBlank()) ? "Node " + nodes.size() : label.trim();
        Node node = new Node(nodes.size(), safeLabel);
        nodes.add(node);
        return node;
    }
}

/** Basic data class for a graph node. */
class Node {
    private final int id;
    private String label;
    private Point2D position = new Point2D.Double();

    public Node(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Point2D getPosition() {
        return position;
    }

    public void setPosition(Point2D position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return label;
    }
}

/** Simple undirected edge linking two nodes. */
class Edge {
    private final Node source;
    private final Node target;
    private double weight;

    public Edge(Node source, Node target, double weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
    }

    public Node getSource() {
        return source;
    }

    public Node getTarget() {
        return target;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean connects(Node a, Node b) {
        return (source == a && target == b) || (source == b && target == a);
    }

    public boolean isIncident(Node node) {
        return source == node || target == node;
    }

    public Node getOpposite(Node node) {
        if (node == source) {
            return target;
        } else if (node == target) {
            return source;
        }
        throw new IllegalArgumentException("Node not part of edge");
    }
}

/** Interface for layout strategies. */
interface GraphLayout {
    void apply(GraphModel model);
}

/** Arranges nodes in a circle. */
class CircleLayout implements GraphLayout {
    private final double radius;
    private final Point2D center;

    public CircleLayout(double radius, Point2D center) {
        this.radius = radius;
        this.center = center;
    }

    @Override
    public void apply(GraphModel model) {
        List<Node> nodes = model.getNodes();
        int n = nodes.size();
        if (n == 0) return;

        double angleStep = 2 * Math.PI / n;
        for (int i = 0; i < n; i++) {
            double angle = i * angleStep - Math.PI / 2; // Start from top
            double x = center.getX() + radius * Math.cos(angle);
            double y = center.getY() + radius * Math.sin(angle);
            nodes.get(i).setPosition(new Point2D.Double(x, y));
        }
    }
}

/** Panel responsible for rendering the graph. */
class GraphPanel extends JPanel {
    private int animationIndex;
    private static final int NODE_DIAMETER = 42;
    private static final int PANEL_SIZE = 640;
    private final GraphModel model;
    private final GraphLayout layout;
    private DijkstraResult dijkstraResult;
    private final DecimalFormat weightFormat = new DecimalFormat("0.##");
    private Timer animationTimer;
    private List<Node> animationSequence = List.of();
    private final Set<Node> animatedVisited = new HashSet<>();
    private boolean showShortestPath;

    public GraphPanel(GraphModel model, GraphLayout layout) {
        this.model = model;
        this.layout = layout;
        this.layout.apply(model);
        setPreferredSize(new Dimension(PANEL_SIZE, PANEL_SIZE));
        setBackground(Color.WHITE);
    }

    public void playDijkstraAnimation(DijkstraResult result) {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        this.dijkstraResult = result;
        this.animationSequence = result == null ? List.of() : result.getVisitSequence();
        this.animatedVisited.clear();
        this.animationIndex = 0;
        this.showShortestPath = false;
        if (animationSequence.isEmpty()) {
            showShortestPath = result != null;
            repaint();
            return;
        }
        animationTimer = new Timer(650, e -> advanceAnimationStep());
        animationTimer.setInitialDelay(0);
        animationTimer.start();
    }

    private void advanceAnimationStep() {
        if (dijkstraResult == null) {
            stopAnimation();
            return;
        }
        if (animationIndex < animationSequence.size()) {
            Node next = animationSequence.get(animationIndex++);
            animatedVisited.add(next);
            repaint();
        } else {
            showShortestPath = true;
            stopAnimation();
            repaint();
        }
    }

    private void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    public void clearDijkstraResult() {
        stopAnimation();
        this.dijkstraResult = null;
        this.animationSequence = List.of();
        this.animatedVisited.clear();
        this.showShortestPath = false;
        this.animationIndex = 0;
        repaint();
    }

    public void relayoutGraph() {
        layout.apply(model);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawEdges(g2d);
        drawNodes(g2d);

        g2d.dispose();
    }

    private void drawEdges(Graphics2D g2d) {
        List<Node> shortestPath = (dijkstraResult == null || !showShortestPath)
                ? List.of() : dijkstraResult.getShortestPath();
        Set<String> highlighted = new HashSet<>();
        for (int i = 0; i < shortestPath.size() - 1; i++) {
            highlighted.add(edgeKey(shortestPath.get(i), shortestPath.get(i + 1)));
        }

        for (Edge edge : model.getEdges()) {
            Point2D p1 = edge.getSource().getPosition();
            Point2D p2 = edge.getTarget().getPosition();
            String key = edgeKey(edge.getSource(), edge.getTarget());

            if (!highlighted.isEmpty() && highlighted.contains(key)) {
                g2d.setColor(new Color(0xFF6F00));
                g2d.setStroke(new BasicStroke(3.5f));
            } else {
                g2d.setColor(Color.GRAY);
                g2d.setStroke(new BasicStroke(2f));
            }
            g2d.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());

            int labelX = (int) ((p1.getX() + p2.getX()) / 2);
            int labelY = (int) ((p1.getY() + p2.getY()) / 2);
            g2d.setColor(Color.DARK_GRAY);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2d.drawString(weightFormat.format(edge.getWeight()), labelX + 4, labelY - 4);
        }
    }

    private String edgeKey(Node a, Node b) {
        int low = Math.min(a.getId(), b.getId());
        int high = Math.max(a.getId(), b.getId());
        return low + "-" + high;
    }

    private void drawNodes(Graphics2D g2d) {
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        Map<Node, Integer> visitOrder = dijkstraResult == null ? Map.of() : dijkstraResult.getVisitOrder();
        Set<Node> pathNodes = (dijkstraResult == null || !showShortestPath)
                ? Set.of() : new HashSet<>(dijkstraResult.getShortestPath());

        for (Node node : model.getNodes()) {
            Point2D p = node.getPosition();
            int x = (int) p.getX() - NODE_DIAMETER / 2;
            int y = (int) p.getY() - NODE_DIAMETER / 2;

            if (pathNodes.contains(node)) {
                g2d.setColor(new Color(0xFFB347));
            } else if (animatedVisited.contains(node)) {
                g2d.setColor(new Color(0x1EB980));
            } else {
                g2d.setColor(new Color(0x4F8EF7));
            }
            g2d.fillOval(x, y, NODE_DIAMETER, NODE_DIAMETER);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x, y, NODE_DIAMETER, NODE_DIAMETER);

            int textWidth = g2d.getFontMetrics().stringWidth(node.getLabel());
            int textHeight = g2d.getFontMetrics().getAscent();
            g2d.setColor(Color.BLACK);
            g2d.drawString(node.getLabel(), x + (NODE_DIAMETER - textWidth) / 2,
                    y + (NODE_DIAMETER + textHeight) / 2 - 4);

            if (visitOrder.containsKey(node) && animatedVisited.contains(node)) {
                String orderLabel = visitOrder.get(node) + "";
                Font previousFont = g2d.getFont();
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g2d.drawString(orderLabel, x + NODE_DIAMETER - 12, y + 14);
                g2d.setFont(previousFont);
            }
        }
    }
}

class GraphControlPanel extends JPanel {
    private final GraphModel model;
    private final GraphPanel graphPanel;
    private final JComboBox<Node> weightSourceCombo;
    private final JComboBox<Node> weightTargetCombo;
    private final JTextField weightField;
    private final JComboBox<Node> startCombo;
    private final JComboBox<Node> destinationCombo;
    private final JTextArea dijkstraArea;
    private final JPanel labelsPanel;
    private final JTextField newNodeLabelField;

    public GraphControlPanel(GraphModel model, GraphPanel graphPanel) {
        this.model = model;
        this.graphPanel = graphPanel;
        this.weightSourceCombo = new JComboBox<>();
        this.weightTargetCombo = new JComboBox<>();
        this.weightField = new JTextField("1.0", 5);
        this.startCombo = new JComboBox<>();
        this.destinationCombo = new JComboBox<>();
        this.dijkstraArea = new JTextArea(8, 18);
        this.dijkstraArea.setEditable(false);
        this.labelsPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        this.newNodeLabelField = new JTextField("Node " + model.getNodes().size());

        setPreferredSize(new Dimension(340, 680));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(true);
        setBackground(new Color(0xFFFDE7));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        refreshComboOptions();

        add(new CollapsibleSection("Node Labels", createNodeLabelSection(), true));
        add(Box.createVerticalStrut(8));
        add(new CollapsibleSection("Tambah Node", createAddNodeSection(), true));
        add(Box.createVerticalStrut(8));
        add(new CollapsibleSection("Edge Weights", createEdgeWeightSection(), true));
        add(Box.createVerticalStrut(8));
        add(new CollapsibleSection("Dijkstra", createDijkstraSection(), true));
    }

    private JPanel createNodeLabelSection() {
        JPanel container = new JPanel(new BorderLayout());
        refreshLabelsPanel();
        container.add(new JScrollPane(labelsPanel), BorderLayout.CENTER);
        return container;
    }

    private JPanel createAddNodeSection() {
        JPanel container = new JPanel(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(new JLabel("Label node baru:"));
        form.add(Box.createVerticalStrut(4));
        form.add(newNodeLabelField);
        form.add(Box.createVerticalStrut(8));

        JButton addButton = new JButton("Tambah Node");
        addButton.addActionListener(e -> addNode());
        form.add(addButton);

        JLabel hint = new JLabel("* Gunakan menu bobot untuk menghubungkan");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(Color.DARK_GRAY);
        form.add(Box.createVerticalStrut(6));
        form.add(hint);

        container.add(form, BorderLayout.CENTER);
        return container;
    }

    private JPanel createEdgeWeightSection() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        container.add(new JLabel("Dari Node:"));
        container.add(weightSourceCombo);
        container.add(Box.createVerticalStrut(6));
        container.add(new JLabel("Ke Node:"));
        container.add(weightTargetCombo);
        container.add(Box.createVerticalStrut(6));
        container.add(new JLabel("Bobot:"));
        container.add(weightField);
        container.add(Box.createVerticalStrut(8));

        JButton updateButton = new JButton("Update Bobot");
        updateButton.addActionListener(e -> updateWeight());
        container.add(updateButton);

        return container;
    }

    private JPanel createDijkstraSection() {
        JPanel container = new JPanel(new BorderLayout());

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

        controls.add(new JLabel("Start:"));
        controls.add(startCombo);
        controls.add(Box.createVerticalStrut(6));
        controls.add(new JLabel("Tujuan:"));
        controls.add(destinationCombo);
        controls.add(Box.createVerticalStrut(8));

        JButton runButton = new JButton("Run Dijkstra");
        runButton.addActionListener(e -> runDijkstra());
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            graphPanel.clearDijkstraResult();
            dijkstraArea.setText("");
        });

        controls.add(runButton);
        controls.add(Box.createVerticalStrut(4));
        controls.add(clearButton);

        JScrollPane scrollPane = new JScrollPane(dijkstraArea);
        scrollPane.setPreferredSize(new Dimension(300, 200));

        container.add(controls, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    private void handleLabelChange(Node node, JTextField field) {
        model.updateNodeLabel(node, field.getText());
        graphPanel.repaint();
        weightSourceCombo.repaint();
        weightTargetCombo.repaint();
        startCombo.repaint();
        destinationCombo.repaint();
    }

    private void refreshLabelsPanel() {
        labelsPanel.removeAll();
        for (Node node : model.getNodes()) {
            JPanel row = new JPanel(new BorderLayout(5, 0));
            row.add(new JLabel("Node " + node.getId() + ":"), BorderLayout.WEST);
            JTextField field = new JTextField(node.getLabel());
            
            field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
                void update() { handleLabelChange(node, field); }
            });

            row.add(field, BorderLayout.CENTER);
            labelsPanel.add(row);
        }
        labelsPanel.revalidate();
        labelsPanel.repaint();
    }

    private void refreshComboOptions() {
        List<Node> nodes = model.getNodes();
        resetCombo(weightSourceCombo, nodes);
        resetCombo(weightTargetCombo, nodes);
        resetCombo(startCombo, nodes);
        resetCombo(destinationCombo, nodes);
    }

    private void resetCombo(JComboBox<Node> combo, List<Node> nodes) {
        Node selected = (Node) combo.getSelectedItem();
        combo.removeAllItems();
        for (Node node : nodes) {
            combo.addItem(node);
        }
        if (selected != null) {
            combo.setSelectedItem(selected);
        }
    }

    private void addNode() {
        model.addNode(newNodeLabelField.getText());
        newNodeLabelField.setText("Node " + model.getNodes().size());
        refreshLabelsPanel();
        refreshComboOptions();
        graphPanel.clearDijkstraResult();
        graphPanel.relayoutGraph();
    }

    private void updateWeight() {
        Node source = (Node) weightSourceCombo.getSelectedItem();
        Node target = (Node) weightTargetCombo.getSelectedItem();
        if (source == null || target == null || source == target) {
            return;
        }
        try {
            double weight = Double.parseDouble(weightField.getText());
            model.setEdgeWeight(source.getId(), target.getId(), weight);
            graphPanel.repaint();
        } catch (NumberFormatException ex) {
            dijkstraArea.setText("Bobot tidak valid.");
        }
    }

    private void runDijkstra() {
        Node start = (Node) startCombo.getSelectedItem();
        Node destination = (Node) destinationCombo.getSelectedItem();
        if (start == null || destination == null) {
            return;
        }
        DijkstraResult result = GraphAlgorithms.runDijkstra(model, start, destination);
        graphPanel.playDijkstraAnimation(result);
        renderResult(result);
    }

    private void renderResult(DijkstraResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append("Urutan kunjungan: \n");
        for (Node node : result.getVisitSequence()) {
            builder.append(node.getLabel()).append(' ');
        }
        builder.append("\n\nJalur terpendek: \n");
        if (result.getShortestPath().isEmpty()) {
            builder.append("Tidak ada jalur terhubung.");
        } else {
            for (int i = 0; i < result.getShortestPath().size(); i++) {
                builder.append(result.getShortestPath().get(i).getLabel());
                if (i < result.getShortestPath().size() - 1) {
                    builder.append(" -> ");
                }
            }
            builder.append("\nTotal jarak: ")
                    .append(result.getTotalDistance().map(d -> new DecimalFormat("0.##").format(d)).orElse("-"));
        }
        dijkstraArea.setText(builder.toString());
    }
}

/** Simple collapsible container for the side panel sections. */
class CollapsibleSection extends JPanel {
    private final JButton toggleButton;
    private final JPanel contentHolder;
    private final String title;
    private boolean expanded;

    public CollapsibleSection(String title, JComponent content, boolean expanded) {
        this.title = title;
        this.toggleButton = new JButton();
        this.toggleButton.setFocusPainted(false);
        this.toggleButton.setContentAreaFilled(false);
        this.toggleButton.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
        this.toggleButton.setHorizontalAlignment(SwingConstants.LEFT);
        this.toggleButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        this.toggleButton.addActionListener(e -> toggle());

        this.contentHolder = new JPanel(new BorderLayout());
        this.contentHolder.add(content, BorderLayout.CENTER);
        this.contentHolder.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        add(toggleButton, BorderLayout.NORTH);
        add(contentHolder, BorderLayout.CENTER);

        setExpanded(expanded);
    }

    private void toggle() {
        setExpanded(!expanded);
    }

    private void setExpanded(boolean expanded) {
        this.expanded = expanded;
        contentHolder.setVisible(expanded);
        toggleButton.setText(labelForState());
        revalidate();
        repaint();
        if (getParent() != null) {
            getParent().revalidate();
            getParent().repaint();
        }
    }

    private String labelForState() {
        return (expanded ? "[-] " : "[+] ") + title;
    }
}

class GraphAlgorithms {
    public static DijkstraResult runDijkstra(GraphModel model, Node start, Node destination) {
        Map<Node, Double> distances = new HashMap<>();
        Map<Node, Node> previous = new HashMap<>();
        Set<Node> visited = new HashSet<>();
        List<Node> visitSequence = new ArrayList<>();

        PriorityQueue<NodeDistance> queue = new PriorityQueue<>();
        for (Node node : model.getNodes()) {
            double distance = node == start ? 0 : Double.POSITIVE_INFINITY;
            distances.put(node, distance);
            queue.offer(new NodeDistance(node, distance));
        }

        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();
            if (visited.contains(current.node)) {
                continue;
            }
            visited.add(current.node);
            visitSequence.add(current.node);
            if (current.node == destination) {
                break;
            }

            for (Edge edge : model.getAdjacentEdges(current.node)) {
                Node neighbor = edge.getOpposite(current.node);
                double tentative = distances.get(current.node) + edge.getWeight();
                if (tentative < distances.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                    distances.put(neighbor, tentative);
                    previous.put(neighbor, current.node);
                    queue.offer(new NodeDistance(neighbor, tentative));
                }
            }
        }

        List<Node> shortestPath = reconstructPath(previous, start, destination);
        Map<Node, Integer> visitOrder = new HashMap<>();
        for (int i = 0; i < visitSequence.size(); i++) {
            visitOrder.put(visitSequence.get(i), i + 1);
        }

        Double destinationDistance = distances.get(destination);
        Optional<Double> totalDistance = destinationDistance == null || Double.isInfinite(destinationDistance)
                ? Optional.empty() : Optional.of(destinationDistance);

        return new DijkstraResult(visitSequence, shortestPath, visitOrder, totalDistance);
    }

    private static List<Node> reconstructPath(Map<Node, Node> previous, Node start, Node destination) {
        if (start == destination) {
            return List.of(start);
        }
        List<Node> reversed = new ArrayList<>();
        Node current = destination;
        while (current != null) {
            reversed.add(current);
            if (current == start) {
                break;
            }
            current = previous.get(current);
        }
        if (reversed.get(reversed.size() - 1) != start) {
            return List.of();
        }
        List<Node> ordered = new ArrayList<>();
        for (int i = reversed.size() - 1; i >= 0; i--) {
            ordered.add(reversed.get(i));
        }
        return ordered;
    }

    private static class NodeDistance implements Comparable<NodeDistance> {
        private final Node node;
        private final double distance;

        NodeDistance(Node node, double distance) {
            this.node = node;
            this.distance = distance;
        }

        @Override
        public int compareTo(NodeDistance other) {
            return Double.compare(this.distance, other.distance);
        }
    }
}

class DijkstraResult {
    private final List<Node> visitSequence;
    private final List<Node> shortestPath;
    private final Map<Node, Integer> visitOrder;
    private final Optional<Double> totalDistance;

    public DijkstraResult(List<Node> visitSequence, List<Node> shortestPath,
                          Map<Node, Integer> visitOrder, Optional<Double> totalDistance) {
        this.visitSequence = visitSequence;
        this.shortestPath = shortestPath;
        this.visitOrder = visitOrder;
        this.totalDistance = totalDistance;
    }

    public List<Node> getVisitSequence() {
        return visitSequence;
    }

    public List<Node> getShortestPath() {
        return shortestPath;
    }

    public Map<Node, Integer> getVisitOrder() {
        return visitOrder;
    }

    public Optional<Double> getTotalDistance() {
        return totalDistance;
    }
}
