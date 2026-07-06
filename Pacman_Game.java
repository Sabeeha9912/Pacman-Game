import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;

public class Pacman_Game extends JFrame {

    public Pacman_Game() {
        add(new GameBoard());
        setTitle("Pacman");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(416, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Pacman_Game::new);
    }
}

// ================= SOUND =================
class SoundPlayer {

    private static Clip bg;

    public static void play(String f) {
        try {
            AudioInputStream s = AudioSystem.getAudioInputStream(new File(f));
            Clip c = AudioSystem.getClip();
            c.open(s);
            c.start();
        } catch (Exception e) {}
    }

    public static void playLoop(String f) {
        try {
            bg = AudioSystem.getClip();
            bg.open(AudioSystem.getAudioInputStream(new File(f)));
            bg.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {}
    }

    public static void stop() {
        if (bg != null) bg.stop();
    }
}

// ================= GAME BOARD =================
class GameBoard extends JPanel implements ActionListener {

    private Timer timer = new Timer(40, this);

    private boolean inGame, isOver, isWin;
    private int score, lives = 3;

    private int pacX, pacY, BS = 20;

    // ONLY PACMAN ANIMATION ADDED
    private int mouthAngle = 45;
    private boolean mouthClosing = true;

    private int[] gX, gY, gDX, gDY;

    private Image ghosts[] = new Image[4];

    private short[] screenData;

    private final short[] level = {
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,1,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,0,
            0,1,0,0,1,0,0,0,1,0,0,1,0,0,0,1,0,0,1,0,
            0,3,0,0,1,0,0,0,1,0,0,1,0,0,0,1,0,0,3,0,
            0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,
            0,1,0,0,1,0,1,0,0,0,0,0,0,1,0,1,0,0,1,0,
            0,1,1,1,1,0,1,1,1,0,0,1,1,1,0,1,1,1,1,0,
            0,0,0,0,1,0,0,0,2,0,0,2,0,0,0,1,0,0,0,0,
            0,0,0,0,1,0,2,2,2,2,2,2,2,2,0,1,0,0,0,0,
            0,0,0,0,1,0,2,0,0,0,0,0,0,2,0,1,0,0,0,0,
            0,2,2,2,1,2,2,0,0,0,0,0,0,2,2,1,2,2,2,0,
            0,0,0,0,1,0,2,0,0,0,0,0,0,2,0,1,0,0,0,0,
            0,0,0,0,1,0,2,2,2,2,2,2,2,2,0,1,0,0,0,0,
            0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,
            0,1,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,0,
            0,1,0,0,1,0,0,0,1,0,0,1,0,0,0,1,0,0,1,0,
            0,3,1,0,1,1,1,1,1,2,2,1,1,1,1,1,0,1,3,0,
            0,0,1,0,1,0,1,0,0,0,0,0,0,1,0,1,0,1,0,0,
            0,1,1,1,1,0,1,1,1,0,0,1,1,1,0,1,1,1,1,0,
            0,1,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0,1,0,
            0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    };

    public GameBoard() {

        for (int i = 0; i < 4; i++)
            ghosts[i] = new ImageIcon("ghost" + (i + 1) + ".png").getImage();

        init();

        addKeyListener(new TAdapter());
        setFocusable(true);
        setBackground(Color.BLACK);

        timer.start();
    }

    private void init() {
        screenData = level.clone();
        score = 0;
        lives = 3;
        isOver = isWin = inGame = false;
        reset();
    }

    private void reset() {
        pacX = 160;
        pacY = 320;

        gX = new int[]{360, 20, 360, 100};
        gY = new int[]{20, 400, 400, 200};
        gDX = new int[]{-2, 2, -2, 2};
        gDY = new int[]{0, 0, 0, 0};
    }

    // ONLY ADDED FOR PACMAN MOUTH
    private void animateMouth() {
        if (mouthClosing) {
            mouthAngle -= 5;
            if (mouthAngle <= 5) mouthClosing = false;
        } else {
            mouthAngle += 5;
            if (mouthAngle >= 45) mouthClosing = true;
        }
    }

    public void actionPerformed(ActionEvent e) {

        if (inGame && !isOver && !isWin) {
            animateMouth(); // only added
            moveGhosts();
            checkLogic();
        }

        repaint();
    }

