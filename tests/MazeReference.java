package maze;

import java.awt.Graphics;
import java.util.Random;
import java.util.StringJoiner;
import myutil.GL;

/** Runs the Java maze with a reproducible replacement for Math.random. */
public final class MazeReference extends MazeCanvas {
    private static Random rng;

    public static double random() { return rng.nextDouble(); }

    @Override
    public void paint(Graphics g) {}

    @Override
    public void hessitate() {}

    public static void main(String[] args) {
        int size = Integer.parseInt(args[0]);
        rng = new Random(Long.parseLong(args[1]));
        MazeReference canvas = new MazeReference();
        canvas.genmaze(size, size);
        StringJoiner walls = new StringJoiner(",");
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size; i++) {
                MazeElem cell = canvas.maze[i][j];
                walls.add(Integer.toString(
                    (cell.left ? 1 : 0) + (cell.right ? 2 : 0)
                    + (cell.top ? 4 : 0) + (cell.bottom ? 8 : 0)));
            }
        }
        System.out.println(canvas.i0 + "," + (canvas.i1 + (size - 1) * size));
        System.out.println(walls);
        GL.begin(size, canvas.i0, canvas.j0);
        canvas.solve(canvas.i0, canvas.j0);
        System.out.println(GL.walk());
        if (!canvas.finished) throw new AssertionError("The solver did not reach the exit.");
    }
}
