/**
 * Project: Solo Lab 7 Assignment
 * Purpose Details: Space game with random stars, spaceship player, blue score text,
 * meteor obstacles, sound effects, shield, health, power-ups, timer, and levels.
 * Course: IST 242
 * Author: Aziz Abilkosimov
 * Date Developed: 04/29/2026
 * Last Date Changed: 04/29/2026
 * Rev: 3
 */

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class SpaceGame extends JFrame implements KeyListener {

    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;

    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 50;

    private static final int METEOR_WIDTH = 35;
    private static final int METEOR_HEIGHT = 35;

    private static final int PROJECTILE_WIDTH = 5;
    private static final int PROJECTILE_HEIGHT = 10;

    private static final int PLAYER_SPEED = 8;
    private static final int PROJECTILE_SPEED = 10;
    private static final int SCORE_AMOUNT = 10;

    private JPanel gamePanel;
    private JLabel scoreLabel;
    private Timer timer;
    private Random random = new Random();

    private boolean isGameOver;
    private boolean isProjectileVisible;
    private boolean isFiring;
    private boolean shieldActive;

    // ✅ SMOOTH MOVEMENT FLAGS
    private boolean movingLeft;
    private boolean movingRight;

    private int playerX;
    private int playerY;
    private int projectileX;
    private int projectileY;

    private int score = 0;
    private int health = 100;
    private int level = 1;
    private int timeLeft = 60;
    private int timerTick = 0;

    private ArrayList<Point> meteors = new ArrayList<>();
    private ArrayList<Point> powerUps = new ArrayList<>();
    private ArrayList<Point> stars = new ArrayList<>();
    private ArrayList<Color> starColors = new ArrayList<>();

    public SpaceGame() {
        setTitle("Space Game - Aziz Abilkosimov");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        createStars();

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };

        gamePanel.setLayout(null);

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setBounds(10, 10, 300, 20);
        scoreLabel.setForeground(Color.BLUE);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gamePanel.add(scoreLabel);

        add(gamePanel);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);

        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 45;

        timer = new Timer(20, e -> {
            if (!isGameOver) {
                update();
                gamePanel.repaint();
            }
        });

        timer.start();
    }

    private void createStars() {
        for (int i = 0; i < 80; i++) {
            stars.add(new Point(random.nextInt(WIDTH), random.nextInt(HEIGHT)));
            starColors.add(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
        }
    }

    private void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        for (int i = 0; i < stars.size(); i++) {
            g.setColor(starColors.get(i));
            Point s = stars.get(i);
            g.fillOval(s.x, s.y, 3, 3);
        }

        // Player
        g.setColor(Color.BLUE);
        int[] xPoints = {playerX + PLAYER_WIDTH / 2, playerX, playerX + PLAYER_WIDTH};
        int[] yPoints = {playerY, playerY + PLAYER_HEIGHT, playerY + PLAYER_HEIGHT};
        g.fillPolygon(xPoints, yPoints, 3);

        if (shieldActive) {
            g.setColor(Color.CYAN);
            g.drawOval(playerX - 10, playerY - 10, PLAYER_WIDTH + 20, PLAYER_HEIGHT + 20);
        }

        // Projectile
        if (isProjectileVisible) {
            g.setColor(Color.GREEN);
            g.fillRect(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }

        // Meteors
        for (Point m : meteors) {
            g.setColor(Color.DARK_GRAY);
            g.fillOval(m.x, m.y, METEOR_WIDTH, METEOR_HEIGHT);
        }

        // Power-ups
        g.setColor(Color.GREEN);
        for (Point p : powerUps) {
            g.fillOval(p.x, p.y, 20, 20);
        }

        // UI
        g.setColor(Color.WHITE);
        g.drawString("Health: " + health, 10, 45);
        g.drawString("Level: " + level, 10, 65);
        g.drawString("Time: " + timeLeft, 10, 85);

        if (isGameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Game Over!", 150, 250);
        }
    }

    private void update() {

        // ✅ SMOOTH MOVEMENT HERE
        if (movingLeft && playerX > 0) {
            playerX -= PLAYER_SPEED;
        }
        if (movingRight && playerX < WIDTH - PLAYER_WIDTH) {
            playerX += PLAYER_SPEED;
        }

        timerTick++;
        if (timerTick % 50 == 0) timeLeft--;

        if (timeLeft <= 0) isGameOver = true;

        level = 1 + score / 50;
        int speed = 3 + level;

        // Meteors
        for (int i = 0; i < meteors.size(); i++) {
            meteors.get(i).y += speed;
            if (meteors.get(i).y > HEIGHT) {
                meteors.remove(i);
                i--;
            }
        }

        if (Math.random() < 0.02) {
            meteors.add(new Point(random.nextInt(WIDTH - 40), 0));
        }

        // Projectile
        if (isProjectileVisible) {
            projectileY -= PROJECTILE_SPEED;
            if (projectileY < 0) isProjectileVisible = false;
        }

        // Collision
        Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

        for (int i = 0; i < meteors.size(); i++) {
            Rectangle mRect = new Rectangle(meteors.get(i).x, meteors.get(i).y, METEOR_WIDTH, METEOR_HEIGHT);

            if (playerRect.intersects(mRect)) {
                meteors.remove(i);
                if (!shieldActive) health -= 20;
                if (health <= 0) isGameOver = true;
                break;
            }
        }

        scoreLabel.setText("Score: " + score);
    }

    private void playSound(String file) {
        try {
            File f = new File(file);
            if (f.exists()) {
                Clip clip = AudioSystem.getClip();
                clip.open(AudioSystem.getAudioInputStream(f));
                clip.start();
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {
            movingLeft = true;
        }
        if (key == KeyEvent.VK_RIGHT) {
            movingRight = true;
        }

        if (key == KeyEvent.VK_SPACE && !isFiring) {
            isFiring = true;
            projectileX = playerX + 22;
            projectileY = playerY;
            isProjectileVisible = true;
            playSound("fire.wav");

            new Thread(() -> {
                try {
                    Thread.sleep(400);
                    isFiring = false;
                } catch (Exception ignored) {}
            }).start();
        }

        if (key == KeyEvent.VK_S) {
            shieldActive = true;
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    shieldActive = false;
                } catch (Exception ignored) {}
            }).start();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {
            movingLeft = false;
        }
        if (key == KeyEvent.VK_RIGHT) {
            movingRight = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SpaceGame().setVisible(true));
    }
}