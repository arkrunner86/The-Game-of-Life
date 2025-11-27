import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

public class ConwayMain extends JFrame {
    private Conway2DFixed game;
    private JPanel gridPanel;
    private Timer timer;
    private final int CELL_SIZE = 10;
    private JLabel statusLabel;
    
    // Generation tracking
    private int generationCount = 0;
    private byte[] previousState = null;
    private byte[] twoGenerationsAgo = null;
    private boolean isStable = false;
    private int stableAtGeneration = -1;
    
    public ConwayMain() {
        super("Conway's Game of Life");
        
        // Initialize game with random seed
        game = new Conway2DFixed(100, 40);
        game.setSeedCount(700);
        game.randomSeed();
        
        // Setup window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create grid panel
        gridPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGrid(g);
            }
        };
        gridPanel.setPreferredSize(new Dimension(
            game.getWidth() * CELL_SIZE, 
            game.getHeight() * CELL_SIZE
        ));
        gridPanel.setBackground(Color.WHITE);
        
        // Add mouse click to toggle cells
        gridPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX() / CELL_SIZE;
                int y = e.getY() / CELL_SIZE;
                if (x < game.getWidth() && y < game.getHeight()) {
                    int pos = y * game.getWidth() + x;
                    game.data[pos] = (byte)(game.data[pos] == 1 ? 0 : 1);
                    resetStabilityTracking();
                    gridPanel.repaint();
                    updateStatus();
                }
            }
        });
        
        add(new JScrollPane(gridPanel), BorderLayout.CENTER);
        
        // Create control panel
        JPanel controlPanel = new JPanel();

        JButton startStopBtn = new JButton("Start");
        JButton stepBtn = new JButton("Step");
        JButton resetBtn = new JButton("Reset");
        JButton clearBtn = new JButton("Clear");
        JButton toggleModeBtn = new JButton("Mode: Bounded");
        
        // Timer for animation
        timer = new Timer(100, e -> {
            game.iterate();
            generationCount++;
            checkStability();
            gridPanel.repaint();
            updateStatus();
            
            // Auto-stop if stable
            if (isStable) {
                timer.stop();
                startStopBtn.setText("Start");
            }
        });
        
        startStopBtn.addActionListener(e -> {
            if (timer.isRunning()) {
                timer.stop();
                startStopBtn.setText("Start");
            } else {
                timer.start();
                startStopBtn.setText("Stop");
            }
        });
        
        stepBtn.addActionListener(e -> {
            game.iterate();
            generationCount++;
            checkStability();
            gridPanel.repaint();
            updateStatus();
        });
        
        resetBtn.addActionListener(e -> {
            timer.stop();
            startStopBtn.setText("Start");
            game.clear();
            game.randomSeed();
            resetStabilityTracking();
            generationCount = 0;
            gridPanel.repaint();
            updateStatus();
        });
        
        clearBtn.addActionListener(e -> {
            timer.stop();
            startStopBtn.setText("Start");
            game.clear();
            resetStabilityTracking();
            generationCount = 0;
            gridPanel.repaint();
            updateStatus();
        });

        toggleModeBtn.addActionListener(e -> {
            game.toggleWrapperMode();
            if (game.isWrapperMode()) {
                toggleModeBtn.setText("Mode: Wrapper");
            } else {
                toggleModeBtn.setText("Mode: Bounded");
            }
            resetStabilityTracking();
            updateStatus();
        });

        controlPanel.add(startStopBtn);
        controlPanel.add(stepBtn);
        controlPanel.add(resetBtn);
        controlPanel.add(clearBtn);
        controlPanel.add(toggleModeBtn);
        
        add(controlPanel, BorderLayout.SOUTH);
        
        // Status bar
        statusLabel = new JLabel();
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        updateStatus();
        add(statusLabel, BorderLayout.NORTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private void checkStability() {
        byte[] currentState = game.getData();
        
        // Check if pattern is unchanged (still life)
        if (previousState != null && Arrays.equals(currentState, previousState)) {
            if (!isStable) {
                isStable = true;
                stableAtGeneration = generationCount;
            }
        }
        // Check if pattern oscillates with period 2 (blinker, etc.)
        else if (twoGenerationsAgo != null && Arrays.equals(currentState, twoGenerationsAgo)) {
            if (!isStable) {
                isStable = true;
                stableAtGeneration = generationCount;
            }
        }
        else {
            isStable = false;
        }
        
        // Update history
        twoGenerationsAgo = previousState;
        previousState = Arrays.copyOf(currentState, currentState.length);
    }
    
    private void resetStabilityTracking() {
        previousState = null;
        twoGenerationsAgo = null;
        isStable = false;
        stableAtGeneration = -1;
    }
    
    private void drawGrid(Graphics g) {
        byte[] data = game.getData();
        int width = game.getWidth();
        int height = game.getHeight();
        
        // Draw cells
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (data[y * width + x] == 1) {
                    g.setColor(Color.BLACK);
                    g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
        
        // Draw grid lines
        g.setColor(Color.LIGHT_GRAY);
        for (int x = 0; x <= width; x++) {
            g.drawLine(x * CELL_SIZE, 0, x * CELL_SIZE, height * CELL_SIZE);
        }
        for (int y = 0; y <= height; y++) {
            g.drawLine(0, y * CELL_SIZE, width * CELL_SIZE, y * CELL_SIZE);
        }
    }
    
    private void updateStatus() {
        String modeText = game.isWrapperMode() ? "Wrapper" : "Bounded";
        String statusText = "Mode: " + modeText +
                          "  |  Generation: " + generationCount +
                          "  |  Alive: " + game.countAlive() + " cells";

        if (isStable) {
            int changeCount = stableAtGeneration - 1;
            statusText += "  |  STABLE (stopped changing after " + changeCount + " iterations)";
        } else {
            statusText += "  |  Evolving...";
        }

        statusLabel.setText(statusText);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ConwayMain frame = new ConwayMain();
            frame.setVisible(true);
        });
    }
}