import java.awt.Color;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

public class GameController {
    private BoardGraph board;
    private GamePanel panel;
    private List<Player> allPlayers;
    private LinkedList<Player> turnQueue;
    private Timer clientRouletteTimer;

    private boolean isGameStarted = false;
    private boolean isAnimating = false;
    private int finalDiceValue;
    private boolean moveForward;

    private NetworkManager net;

    public GameController(BoardGraph board, GamePanel panel, List<Player> players) {
        this.board = board; this.panel = panel; this.allPlayers = players;
        this.turnQueue = new LinkedList<>();
    }

    public void setNetworkManager(NetworkManager net) {
        this.net = net;
        if (net != null) {
            net.setOnMessageReceived(this::handleNetworkMessage);
        }
    }

    private void handleNetworkMessage(String msg) {
        if (msg.startsWith("DICE_ROLLED")) {
            int val = Integer.parseInt(msg.split(" ")[1]);
            this.finalDiceValue = val;

            SwingUtilities.invokeLater(() -> startDiceAnimation(val));
        } else if (msg.startsWith("REQUEST_ROLL")) {

            if (net.isHost()) {
                SwingUtilities.invokeLater(this::startTurn);
            }
        } else if (msg.startsWith("ORDER_RESULT")) {
            int winnerIdx = Integer.parseInt(msg.split(" ")[1]);
            if (clientRouletteTimer != null && clientRouletteTimer.isRunning()) {
                clientRouletteTimer.stop();
            }
            panel.setOrderAnimState(true, winnerIdx);
            Timer pause = new Timer(1500, evt -> setupQueue(winnerIdx));
            pause.setRepeats(false); pause.start();
        } else if (msg.startsWith("CHAT")) {
            String chatMsg = msg.substring(5);
            SwingUtilities.invokeLater(() -> SnakeLadderGame.log(chatMsg));
        }
    }

