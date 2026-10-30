import java.awt.Point;
import java.util.Random;

/** The rules. Only the host runs this; state() turns it into text for both screens. */
class Game {
    static final int WIDTH = 30, HEIGHT = 24;
    private static final double POWER_CHANCE = 0.01; // per frame, about every 6 seconds
    private static final int POWER_LIFETIME = 130;   // frames, about 8 seconds

    private final Random rnd = new Random();
    private Snake[] snakes;
    private Point reward, powerSpot;
    private PowerUp power;
    private int frame, powerLife;
    private boolean running;
    private String message = "Press SPACE to start";

    Game() {
        reset();
    }

    private void reset() {
        snakes = new Snake[] {
            new Snake("Blue", 'A', 6, HEIGHT / 2, Direction.RIGHT),
            new Snake("Orange", 'B', WIDTH - 7, HEIGHT / 2, Direction.LEFT)
        };
        power = null;
        reward = freeCell();
    }

    /** A key press from a player: a Direction name, or SPACE. */
    synchronized void handle(int player, String key) {
        if (!key.equals("SPACE")) {
            snakes[player].turn(Direction.valueOf(key));
        } else if (!running) {
            reset();
            running = true;
            message = "";
        }
    }

    synchronized void tick() {
        if (!running) return;
        frame++;
        for (Snake s : snakes) {
            if (s.movesOn(frame)) s.move();
            s.wearOffEffects();
        }
        // Everyone moves first, then we check, so a head-on crash is fair.
        Snake a = snakes[0], b = snakes[1];
        a.alive = !crashed(a, b);
        b.alive = !crashed(b, a);
        collect(a, b);
        collect(b, a);
        updatePowerUp();
        if (!a.alive || !b.alive) endRound(a, b);
    }

    private boolean crashed(Snake s, Snake foe) {
        if (s.ghostTicks > 0) return false;
        Point h = s.head();
        boolean hitWall = h.x < 0 || h.y < 0 || h.x >= WIDTH || h.y >= HEIGHT;
        boolean hitSelf = s.body.subList(1, s.body.size()).contains(h);
        boolean hitFoe = foe.ghostTicks == 0 && foe.body.contains(h);
        return hitWall || hitSelf || hitFoe;
    }

    private void collect(Snake s, Snake foe) {
        if (s.head().equals(reward)) {
            s.eat();
            reward = freeCell();
        }
        if (power != null && s.head().equals(powerSpot)) {
            power.apply(s, foe);
            power = null;
        }
    }

    private void updatePowerUp() {
        if (power != null) {
            if (--powerLife == 0) power = null;
        } else if (rnd.nextDouble() < POWER_CHANCE) {
            power = PowerUp.random(rnd);
            powerSpot = freeCell();
            powerLife = POWER_LIFETIME;
        }
    }

    private void endRound(Snake a, Snake b) {
        running = false;
        String result = a.alive ? a.name + " wins!" : b.alive ? b.name + " wins!" : "Draw!";
        message = result + " Press SPACE to play again";
    }

    private Point freeCell() {
        while (true) {
            Point p = new Point(rnd.nextInt(WIDTH), rnd.nextInt(HEIGHT));
            boolean taken = p.equals(reward) || (power != null && p.equals(powerSpot));
            for (Snake s : snakes) taken |= s.body.contains(p);
            if (!taken) return p;
        }
    }

    /** Text format: "top bar|message|code x y,code x y,..." */
    synchronized String state() {
        StringBuilder cells = new StringBuilder();
        for (Snake s : snakes) {
            for (Point p : s.body) {
                char code = p == s.head() ? s.code : Character.toLowerCase(s.code);
                addCell(cells, code, p);
            }
        }
        addCell(cells, 'R', reward);
        if (power != null) addCell(cells, power.letter(), powerSpot);
        return snakes[0].status() + "     " + snakes[1].status() + "|" + message + "|" + cells;
    }

    private void addCell(StringBuilder cells, char code, Point p) {
        cells.append(code).append(' ').append(p.x).append(' ').append(p.y).append(',');
    }
}
