/**
 * Project: Solo Lab 7 Assignment
 * Purpose Details: Space game with random stars, spaceship image, blue score text,
 * asteroid sprite sheet obstacles, sound effects, shield, health, power-ups, timer, and levels.
 * Course: IST 242
 * Author: Aziz Abilkosimov
 * Date Developed: 04/29/2026
 * Last Date Changed: 05/03/2026
 * Rev: 6
 */

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class SpaceGame extends JFrame implements KeyListener {

    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;

    private static final int PLAYER_WIDTH = 80;
    private static final int PLAYER_HEIGHT = 50;

    private static final int METEOR_WIDTH = 45;
    private static final int METEOR_HEIGHT = 45;

    private static final int PROJECTILE_WIDTH = 5;
    private static final int PROJECTILE_HEIGHT = 12;

    private static final int PLAYER_SPEED = 8;
    private static final int PROJECTILE_SPEED = 10;
    private static final int SCORE_AMOUNT = 10;

    private Image shipImage;
    private BufferedImage asteroidSheet;
    private BufferedImage[] asteroidFrames = new BufferedImage[4];

    private JPanel gamePanel;
    private JLabel scoreLabel;
    private Timer timer;
    private Random random = new Random();

    private boolean isGameOver;
    private boolean isFiring;
    private boolean shieldActive;

    private boolean movingLeft;
    private boolean movingRight;

    private int playerX;
    private int playerY;

    private int score = 0;
    private int health = 100;
    private int level = 1;
    private int timeLeft = 60;
    private int timerTick = 0;

    private ArrayList<Point> projectiles = new ArrayList<>();
    private ArrayList<Meteor> meteors = new ArrayList<>();
    private ArrayList<Point> powerUps = new ArrayList<>();
    private ArrayList<Point> stars = new ArrayList<>();
    private ArrayList<Color> starColors = new ArrayList<>();

    private static class Meteor {
        int x;
        int y;
        int frame;

        Meteor(int x, int y, int frame) {
            this.x = x;
            this.y = y;
            this.frame = frame;
        }
    }

    public SpaceGame() {
        setTitle("Space Game - Aziz Abilkosimov");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        loadImages();
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

    private void loadImages() {
        try {
            shipImage = ImageIO.read(getClass().getResource("/resources/xwing.png"));

            asteroidSheet = ImageIO.read(getClass().getResource("/resources/asteroids.png"));

            int frameWidth = asteroidSheet.getWidth() / 4;
            int frameHeight = asteroidSheet.getHeight();

            for (int i = 0; i < 4; i++) {
                asteroidFrames[i] = asteroidSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            }

        } catch (Exception e) {
            System.out.println("Images not found. Make sure xwing.png and asteroids.png are inside src/resources.");
        }
    }

    private void createStars() {
        for (int i = 0; i < 80; i++) {
            stars.add(new Point(random.nextInt(WIDTH), random.nextInt(HEIGHT)));
            starColors.add(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
        }
    }

    private void restartGame() {
        score = 0;
        health = 100;
        level = 1;
        timeLeft = 60;
        timerTick = 0;

        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 45;

        meteors.clear();
        powerUps.clear();
        projectiles.clear();

        isGameOver = false;
        isFiring = false;
        shieldActive = false;

        movingLeft = false;
        movingRight = false;

        scoreLabel.setText("Score: " + score);
    }

    private void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        for (int i = 0; i < stars.size(); i++) {
            g.setColor(starColors.get(i));
            Point s = stars.get(i);
            g.fillOval(s.x, s.y, 3, 3);
        }

        if (shipImage != null) {
            g.drawImage(shipImage, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, null);
        } else {
            g.setColor(Color.BLUE);
            int[] xPoints = {playerX + PLAYER_WIDTH / 2, playerX, playerX + PLAYER_WIDTH};
            int[] yPoints = {playerY, playerY + PLAYER_HEIGHT, playerY + PLAYER_HEIGHT};
            g.fillPolygon(xPoints, yPoints, 3);
        }

        if (shieldActive) {
            g.setColor(Color.CYAN);
            g.drawOval(playerX - 10, playerY - 10, PLAYER_WIDTH + 20, PLAYER_HEIGHT + 20);
        }

        g.setColor(Color.GREEN);
        for (Point p : projectiles) {
            g.fillRect(p.x, p.y, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }

        for (Meteor m : meteors) {
            if (asteroidFrames[m.frame] != null) {
                g.drawImage(asteroidFrames[m.frame], m.x, m.y, METEOR_WIDTH, METEOR_HEIGHT, null);
            } else {
                g.setColor(Color.DARK_GRAY);
                g.fillOval(m.x, m.y, METEOR_WIDTH, METEOR_HEIGHT);
            }
        }

        g.setColor(Color.GREEN);
        for (Point p : powerUps) {
            g.fillOval(p.x, p.y, 20, 20);
            g.setColor(Color.WHITE);
            g.drawString("+", p.x + 6, p.y + 15);
            g.setColor(Color.GREEN);
        }

        g.setColor(Color.WHITE);
        g.drawString("Health: " + health, 10, 45);
        g.drawString("Level: " + level, 10, 65);
        g.drawString("Time: " + timeLeft, 10, 85);

        if (shieldActive) {
            g.setColor(Color.CYAN);
            g.drawString("Shield: ON", 10, 105);
        } else {
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("Press S for Shield", 10, 105);
        }

        if (isGameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Game Over!", 150, 240);
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("Press R to Restart", 155, 270);
        }
    }

    private void update() {
        for (int i = 0; i < stars.size(); i++) {
            stars.get(i).y += 2;

            if (stars.get(i).y > HEIGHT) {
                stars.get(i).y = 0;
                stars.get(i).x = random.nextInt(WIDTH);
            }
        }

        if (movingLeft && playerX > 0) {
            playerX -= PLAYER_SPEED;
        }

        if (movingRight && playerX < WIDTH - PLAYER_WIDTH) {
            playerX += PLAYER_SPEED;
        }

        timerTick++;

        if (timerTick % 50 == 0) {
            timeLeft--;
        }

        if (timeLeft <= 0) {
            isGameOver = true;
        }

        level = 1 + score / 50;

        if (level > 5) {
            level = 5;
        }

        int speed = 2 + (level * 2);
        double spawnChance = 0.01 + (level * 0.008);

        for (int i = 0; i < meteors.size(); i++) {
            meteors.get(i).y += speed;

            if (meteors.get(i).y > HEIGHT) {
                meteors.remove(i);
                i--;
            }
        }

        if (Math.random() < spawnChance) {
            meteors.add(new Meteor(random.nextInt(WIDTH - METEOR_WIDTH), 0, random.nextInt(4)));
        }

        double powerUpChance = 0.006 - (level * 0.001);

        if (powerUpChance < 0.001) {
            powerUpChance = 0.001;
        }

        if (Math.random() < powerUpChance) {
            powerUps.add(new Point(random.nextInt(WIDTH - 20), 0));
        }

        for (int i = 0; i < powerUps.size(); i++) {
            powerUps.get(i).y += 3;

            if (powerUps.get(i).y > HEIGHT) {
                powerUps.remove(i);
                i--;
            }
        }

        for (int i = 0; i < projectiles.size(); i++) {
            projectiles.get(i).y -= PROJECTILE_SPEED;

            if (projectiles.get(i).y < 0) {
                projectiles.remove(i);
                i--;
            }
        }

        Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

        for (int i = 0; i < meteors.size(); i++) {
            Rectangle mRect = new Rectangle(meteors.get(i).x, meteors.get(i).y, METEOR_WIDTH, METEOR_HEIGHT);

            if (playerRect.intersects(mRect)) {
                meteors.remove(i);

                if (!shieldActive) {
                    health -= 20;
                    playSound("collision.wav");
                }

                if (health <= 0) {
                    isGameOver = true;
                }

                break;
            }
        }

        for (int i = 0; i < projectiles.size(); i++) {
            Rectangle projectileRect = new Rectangle(projectiles.get(i).x, projectiles.get(i).y, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);

            for (int j = 0; j < meteors.size(); j++) {
                Rectangle mRect = new Rectangle(meteors.get(j).x, meteors.get(j).y, METEOR_WIDTH, METEOR_HEIGHT);

                if (projectileRect.intersects(mRect)) {
                    meteors.remove(j);
                    projectiles.remove(i);
                    score += SCORE_AMOUNT;
                    i--;
                    break;
                }
            }
        }

        Rectangle playerPowerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

        for (int i = 0; i < powerUps.size(); i++) {
            Rectangle powerRect = new Rectangle(powerUps.get(i).x, powerUps.get(i).y, 20, 20);

            if (playerPowerRect.intersects(powerRect)) {
                powerUps.remove(i);
                health += 20;

                if (health > 100) {
                    health = 100;
                }

                break;
            }
        }

        scoreLabel.setText("Score: " + score);
    }

    private void playSound(String fileName) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(
                    getClass().getResource("/resources/" + fileName)
            ));
            clip.start();
        } catch (Exception e) {
            System.out.println("Sound error: " + fileName);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (isGameOver && key == KeyEvent.VK_R) {
            restartGame();
            return;
        }

        if (key == KeyEvent.VK_LEFT) {
            movingLeft = true;
        }

        if (key == KeyEvent.VK_RIGHT) {
            movingRight = true;
        }

        if (key == KeyEvent.VK_SPACE && !isFiring) {
            isFiring = true;

            projectiles.add(new Point(playerX + 5, playerY + 8));
            projectiles.add(new Point(playerX + PLAYER_WIDTH - 10, playerY + 8));

            playSound("fire.wav");

            new Thread(() -> {
                try {
                    Thread.sleep(400);
                    isFiring = false;
                } catch (Exception ignored) {
                }
            }).start();
        }

        if (key == KeyEvent.VK_S) {
            shieldActive = true;

            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    shieldActive = false;
                } catch (Exception ignored) {
                }
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
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SpaceGame().setVisible(true));
    }
}