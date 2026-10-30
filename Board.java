import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;
import javax.swing.JFrame;
import javax.swing.JPanel;

/** The window. It draws whatever state text it is given and reports key presses. */
class Board extends JPanel {
    private static final int CELL = 24, BAR = 32;
    private static final String LEGEND = "S speed    G ghost    C cut enemy    SPACE start    Arrows / WASD move";

    volatile Consumer<String> onKey = key -> { }; // ignores keys until connected
    private volatile String state = "||";

    Board(String title) {
        setPreferredSize(new Dimension(Game.WIDTH * CELL, Game.HEIGHT * CELL + 2 * BAR));
        setBackground(new Color(24, 24, 32));
        setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                String key = keyName(e.getKeyCode());
                if (key != null) onKey.accept(key);
            }
        });

        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        requestFocusInWindow();
    }

    void show(String newState) {
        state = newState;
        repaint();
    }

    private static String keyName(int code) {
        switch (code) {
            case KeyEvent.VK_UP:    case KeyEvent.VK_W: return "UP";
            case KeyEvent.VK_DOWN:  case KeyEvent.VK_S: return "DOWN";
            case KeyEvent.VK_LEFT:  case KeyEvent.VK_A: return "LEFT";
            case KeyEvent.VK_RIGHT: case KeyEvent.VK_D: return "RIGHT";
            case KeyEvent.VK_SPACE: return "SPACE";
            default: return null;
        }
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        String[] parts = state.split("\\|", 3);

        int width = Game.WIDTH * CELL, bottom = BAR + Game.HEIGHT * CELL;
        g.setClip(0, BAR, width, Game.HEIGHT * CELL); // a crashed head stays inside the field
        for (String cell : parts[2].split(",")) {
            if (!cell.isEmpty()) drawCell(g, cell.split(" "));
        }
        g.setClip(null);

        g.setColor(new Color(50, 50, 64));
        g.drawRect(0, BAR, width - 1, Game.HEIGHT * CELL - 1);
        g.setColor(Color.WHITE);
        drawCentered(g, parts[0], BAR - 10);
        g.setColor(Color.GRAY);
        drawCentered(g, LEGEND, bottom + BAR - 10);
        g.setColor(Color.WHITE);
        drawCentered(g, parts[1], BAR + Game.HEIGHT * CELL / 2);
        Toolkit.getDefaultToolkit().sync(); // keeps animation smooth on Linux
    }

    private void drawCell(Graphics g, String[] cell) {
        char code = cell[0].charAt(0);
        int x = Integer.parseInt(cell[1]) * CELL;
        int y = Integer.parseInt(cell[2]) * CELL + BAR;
        switch (code) {
            case 'A': g.setColor(new Color(120, 190, 255)); g.fillRect(x, y, CELL - 1, CELL - 1); break;
            case 'a': g.setColor(new Color(50, 120, 220));  g.fillRect(x, y, CELL - 1, CELL - 1); break;
            case 'B': g.setColor(new Color(255, 190, 110)); g.fillRect(x, y, CELL - 1, CELL - 1); break;
            case 'b': g.setColor(new Color(225, 120, 40));  g.fillRect(x, y, CELL - 1, CELL - 1); break;
            case 'R': g.setColor(new Color(240, 70, 90));   g.fillOval(x + 2, y + 2, CELL - 4, CELL - 4); break;
            default: // a power-up: a green tile with its letter
                g.setColor(new Color(70, 200, 120));
                g.fillRoundRect(x, y, CELL - 1, CELL - 1, 8, 8);
                g.setColor(Color.BLACK);
                g.drawString(String.valueOf(code), x + 7, y + 18);
        }
    }

    private void drawCentered(Graphics g, String text, int y) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, (Game.WIDTH * CELL - metrics.stringWidth(text)) / 2, y);
    }
}
