
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;

public class TreeSimulator extends JPanel implements ActionListener {

    //(0: Durgun, 1: Rüzgarlı, 2: Solgun)
    int currentState = 0;
    double time = 0;

    List<Leaf> fallingLeaves = new ArrayList<>();
    boolean spawnLeaves = false;

    Bird aiBird = new Bird(-50, 150);

    class Leaf {

        double x, y, vx, vy;
        Color color;

        public Leaf(double x, double y) {
            this.x = x;
            this.y = y;
            this.vy = Math.random() * 2 + 1;
            this.vx = (Math.random() - .5) * 2;
            this.color = new Color(200 + (int) (Math.random() * 55), 100 + (int) (Math.random() * 50), 20, 200);
        }
    }

    class Bird {

        double x, y;
        double targetX = 370;
        double targetY = 150;

        public Bird(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void update(int state, double time) {
            double dx = targetX - this.x;
            double dy = targetY - this.y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist > 5) {
                double speed = 2;

                if (state == 1) { //Windy
                    speed = 0.8;
                    this.y += Math.sin(time * 5) * 2;
                    this.x -= 1.5;
                }

                this.x += (dx / dist) * speed;
                this.y += (dy / dist) * speed;
            } else {
                if (state == 1 || state == 2) {
                    this.y += Math.sin(time) * .5;
                }
            }

            if (this.x < -100) {
                this.x = -50;
            }
        }
    }

    public TreeSimulator() {
        Timer timer = new Timer(16, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        Random rand = new Random(12345);

        g2d.setColor(new Color(135, 206, 235));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int rootX = getWidth() / 2;
        int rootY = getHeight() - 50;

        double initialLength = 120.0;
        double initialAngle = -Math.PI / 2;

        drawBranch(g2d, rootX, rootY, initialLength, initialAngle, rand);

        g2d.setColor(new Color(34, 139, 34));
        g2d.fillRect(0, getHeight() - 50, getWidth(), 50);

        for (Leaf l : fallingLeaves) {
            g2d.setColor(l.color);
            g2d.fillOval((int) l.x - 5, (int) l.y - 5, 10, 10);
        }

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));

        g2d.drawLine((int) aiBird.x, (int) aiBird.y, (int) aiBird.x - 10, (int) aiBird.y - 10);
        g2d.drawLine((int) aiBird.x, (int) aiBird.y, (int) aiBird.x + 10, (int) aiBird.y - 10);

        spawnLeaves = false;
    }

    private void drawBranch(Graphics2D g2d, double x, double y, double length, double angle, Random rand) {

        if (length < 2) {
            return;
        }

        double currentAngle = angle;
        if (currentState == 1 || currentState == 2) {
            double sway = Math.sin(time + length) * .05;
            currentAngle += sway;
        }

        double endX = x + length * Math.cos(currentAngle);
        double endY = y + length * Math.sin(currentAngle);

        g2d.setStroke(new BasicStroke((float) (length / 10.0)));
        g2d.setColor(new Color(101, 67, 33));
        g2d.drawLine((int) x, (int) y, (int) endX, (int) endY);

        if (length < 15) {
            if (spawnLeaves) {
                fallingLeaves.add(new Leaf(endX, endY));
            }
            if (currentState != 2) {
                g2d.setColor(new Color(34, 139, 34, 200));
                g2d.fillOval((int) endX - 5, (int) endY - 5, 10, 10);
            }
        }

        double rightAngleOffset = .2 + (rand.nextDouble() * .4);
        double leftAngleOffset = .2 + (rand.nextDouble() * .4);

        double lengthFactor = .6 + (rand.nextDouble() * .2);

        drawBranch(g2d, endX, endY, length * lengthFactor, currentAngle + rightAngleOffset, rand);
        drawBranch(g2d, endX, endY, length * lengthFactor, currentAngle - leftAngleOffset, rand);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        time += 0.05;

        aiBird.update(currentState, time);

        for (Leaf l : fallingLeaves) {
            if (l.y < getHeight() - 55) {
                l.y += l.vy;

                l.x += l.vx + Math.sin(time + l.y / 50.0) * 1.5;
            }
        }

        repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Smart Tree Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        TreeSimulator sim = new TreeSimulator();

        JPanel controlPanel = new JPanel();
        JButton btnDurgun = new JButton("Still");
        JButton btnRuzgar = new JButton("Windy");
        JButton btnSolgun = new JButton("Autumn");

        btnDurgun.addActionListener(e -> {
            sim.currentState = 0;
            sim.fallingLeaves.clear();
        }
        );
        btnRuzgar.addActionListener(e -> {
            sim.currentState = 1;
            sim.fallingLeaves.clear();
        }
        );
        btnSolgun.addActionListener(e -> {
            if (sim.currentState != 2) {
                sim.currentState = 2;
                sim.fallingLeaves.clear();
                sim.spawnLeaves = true;
            }
        });

        controlPanel.add(btnDurgun);
        controlPanel.add(btnRuzgar);
        controlPanel.add(btnSolgun);

        frame.setLayout(new BorderLayout());
        frame.add(controlPanel, BorderLayout.NORTH);
        frame.add(sim, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