    private void moveGhosts() {

        int[][] dirs = {{2,0},{-2,0},{0,2},{0,-2}};

        for (int i = 0; i < 4; i++) {

            if (gX[i] % BS == 0 && gY[i] % BS == 0) {

                ArrayList<Integer> v = new ArrayList<>();

                for (int d = 0; d < 4; d++)
                    if (clear(gX[i] + dirs[d][0], gY[i] + dirs[d][1]))
                        v.add(d);

                int p = v.get((int)(Math.random() * v.size()));

                gDX[i] = dirs[p][0];
                gDY[i] = dirs[p][1];
            }

            gX[i] += gDX[i];
            gY[i] += gDY[i];
        }
    }

    private boolean clear(int x, int y) {

        if (x < 0 || x >= 400 || y < 0 || y >= 440)
            return false;

        return level[(x / BS) + 20 * (y / BS)] != 0 &&
                level[((x + 19) / BS) + 20 * ((y + 19) / BS)] != 0;
    }

    private void checkLogic() {

        int p = (pacX / BS) + 20 * (pacY / BS);

        if (screenData[p] == 1 || screenData[p] == 3) {

            score += (screenData[p] == 1) ? 10 : 100;

            SoundPlayer.play(screenData[p] == 1 ? "munch.wav" : "cherry.wav");

            screenData[p] = 2;

            boolean dots = false;

            for (short s : screenData)
                if (s == 1 || s == 3) dots = true;

            if (!dots) {
                isWin = true;
                SoundPlayer.stop();
            }
        }

        Rectangle pr = new Rectangle(pacX + 4, pacY + 4, 12, 12);

        for (int i = 0; i < 4; i++) {

            if (pr.intersects(new Rectangle(gX[i] + 4, gY[i] + 4, 12, 12))) {

                if (--lives <= 0) {
                    isOver = true;
                    SoundPlayer.stop();
                } else {
                    reset();
                }

                SoundPlayer.play("death.wav");
            }
        }
    }

    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        if (isOver || isWin) {

            g2.setColor(isWin ? Color.GREEN : Color.RED);
            g2.setFont(new Font("Arial", 1, 40));
            g2.drawString(isWin ? "YOU WIN!" : "GAME OVER", 90, 240);

            g2.setColor(Color.WHITE);
            g2.drawString("Score: " + score, 110, 300);

        } else if (!inGame) {

            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", 1, 40));
            g2.drawString("PACMAN", 115, 240);

        } else {

            for (int i = 0; i < level.length; i++) {

                int x = (i % 20) * BS;
                int y = (i / 20) * BS;

                if (level[i] == 0) {
                    g2.setColor(new Color(40,40,80));
                    g2.fillRect(x, y, 20, 20);
                }
                else if (screenData[i] == 1) {
                    g2.setColor(Color.WHITE);
                    g2.fillRect(x + 9, y + 9, 2, 2);
                }
                else if (screenData[i] == 3) {
                    g2.setColor(Color.RED);
                    g2.fillOval(x + 5, y + 5, 10, 10);
                }
            }

            // ONLY PACMAN CHANGED
            g2.setColor(Color.YELLOW);
            g2.fillArc(pacX, pacY, 20, 20, mouthAngle, 360 - (mouthAngle * 2));

            for (int i = 0; i < 4; i++)
                g2.drawImage(ghosts[i], gX[i], gY[i], 20, 20, null);

            g2.setColor(Color.YELLOW);
            g2.drawString("Score: " + score + "  Lives: " + lives, 30, 500);
        }
    }

    private class TAdapter extends KeyAdapter {

        public void keyPressed(KeyEvent e) {

            int k = e.getKeyCode();

            if (!inGame && k == KeyEvent.VK_SPACE) {
                inGame = true;
                SoundPlayer.playLoop("background.wav");
            }

            if ((isOver || isWin) && k == KeyEvent.VK_R)
                init();

            if (k == KeyEvent.VK_LEFT && clear(pacX - BS, pacY)) pacX -= BS;
            if (k == KeyEvent.VK_RIGHT && clear(pacX + BS, pacY)) pacX += BS;
            if (k == KeyEvent.VK_UP && clear(pacX, pacY - BS)) pacY -= BS;
            if (k == KeyEvent.VK_DOWN && clear(pacX, pacY + BS)) pacY += BS;
        }
    }
}