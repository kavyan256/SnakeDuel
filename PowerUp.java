import java.util.Random;

/** Each power-up knows its own effect. Shown on the board by its first letter. */
enum PowerUp {
    SPEED { void apply(Snake me, Snake foe) { me.speedTicks = 100; } },
    GHOST { void apply(Snake me, Snake foe) { me.ghostTicks = 100; } },
    CUT   { void apply(Snake me, Snake foe) { foe.cutInHalf(); } };

    abstract void apply(Snake me, Snake foe);

    char letter() {
        return name().charAt(0);
    }

    static PowerUp random(Random rnd) {
        return values()[rnd.nextInt(values().length)];
    }
}
