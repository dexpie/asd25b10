import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

public class GameController {
    private BoardGraph board;
    private GamePanel panel;
    private List<Player> allPlayers;
    private LinkedList<Player> turnQueue; // Menggunakan Queue sesuai permintaan

    private boolean isGameStarted = false;
    private boolean isAnimating = false;
    private int finalDiceValue;
    private boolean moveForward;

    public GameController(BoardGraph board, GamePanel panel, List<Player> players) {
        this.board = board; this.panel = panel; this.allPlayers = players;
        this.turnQueue = new LinkedList<>();
    }

    public void startOrderRoulette() {
        panel.setStatus("Determining who goes first...");
        Timer rouletteTimer = new Timer(100, null);
        final long startTime = System.currentTimeMillis();
        final int[] currentIndex = {0};

        rouletteTimer.addActionListener(e -> {
            if (System.currentTimeMillis() - startTime < 3000) {
                panel.setOrderAnimState(true, currentIndex[0]);
                currentIndex[0] = (currentIndex[0] + 1) % allPlayers.size();
            } else {
                ((Timer)e.getSource()).stop();
                int winnerIdx = (int)(Math.random() * allPlayers.size());
                panel.setOrderAnimState(true, winnerIdx);
                Timer pause = new Timer(1500, evt -> setupQueue(winnerIdx));
                pause.setRepeats(false); pause.start();
            }
        });
        rouletteTimer.start();
    }

    private void setupQueue(int winnerIdx) {
        Collections.rotate(allPlayers, -winnerIdx);
        turnQueue.clear();
        turnQueue.addAll(allPlayers);
        panel.setTurnQueue(turnQueue);

        Player first = turnQueue.peek();
        panel.setOrderAnimState(false, 0);
        panel.setStatus("Ready! " + first.name + " goes first.");
        isGameStarted = true;
        panel.refresh();
        JOptionPane.showMessageDialog(panel, first.name + " starts first!");
    }

    public void startTurn() {
        if (!isGameStarted || isAnimating) return;
        Player currentP = turnQueue.peek();
        if (currentP.position >= 64) return;
        isAnimating = true;
        startDiceAnimation();
    }

    private void startDiceAnimation() {
        finalDiceValue = (int)(Math.random() * 6) + 1;
        Timer diceTimer = new Timer(80, null);
        final long startTime = System.currentTimeMillis();
        diceTimer.addActionListener(e -> {
            if (System.currentTimeMillis() - startTime < 1000) {
                panel.setDiceState(true, (int)(Math.random() * 6) + 1);
            } else {
                ((Timer)e.getSource()).stop();
                panel.setDiceState(true, finalDiceValue);
                Timer pause = new Timer(600, evt -> {
                    panel.setDiceState(false, 1); startChaosSlotMachine();
                });
                pause.setRepeats(false); pause.start();
            }
        });
        diceTimer.start();
    }

    private void startChaosSlotMachine() {
        Player currentP = turnQueue.peek();
        double prob = Math.random();
        moveForward = (prob <= 0.80);
        if (currentP.position == 0) moveForward = true;

        Timer slotTimer = new Timer(20, null);
        final long startTime = System.currentTimeMillis();
        final int[] scrollY = {0};
        slotTimer.addActionListener(e -> {
            if (System.currentTimeMillis() - startTime < 1500) {
                scrollY[0] += 25; panel.setChaosState(true, scrollY[0]);
            } else {
                ((Timer)e.getSource()).stop();
                panel.setChaosResult(moveForward);
                Timer pause = new Timer(1000, evt -> {
                    panel.hideOverlay(); movePlayer();
                });
                pause.setRepeats(false); pause.start();
            }
        });
        slotTimer.start();
    }

