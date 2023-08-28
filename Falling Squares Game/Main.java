import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main extends JFrame {
    private static final int PANEL_WIDTH = 400;
    private static final int PANEL_HEIGHT = 500;
    private static final int SQUARE_SIZE = 50;
    private static int squareSpeed = 3;
    private int creationInterval = 1000;
    private static double WINNING_THRESHOLD = 0.75;
    private static int GAME_DURATION = 30000;

    private final JPanel panel;
    private final JLabel scoreLabel, timerLabel;
    private long startTime;
    private final Timer creationTimer, gameTimer;
    private final List<Square> squares;
    private int destroyedSquares, missedSquares;
    private Color color, backgroundColor;

    private static class Square {
        private int x;
        private int y;
        private Color color;

        public Square(int x, int y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }

        public void move() {
            y += squareSpeed;
        }

        public boolean isClicked(int clickX, int clickY) {
            Rectangle bounds = getBounds();
            return bounds.contains(clickX, clickY);
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, SQUARE_SIZE, SQUARE_SIZE);
        }
    }

    public Main() {
        setTitle("Falling Squares - The Game");
        setSize(PANEL_WIDTH, PANEL_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        color = Color.BLUE;
        backgroundColor = Color.YELLOW;

        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(backgroundColor);
                g.fillRect(0, 0, getWidth(), getHeight());
                for (Square square : squares) {
                    g.setColor(square.color);
                    g.fillRect(square.x, square.y, SQUARE_SIZE, SQUARE_SIZE);
                }
            }
        };

        scoreLabel = new JLabel("Score: 0%");
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);

        add(panel, BorderLayout.CENTER);
        add(scoreLabel, BorderLayout.SOUTH);

        timerLabel = new JLabel("Time: 0s");
        timerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        add(timerLabel, BorderLayout.NORTH);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                int clickX = e.getX();
                int clickY = e.getY();
                for (int i = squares.size() - 1; i >= 0; i--) {
                    Square square = squares.get(i);
                    if (square.isClicked(clickX, clickY)) {
                        destroyedSquares++;
                        squares.remove(i);
                        updateScore();
                        break;
                    }
                }
            }
        });

        creationTimer = new Timer(creationInterval, e -> {
            createSquare();
        });

        gameTimer = new Timer(GAME_DURATION, e -> endGame());
        squares = new ArrayList<>();
    }

    private void createSquare() {
        int x = new Random().nextInt((int) (PANEL_WIDTH - 1.5*SQUARE_SIZE));
        squares.add(new Square(x, 0, color));
        panel.repaint();
    }


    private void updateScore() {
        int percentage;

        if (missedSquares != 0) {
            int sumSquares = missedSquares+destroyedSquares;
            double score = (double) destroyedSquares/sumSquares;
            percentage = (int) (score * 100);
        } else {
            percentage = 100;
        }
        scoreLabel.setText("Score: " + percentage + "%");
    }

    private void endGame() {
        creationTimer.stop();
        gameTimer.stop();
        panel.setEnabled(false);
        squareSpeed = 0;
        GAME_DURATION = 0;
        int sumSquares = missedSquares+destroyedSquares;
        double score = (double) destroyedSquares/sumSquares;
        if (score >= WINNING_THRESHOLD) {
            JOptionPane.showMessageDialog(this, "Congratulations! You won!");
            System.exit(0);
        } else {
            JOptionPane.showMessageDialog(this, "Game over! You lost.");
            System.exit(0);
        }
    }

    public void startGame() {
        creationTimer.start();
        gameTimer.setRepeats(false);
        gameTimer.setInitialDelay(GAME_DURATION);
        gameTimer.start();
        panel.setEnabled(true);
        panel.requestFocus();
        destroyedSquares = 0;
        missedSquares = 0;

        startTime = GAME_DURATION + 1000 + System.currentTimeMillis();

        Timer levelIncrease = new Timer(GAME_DURATION/11, f -> {
            if (squareSpeed < 10 && squareSpeed >= 1) {
                squareSpeed += 0.1;
            }
            if (creationInterval > 200) {
                creationInterval -= 100;
                creationTimer.setDelay(creationInterval);
            }
        });
        Timer gameLoop = new Timer(10, e -> {
            for (Square square : squares) {
                square.move();
                if (square.y > PANEL_HEIGHT-2*SQUARE_SIZE) {
                    squares.remove(square);
                    missedSquares++;
                    updateScore();
                    break;
                }
            }
            long elapsedTime = startTime - System.currentTimeMillis();
            if (elapsedTime/1000 >= 0) {
                timerLabel.setText("Time: " + (elapsedTime / 1000) + "s");
            }

            panel.repaint();
        });
        gameLoop.start();
        levelIncrease.start();
    }

    private void showMainMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        JMenuItem playMenuItem = new JMenuItem("Play");
        JMenuItem settingsMenuItem = new JMenuItem("Settings");
        menu.add(playMenuItem);
        menu.add(settingsMenuItem);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        playMenuItem.addActionListener(e -> {
            menuBar.setVisible(false);
            startGame();
        });

        settingsMenuItem.addActionListener(e -> showSettingsDialog());
    }

    private void showSettingsDialog() {
        JDialog dialog = new JDialog(this, "Settings", true);
        dialog.setLayout(new GridLayout(6, 2));
        dialog.setSize(800, 400);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JLabel winningThresholdLabel = new JLabel("Winning Threshold:");
        JSlider winningThresholdSlider = new JSlider(0, 100, (int) (WINNING_THRESHOLD * 100));
        winningThresholdSlider.setMajorTickSpacing(10);
        winningThresholdSlider.setPaintTicks(true);
        winningThresholdSlider.setPaintLabels(true);

        winningThresholdSlider.addChangeListener(e -> {
            int value = winningThresholdSlider.getValue();
            WINNING_THRESHOLD = value / 100.0;
        });

        JLabel gameDurationLabel = new JLabel("Game Duration [seconds]:");
        JSlider gameDurationSlider = new JSlider(JSlider.HORIZONTAL, 10, 60, GAME_DURATION/1000);
        gameDurationSlider.setMajorTickSpacing(10);
        gameDurationSlider.setPaintTicks(true);
        gameDurationSlider.setPaintLabels(true);
        gameDurationSlider.addChangeListener(e -> {
            GAME_DURATION = gameDurationSlider.getValue()*1000;
            gameTimer.setInitialDelay(GAME_DURATION);
        });

        JLabel squareSpeedLabel = new JLabel("Square Speed Levels:");
        JSlider squareSpeedSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, squareSpeed);
        squareSpeedSlider.setMajorTickSpacing(1);
        squareSpeedSlider.setPaintTicks(true);
        squareSpeedSlider.setPaintLabels(true);
        squareSpeedSlider.addChangeListener(e -> squareSpeed = squareSpeedSlider.getValue());

        JLabel creationIntervalLabel = new JLabel("Creation Interval [milliseconds]:");
        JSlider creationIntervalSlider = new JSlider(JSlider.HORIZONTAL, 200, 2000, creationInterval);
        creationIntervalSlider.setMajorTickSpacing(200);
        creationIntervalSlider.setPaintTicks(true);
        creationIntervalSlider.setPaintLabels(true);
        creationIntervalSlider.addChangeListener(e -> {
            creationInterval = creationIntervalSlider.getValue();
            creationTimer.setDelay(creationInterval);
        });

        JLabel squareColorLabel = new JLabel("Square Color:");
        JButton squareColorButton = new JButton("Choose Color");
        squareColorButton.addActionListener(e -> {
            color = JColorChooser.showDialog(dialog, "Choose Square Color", panel.getBackground());
            if (color != null) {
                squareColorButton.setBackground(color);
            }
        });

        JLabel backgroundColorLabel = new JLabel("Background Color:");
        JButton backgroundColorButton = new JButton("Choose Color");
        backgroundColorButton.addActionListener(e -> {
            backgroundColor = JColorChooser.showDialog(dialog, "Choose Background Color", panel.getBackground());
            if (backgroundColor != null) {
                backgroundColorButton.setBackground(backgroundColor);
                panel.repaint();
            } else {
                backgroundColorButton.setBackground(Color.WHITE);
                panel.repaint();
            }
        });

        dialog.add(winningThresholdLabel);
        dialog.add(winningThresholdSlider);
        dialog.add(gameDurationLabel);
        dialog.add(gameDurationSlider);
        dialog.add(squareSpeedLabel);
        dialog.add(squareSpeedSlider);
        dialog.add(creationIntervalLabel);
        dialog.add(creationIntervalSlider);
        dialog.add(squareColorLabel);
        dialog.add(squareColorButton);
        dialog.add(backgroundColorLabel);
        dialog.add(backgroundColorButton);

        dialog.setVisible(true);
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Main game = new Main();
            game.setVisible(true);
            game.showMainMenu();
        });
    }
}