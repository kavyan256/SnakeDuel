import java.awt.Point;
import java.util.LinkedList;

/** One player's snake. It knows how to turn, move, grow and shrink. */
class Snake {
    final String name;
    final char code; // how the snake is drawn: 'A' or 'B'
    final LinkedList<Point> body = new LinkedList<>();
    int score, speedTicks, ghostTicks;
    boolean alive = true;

    private Direction dir, lastMoved;
    private int growth;

    Snake(String name, char code, int x, int y, Direction dir) {
        this.name = name;
        this.code = code;
        this.dir = this.lastMoved = dir;
        for (int i = 0; i < 3; i++) body.add(new Point(x - i * dir.dx, y - i * dir.dy));
    }

    Point head() {
        return body.getFirst();
    }

    void turn(Direction d) {
        if (!d.isOpposite(lastMoved)) dir = d; // can't reverse into yourself
    }

    /** Normal snakes move every 2nd frame, boosted ones every frame. */
    boolean movesOn(int frame) {
        return speedTicks > 0 || frame % 2 == 0;
    }

    void move() {
        int x = head().x + dir.dx, y = head().y + dir.dy;
        if (ghostTicks > 0) { // ghosts wrap around the walls
            x = (x + Game.WIDTH) % Game.WIDTH;
            y = (y + Game.HEIGHT) % Game.HEIGHT;
        }
        body.addFirst(new Point(x, y));
        lastMoved = dir;
        if (growth > 0) growth--;
        else body.removeLast();
    }

    void eat() {
        score++;
        growth++;
    }

    void cutInHalf() {
        int newLength = Math.max(3, body.size() / 2);
        while (body.size() > newLength) body.removeLast();
    }

    void wearOffEffects() {
        if (speedTicks > 0) speedTicks--;
        if (ghostTicks > 0) ghostTicks--;
    }

    String status() {
        return name + ": " + score + (speedTicks > 0 ? " [SPEED]" : "") + (ghostTicks > 0 ? " [GHOST]" : "");
    }
}
