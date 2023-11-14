package brickBreaker;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Gameplay extends JPanel implements KeyListener, ActionListener {

    private static final long serialVersionUID = 1L;
    private boolean play = false;
    private boolean gameover = false;
    private int score = 0;
    private int highScore = 0;
    private int totalBricks = 21;
    private Timer timer;
    private int delay = 8;
    private int playerX = 260; // Modified for a wider paddle
    private int ballposX = 50;
    private int ballposY = 250;
    private int ballXdir = -1;
    private int ballYdir = -2;
    private MapGenerator map;
    private int maxBricksHit = 1;
    private Color ballColor = new Color(255, 175, 175);
    private TrailEffect[] trailEffects;
    private Clip backgroundMusic;
    private Image backgroundImage;
    private Image paddleImage;
    private Image brickImage; // Add this line

    public Gameplay() {
        map = new MapGenerator(3, 7);
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        timer = new Timer(delay, this);
        timer.start();

        trailEffects = new TrailEffect[50];
        for (int i = 0; i < trailEffects.length; i++) {
            trailEffects[i] = new TrailEffect();
        }

        playBackgroundMusic("Sounds/mixkit-fun-and-games-6.wav");

        try {
            // Load the background image
            backgroundImage = ImageIO.read(new File("Images/forest2.jpg"));

            // Load the paddle image
            paddleImage = ImageIO.read(new File("Images/mf5m_83oh_120820.jpg"));

            // Load the brick image
            brickImage = ImageIO.read(new File("I/bricks.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playBackgroundMusic(String filePath) {
        try {
            File audioFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioStream);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Set a background color
        g.setColor(new Color(34, 139, 34)); // Dark green color
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw the background image
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        map.draw((Graphics2D) g);

        Font font = new Font("Serif", Font.BOLD, 25);
        g.setFont(font);
        g.setColor(Color.black);
        g.drawString("Score: " + score, 550, 30);
        g.drawString("High Score: " + highScore, 260, 30);

        g.setColor(Color.orange);
        g.fillRect(0, 0, 3, 592);
        g.fillRect(0, 0, 692, 3);
        g.fillRect(691, 0, 3, 592);

        // Draw the wooden paddle image
        g.drawImage(paddleImage, playerX, 550, 120, 8, this);

        for (TrailEffect trailEffect : trailEffects) {
            trailEffect.paint(g);
        }

        GradientPaint gradient = new GradientPaint(ballposX, ballposY, ballColor, ballposX + 20, ballposY + 20,
                Color.RED);
        ((Graphics2D) g).setPaint(gradient);
        g.fillOval(ballposX, ballposY, 20, 20);

        // Draw bricks using the brick image
        for (int i = 0; i < map.map.length; i++) {
            for (int j = 0; j < map.map[0].length; j++) {
                if (map.map[i][j] > 0) {
                    int brickX = j * map.brickWidth + 80;
                    int brickY = i * map.brickHeight + 50;

                    g.drawImage(brickImage, brickX, brickY, map.brickWidth, map.brickHeight, this);
                }
            }
        }

        if (totalBricks <= 0 || ballposY > 570) {
            play = false;
            ballXdir = 0;
            ballYdir = 0;

            if (score > highScore) {
                highScore = score;
                repaint();
            }

            g.setColor(Color.red);
            font = new Font("Serif", Font.BOLD, 25);
            g.setFont(font);

            if (totalBricks <= 0) {
                g.drawString("You Won! High Score: " + highScore, 200, 300);
            } else {
                g.drawString("Game Over, High Score: " + highScore, 190, 300);
            }

            g.drawString("Press Enter to Restart", 230, 350);
            gameover = true;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        timer.start();

        if (play) {
            for (int i = trailEffects.length - 1; i > 0; i--) {
                trailEffects[i].update(trailEffects[i - 1]);
            }
            trailEffects[0].update(ballposX, ballposY, ballColor);

            if (new Rectangle(ballposX, ballposY, 20, 20).intersects(new Rectangle(playerX, 550, 120, 8))) {
                ballYdir = -ballYdir;
            }

            A: for (int i = 0; i < map.map.length; i++) {  // Fixed loop condition
                for (int j = 0; j < map.map[0].length; j++) {
                    if (map.map[i][j] > 0) {
                        int brickX = j * map.brickWidth + 80;
                        int brickY = i * map.brickHeight + 50;
                        int brickWidth = map.brickWidth;
                        int brickHeight = map.brickHeight;

                        Rectangle rect = new Rectangle(brickX, brickY, brickWidth, brickHeight);
                        Rectangle ballRect = new Rectangle(ballposX, ballposY, 20, 20);
                        Rectangle brickRect = rect;

                        if (ballRect.intersects(brickRect)) {
                            map.reduceBrickOpacity(i, j);

                            if (map.brickOpacity[i][j] <= 0.0f) {
                                map.setBrickValue(0, i, j);
                                totalBricks--;
                                score += 5;

                                if (ballposX + 19 <= brickRect.x || ballposX + 1 >= brickRect.x + brickRect.width) {
                                    ballXdir = -ballXdir;
                                } else {
                                    ballYdir = -ballYdir;
                                }

                                maxBricksHit = Math.max(1, maxBricksHit - 1);
                            }

                            if (maxBricksHit <= 0) {
                                break A;
                            }
                        }
                    }
                }
            }

            ballposX += ballXdir;
            ballposY += ballYdir;

            if (ballposX < 0 || ballposX > 670) {
                ballXdir = -ballXdir;
            }
            if (ballposY < 0) {
                ballYdir = -ballYdir;
            }
        }

        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            moveRight();
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            moveLeft();
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (gameover) {
                ballposX = 50;
                ballposY = 250;
                ballXdir = -1;
                ballYdir = -2;
                playerX = 260;
                score = 0;
                totalBricks = 21;
                map = new MapGenerator(3, 7);
                play = true;
                maxBricksHit = 1;
                resetTrailEffects();
                gameover = false;
                repaint();
            } else {
                play = true;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub
    }

    public void moveRight() {
        play = true;
        if (playerX + 120 < 692) {
            playerX += 20;
        }
        repaint();
    }

    public void moveLeft() {
        play = true;
        if (playerX > 0) {
            playerX -= 20;
        }
        repaint();
    }

    private void resetTrailEffects() {
        for (TrailEffect trailEffect : trailEffects) {
            trailEffect.reset();
        }
    }

    private class TrailEffect {
        private int x;
        private int y;
        private Color color;

        public TrailEffect() {
            reset();
        }

        public void update(int nextX, int nextY, Color nextColor) {
            x = nextX;
            y = nextY;
            color = nextColor;
        }

        public void update(TrailEffect nextEffect) {
            x = nextEffect.x;
            y = nextEffect.y;
            color = nextEffect.color;
        }

        public void paint(Graphics g) {
            g.setColor(color);
            g.fillOval(x, y, 20, 20);
        }

        public void reset() {
            x = -1;
            y = -1;
            color = new Color(0, 0, 0, 0);
        }
    }
}
