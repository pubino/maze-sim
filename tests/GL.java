package myutil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

/** Records the solver's forward and backtracking draws without a display. */
public final class GL {
    private static int x, y, size;
    private static Color color;
    private static final StringBuilder walk = new StringBuilder();

    public static void begin(int n, int i, int j) {
        size = n;
        walk.setLength(0);
        move2(i + 0.5, j + 0.5);
    }

    public static String walk() { return walk.toString(); }

    public static void color(Color value) { color = value; }

    public static void move2(double xx, double yy) {
        x = (int) xx;
        y = (int) yy;
    }

    public static void draw2(double xx, double yy) {
        if ((int) xx != x || (int) yy != y) {
            if (walk.length() > 0) walk.append(";");
            walk.append(x + y * size).append(",")
                .append((int) xx + (int) yy * size).append(",")
                .append(color == Color.gray ? 1 : 0);
        }
        move2(xx, yy);
    }

    public static void ginit(Graphics g, int w, int h, Component c, boolean b) {}
    public static void ortho2(double a, double b, double c, double d) {}
    public static void clear() {}
    public static void rectf(double a, double b, double c, double d) {}
}