    private void movePlayer() {
        Player currentP = turnQueue.peek();
        List<Integer> steps = new ArrayList<>();
        int simPos = currentP.position;
        boolean isMovingForward = moveForward; // Default from slot machine

        if (currentP.primePowerMode) {
            isMovingForward = true; // Prime mode is always forward
            List<Integer> path = getShortestPathToWin(simPos);
            int moves = Math.min(finalDiceValue, path.size());
            for (int i = 0; i < moves; i++) {
                steps.add(path.get(i));
            }
            currentP.primePowerMode = false;
            panel.setStatus("Shortest Path Move!");
        } else {
            if (moveForward) {
                boolean headingUp = true;
                for (int i = 0; i < finalDiceValue; i++) {
                    if (simPos == 64) headingUp = false;
                    if (headingUp) { if (simPos < 64) simPos++; }
                    else { if (simPos > 1) simPos--; }
                    steps.add(simPos);
                }
                panel.setStatus("Walking...");
            } else {
                // BACKWARD using Stack History
                Stack<Integer> tempHist = new Stack<>();
                tempHist.addAll(currentP.moveHistory);
                
                for (int i = 0; i < finalDiceValue; i++) {
                    if (tempHist.size() > 1) {
                        tempHist.pop(); // Remove current
                        steps.add(tempHist.peek()); // Go to previous
                    } else {
                        steps.add(0); // Stay at start
                    }
                }
                panel.setStatus("Walking Backwards (History)...");
            }
        }

        final boolean finalIsMovingForward = isMovingForward;
        final int[] stepIdx = {0};
        Timer moveTimer = new Timer(300, e -> {
            if (stepIdx[0] < steps.size()) {
                int nextPos = steps.get(stepIdx[0]);
                currentP.position = nextPos;
                
                // Update History
                if (finalIsMovingForward) {
                    currentP.moveHistory.push(nextPos);
                } else {
                    if (currentP.moveHistory.size() > 1) {
                        currentP.moveHistory.pop();
                    }
                }

                panel.refresh();
                stepIdx[0]++;
            } else {
                ((Timer)e.getSource()).stop();
                finishTurn(currentP);
            }
        });
        moveTimer.start();
    }

    private void finishTurn(Player p) {
        isAnimating = false;

        // Check Connections (Snakes/Ladders)
        Map<Integer, Integer> conns = board.getConnections();
        if (conns.containsKey(p.position)) {
            int dest = conns.get(p.position);
            p.position = dest;
            p.moveHistory.push(dest); // Add jump to history
            panel.refresh();
            JOptionPane.showMessageDialog(panel, (dest > p.position ? "LADDER! Up to " : "SNAKE! Down to ") + dest);
        }

        if (p.position == 64) {
            JOptionPane.showMessageDialog(panel, p.name + " WINS THE GAME!");
            System.exit(0);
        }

        // Check Prime for Next Turn
        if (isPrime(p.position)) {
            p.primePowerMode = true;
            JOptionPane.showMessageDialog(panel, "PRIME NUMBER (" + p.position + ")! \nShortest Path Activated for Next Turn!");
        }

        // LOGIC GANTI GILIRAN (QUEUE)
        boolean isStarTile = (p.position > 0 && p.position % 5 == 0);

        if (isStarTile) {
            // EXTRA TURN: Hapus depan, masukkan depan lagi
            Player samePlayer = turnQueue.poll();
            turnQueue.addFirst(samePlayer);
            JOptionPane.showMessageDialog(panel, "⭐ STAR TILE! ⭐\n" + p.name + " gets an EXTRA TURN!");
            panel.setStatus("Extra Turn for " + p.name + "!");
        } else {
            // NORMAL TURN: Hapus depan, masukkan belakang
            Player donePlayer = turnQueue.poll();
            turnQueue.offer(donePlayer);
            Player nextPlayer = turnQueue.peek();
            panel.setStatus("Click Roll for " + nextPlayer.name);
        }
        panel.refresh();
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    private List<Integer> getShortestPathToWin(int startNode) {
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> parent = new HashMap<>();
        
        for (int i = 1; i <= 64; i++) dist.put(i, Integer.MAX_VALUE);
        
        dist.put(startNode, 0);
        pq.add(new int[]{startNode, 0});
        
        Map<Integer, Integer> conns = board.getConnections();

        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int u = cur[0];
            int d = cur[1];
            
            if (d > dist.get(u)) continue;
            if (u == 64) break;
            
            List<Integer> neighbors = board.getVisualNeighbors(u);
            
            for (int v : neighbors) {
                // CONSTRAINT: Only allow forward moves (v > u)
                // User requirement: "logic maju gaboleh 24 - 23 tapi 24-25"
                if (v <= u) continue;

                int target = v;
                // If neighbor is a connection start, we effectively land on the end
                if (conns.containsKey(v)) {
                    target = conns.get(v);
                }
                
                int newDist = d + 1;
                if (newDist < dist.get(target)) {
                    dist.put(target, newDist);
                    parent.put(target, u);
                    pq.add(new int[]{target, newDist});
                }
            }
        }
        
        List<Integer> path = new ArrayList<>();
        Integer curr = 64;
        if (dist.get(64) == Integer.MAX_VALUE) return path; // No path found

        while (curr != null && curr != startNode) {
            path.add(0, curr);
            curr = parent.get(curr);
        }
        return path;
    }
}