    public void startOrderRoulette() {
        panel.setStatus("Determining who goes first...");

        if (net != null && !net.isHost()) {
            startClientRouletteAnim();
            return;
        }

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

                if (net != null && net.isHost()) {
                    net.sendMessage("ORDER_RESULT " + winnerIdx);
                }

                panel.setOrderAnimState(true, winnerIdx);
                Timer pause = new Timer(1500, evt -> setupQueue(winnerIdx));
                pause.setRepeats(false); pause.start();
            }
        });
        rouletteTimer.start();
    }

    private void startClientRouletteAnim() {
        final int[] currentIndex = {0};
        clientRouletteTimer = new Timer(100, e -> {
            panel.setOrderAnimState(true, currentIndex[0]);
            currentIndex[0] = (currentIndex[0] + 1) % allPlayers.size();
        });
        clientRouletteTimer.start();
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
        DigiDialog.showMessage(panel, "BATTLE START", first.name + " starts first!");



        checkBotTurn();
    }

    private void checkBotTurn() {
        Player current = turnQueue.peek();
        if (current != null && current.isBot) {
            panel.setStatus("CPU " + current.name + " is thinking...");

            if (net != null && !net.isHost()) return;

            Timer botTimer = new Timer(1500, e -> startTurn());
            botTimer.setRepeats(false);
            botTimer.start();
        }
    }

    public void startTurn() {
        if (!isGameStarted || isAnimating) return;


        if (net != null && !net.isHost()) {
            net.sendMessage("REQUEST_ROLL");
            return;
        }

        Player currentP = turnQueue.peek();
        if (currentP.position >= 64) return;
        isAnimating = true;
        startDiceAnimation(-1);
    }

    private void startDiceAnimation(int forcedValue) {
        SoundManager.play(SoundManager.DICE);


        Player currentP = turnQueue.peek();
        int diceCount = 1;

        int bonus = 0;

        if (forcedValue == -1) {
            int roll1 = (int)(Math.random() * 6) + 1;
            int roll2 = (int)(Math.random() * 6) + 1;
            finalDiceValue = (diceCount == 2) ? (roll1 + roll2) : roll1;
            SnakeLadderGame.log(currentP.name + " rolled " + finalDiceValue);


            if (net != null && net.isHost()) {
                net.sendMessage("DICE_ROLLED " + finalDiceValue);
            }
        } else {
            finalDiceValue = forcedValue;
        }




        panel.throwDice(finalDiceValue);


        Timer checkTimer = new Timer(100, null);
        checkTimer.addActionListener(e -> {
            if (panel.isDiceAnimationComplete()) {
                ((Timer)e.getSource()).stop();


                Timer pause = new Timer(500, evt -> {
                    panel.setDiceState(false, 1);
                    startChaosSlotMachine();
                });
                pause.setRepeats(false);
                pause.start();
            }
        });
        checkTimer.start();
    }

    private void startChaosSlotMachine() {
        SoundManager.play(SoundManager.SLOT);
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



        boolean isMovingForward = moveForward;

        if (currentP.primePowerMode && moveForward) {
            isMovingForward = true;
            List<Integer> path = getShortestPathToWin(simPos);
            int moves = Math.min(finalDiceValue, path.size());
            for (int i = 0; i < moves; i++) {
                steps.add(path.get(i));
            }

            panel.setStatus("Prime Power Active: Moving Smart!");
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

                Stack<Integer> tempHist = new Stack<>();
                tempHist.addAll(currentP.moveHistory);

                for (int i = 0; i < finalDiceValue; i++) {
                    if (tempHist.size() > 1) {
                        tempHist.pop();
                        steps.add(tempHist.peek());
                    } else {
                        steps.add(0);
                    }
                }
                panel.setStatus("Walking Backwards...");
            }
        }


        final boolean finalIsMovingForward = isMovingForward;
        final int[] stepIdx = {0};

        Timer moveTimer = new Timer(300, e -> {
            if (stepIdx[0] < steps.size()) {
                int nextPos = steps.get(stepIdx[0]);
                currentP.position = nextPos;


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

    private void resetGame() {

        for (Player p : allPlayers) {
            p.position = 0;
            p.score = 0;
            p.moveHistory.clear();
            p.moveHistory.push(0);
            p.primePowerMode = false;
        }


        board.resetConnections();


        panel.hideOverlay();
        panel.refresh();


        startOrderRoulette();
    }

    private void finishTurn(Player p) {
        isAnimating = false;

        Tile currentTile = board.getTile(p.position);
        if (currentTile != null && currentTile.hasScore()) {
            int score = currentTile.collectScore();
            p.addScore(score);
            panel.spawnParticlesAtPlayer(p, Color.YELLOW, 20);
            SnakeLadderGame.log(p.name + " collected " + score + " data points!");




            SnakeLadderGame.updateScoreStats(p.name, p.getScore());

            SoundManager.play(SoundManager.MAGIC);
            DigiDialog.showMessage(panel, "DATA FRAGMENT FOUND", "You found a treasure! +" + score + " Points!");
        }


        Map<Integer, Integer> conns = board.getConnections();
        if (conns.containsKey(p.position)) {
            int dest = conns.get(p.position);
            if (dest > p.position) {
                if (p.primePowerMode) {
                    SoundManager.play(SoundManager.LADDER);
                    p.position = dest;
                    p.moveHistory.push(dest);
                    panel.refresh();
                    SnakeLadderGame.log(p.name + " used a Ladder to Tile " + dest);
                    DigiDialog.showMessage(panel, "PRIME POWER ACTIVE", "LADDER! Up to " + dest);
                } else {
                    SnakeLadderGame.log(p.name + " found a Ladder but lacks Prime Power.");
                }
            } else {
                p.position = dest;
                p.moveHistory.push(dest);
                panel.refresh();
                SnakeLadderGame.log("WARNING! " + p.name + " hit a Virus! Dropped to Tile " + dest);
                DigiDialog.showMessage(panel, "VIRUS INFECTION", "OH NO! A Virus infected your data! \nSliding down to " + dest);
            }
        }

        if (p.position == 64) {

            SoundManager.play(SoundManager.WIN);
            SnakeLadderGame.updateWinStats(p.name);

            int choice = DigiDialog.showConfirm(panel, "WINNER!",
                    "CONGRATULATIONS " + p.name + "!\nYou reached the Digital World Core!\nScore: " + p.getScore() + "\nPlay again?");
            if (choice == JOptionPane.YES_OPTION) {
                resetGame();
            } else {
                System.exit(0);
            }
            return;
        }


        if (isPrime(p.position)) {
            SoundManager.play(SoundManager.MAGIC);
            p.primePowerMode = true;
            DigiDialog.showMessage(panel, "PRIME DETECTED", "PRIME NUMBER (" + p.position + ")! \nPrime Power Activated! You can use Ladders next turn!");
        } else {
            p.primePowerMode = false;
        }


        boolean isStarTile = (p.position > 0 && p.position % 5 == 0);

        if (isStarTile) {
            Player samePlayer = turnQueue.poll();
            turnQueue.addFirst(samePlayer);
            DigiDialog.showMessage(panel, "STAR TILE EVENT", "⭐ STAR TILE! ⭐\n" + p.name + " gets an EXTRA TURN!");
            panel.setStatus("Extra Turn for " + p.name + "!");
        } else {
            Player donePlayer = turnQueue.poll();
            turnQueue.offer(donePlayer);
            Player nextPlayer = turnQueue.peek();
            panel.setStatus("Click Roll for " + nextPlayer.name);
        }
        panel.refresh();

        checkBotTurn();
    }


    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    private List<Integer> getShortestPathToWin(int startNode) {
        List<Integer> path = new ArrayList<>();
        int current = startNode;
        Map<Integer, Integer> conns = board.getConnections();
        int safetyLimit = 200;

        while (current < 64 && safetyLimit-- > 0) {
            int next = current + 1;
            path.add(next);
            if (conns.containsKey(next)) {
                int dest = conns.get(next);
                if (dest > next) {
                    path.add(dest);
                    current = dest;
                } else {
                    current = next;
                }
            } else {
                current = next;
            }
        }
        return path;
    }
}
