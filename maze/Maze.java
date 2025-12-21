package maze;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.io.*;
import java.text.*;

import myutil.*;
import myutil.Format;

public class Maze extends Panel implements SimApp
{
	TextField N;

	MazeCanvas mc;

        public static void main(String[] args) {
                new AppletFrame(new Maze(), 350, 500);
        }

	public void init()
	{
	    setLayout(new FlowLayout(FlowLayout.LEFT));
	    setBackground(Color.white);

	    N   = new TextField("10", 5);

	    GridPanel gp = new GridPanel();

	    gp.add(new Label("n:"), 1,1); gp.add(N, 2,1);
	    gp.add(new Button("Generate"),1,2,2,1);
	    gp.add(new Button("Solve"),   3,2,2,1);
	    add(gp);

	    mc = new MazeCanvas();
	    add(mc);
	    mc.resize(300,300);
	    validate();
	    show();
	}

	public void start() {
		// No-op for this simulation
	}

        public boolean action(Event evt, Object arg)
        {
                if (arg.equals("Generate")) {
			int    n = SL.atoi(N.getText());
			mc.genmaze(n, n);
                } else if (arg.equals("Solve")) {
			GL.move2(mc.i0+0.5, mc.j0+0.5);
			mc.solve(mc.i0, mc.j0);
		} else {
                        return super.action(evt, arg);
                }
                return true;
        }

        public boolean handleEvent(Event evt)
        {
                if (evt.id == Event.WINDOW_DESTROY) System.exit(0);
                return super.handleEvent(evt);
        }
}

class MazeCanvas extends Canvas
{
	int m, n;
	int i0, j0, i1, j1;
	MazeElem[][] maze;
	boolean finished;

	public void genmaze(int m, int n)
	{
		this.m = m;
		this.n = n;
		maze = new MazeElem[m][n];
		for (int i=0; i<m; i++) {
		    for (int j=0; j<n; j++) {
			maze[i][j] = new MazeElem();
		    }
		}

		i0 = (int)(Math.random()*m);
		i1 = (int)(Math.random()*m);
		j0 = 0;
		j1 = n-1;

		WallsUp();
		CreateMaze(i0, j0);
		for (int i=0; i<m; i++) {
		    for (int j=0; j<n; j++) {
			maze[i][j].crumb = false;
		    }
		}

		//maze[i1][j1].finish = true;
		finished = false;

		paint(getGraphics());
	}

	void WallsUp()
	{
		int i, j;

		for (i=0; i<m; i++) {
			for (j=0; j<n; j++) {
				maze[i][j].bottom = Blocked;
				maze[i][j].top    = Blocked;
				maze[i][j].left   = Blocked;
				maze[i][j].right  = Blocked;
			}
		}
	}

	void CreateMaze(int i, int j)
	{
	    int cnt;

	    maze[i][j].crumb = true;

	    while (true) {
		cnt = 0;
		if (i>0)   {if (maze[i-1][j].crumb==false) cnt++;}
		if (j>0)   {if (maze[i][j-1].crumb==false) cnt++;}
		if (i<m-1) {if (maze[i+1][j].crumb==false) cnt++;}
		if (j<n-1) {if (maze[i][j+1].crumb==false) cnt++;}
		if (cnt == 0) return;	// nowhere left to go

		cnt = (int) (Math.random()*4);
		switch (cnt) {
		    case LEFT:
			if (i>0)   if (maze[i-1][j].crumb==false) {
			    maze[i][j]  .left  = Open;
			    maze[i-1][j].right = Open;
			    CreateMaze(i-1, j);
			}
			break;
		    case RIGHT:
			if (i<m-1) if (maze[i+1][j].crumb==false) {
			    maze[i][j]  .right = Open;
			    maze[i+1][j].left  = Open;
			    CreateMaze(i+1, j);
			}
			break;
		    case TOP:
			if (j<n-1) if (maze[i][j+1].crumb==false) {
			    maze[i][j]  .top    = Open;
			    maze[i][j+1].bottom = Open;
			    CreateMaze(i, j+1);
			}
			break;
		    case BOTTOM:
			if (j>0)   if (maze[i][j-1].crumb==false) {
			    maze[i][j]  .bottom = Open;
			    maze[i][j-1].top    = Open;
			    CreateMaze(i, j-1);
			}
			break;
		}
	    }
	}

	public void solve(int i, int j)
	{
		if (i == i1 && j == j1) finished = true;

		if (finished) return;

		hessitate();

		GL.color(Color.red);
		GL.draw2(i+0.5, j+0.5);

		maze[i][j].crumb = true;

		if (maze[i][j].left   == Open && maze[i-1][j].crumb == false) {
		    solve(i-1, j);
		    if (!finished) {
		    GL.color(Color.gray);
		    GL.draw2(i+0.5, j+0.5);
		    }
		    hessitate();
		}
		if (maze[i][j].right  == Open && maze[i+1][j].crumb == false) {
		    solve(i+1, j);
		    if (!finished) {
		    GL.color(Color.gray);
		    GL.draw2(i+0.5, j+0.5);
		    }
		    hessitate();
		}
		if (maze[i][j].top    == Open && maze[i][j+1].crumb == false) {
		    solve(i, j+1);
		    if (!finished) {
		    GL.color(Color.gray);
		    GL.draw2(i+0.5, j+0.5);
		    }
		    hessitate();
		}
		if (maze[i][j].bottom == Open && maze[i][j-1].crumb == false) {
		    solve(i, j-1);
		    if (!finished) {
		    GL.color(Color.gray);
		    GL.draw2(i+0.5, j+0.5);
		    }
		    hessitate();
		}

	}

	public void hessitate()
	{
		try {
		    Thread.sleep(25);
		} catch(InterruptedException ie){};
	}

	public void paint (Graphics g)
	{
	        GL.ginit(getGraphics(), size().width, size().height, this,
							false);
		GL.ortho2(-0.1*m, 1.1*m, -0.1*n, 1.1*n);

		GL.color(Color.black);
		GL.clear();

		/* draw maze */

		GL.color(Color.green);
		for (int i=0; i<m; i++) {
		    for (int j=0; j<n; j++) {
			if (maze[i][j].left   == Blocked) {
			    GL.move2(i+0.1, j+0.0);
			    GL.draw2(i+0.1, j+1.0);
			}
			if (maze[i][j].right  == Blocked) {
			    GL.move2(i+0.9, j+0.0);
			    GL.draw2(i+0.9, j+1.0);
			}
			if (maze[i][j].top    == Blocked) {
			    GL.move2(i+0.0, j+0.9);
			    GL.draw2(i+1.0, j+0.9);
			}
			if (maze[i][j].bottom == Blocked) {
			    GL.move2(i+0.0, j+0.1);
			    GL.draw2(i+1.0, j+0.1);
			}
		    }
		}

		/* draw ends */

		GL.color(Color.magenta);
		GL.rectf(i0+0.1, j0+0.1, i0+0.9, j0+0.9);
		GL.color(Color.blue);
		GL.rectf(i1+0.1, j1+0.1, i1+0.9, j1+0.9);
	}

	static final int LEFT   = 0;
	static final int RIGHT  = 1;
	static final int TOP    = 2;
	static final int BOTTOM = 3;

	static final boolean Blocked = false;
	static final boolean Open    = true;
}

class MazeElem
{
	boolean top;
	boolean bottom;
	boolean left;
	boolean right;
	boolean crumb;
}
