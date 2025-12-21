# Maze Generator and Solver

Part of [Prof. Robert Vanderbei's Java Applications](https://pubino.github.io/vanderbei-apps).

An interactive Java application for generating and solving mazes using recursive backtracking algorithms. Originally developed by Robert Vanderbei, Princeton University (1997).

## Overview

This educational tool allows users to:
- Generate random mazes of configurable sizes
- Watch an animated solution that finds the path from start to finish
- Visualize the backtracking algorithm as it explores dead ends

## Features

- **Configurable Size**: Set the maze dimensions (n x n grid)
- **Random Generation**: Creates unique mazes using recursive backtracking
- **Animated Solver**: Watch the algorithm explore the maze in real-time
- **Visual Feedback**: See the path being explored (red) and backtracked (gray)

## Requirements

- Java 14+ (for running the JAR)
- Java 14+ with `jpackage` (for building macOS app bundle)

## Building

```bash
# Build the JAR file
make jar

# Build macOS .app bundle (unsigned)
make app
```

## Running

```bash
# Run the JAR
make run

# Or run directly
java -jar dist/MazeSim.jar
```

## Usage

### Generating a Maze

1. Enter the desired maze size in the "n:" field (default: 10)
2. Click **Generate** to create a new random maze
3. The start position (magenta) appears on the left edge
4. The goal position (blue) appears on the right edge

### Solving the Maze

1. After generating a maze, click **Solve**
2. Watch the algorithm explore paths from start to goal
3. Red lines show the current path being explored
4. Gray lines show paths that led to dead ends (backtracked)
5. The solution is complete when the red path reaches the blue square

## Project Structure

```
maze-sim/
├── maze/
│   └── Maze.java           # Main app, MazeCanvas, MazeElem classes
├── myutil/
│   ├── AppletFrame.java    # Applet-to-Frame wrapper
│   ├── GL.java             # Graphics library utilities
│   ├── SL.java             # String parsing utilities
│   ├── GridPanel.java      # Grid layout panel
│   └── Format.java         # Number formatting
├── Makefile                # Build configuration
└── .github/workflows/      # CI/CD configuration
```

## Algorithms

### Maze Generation (Recursive Backtracking)

1. Start at a random cell, mark it as visited
2. While there are unvisited neighbors:
   - Choose a random unvisited neighbor
   - Remove the wall between current cell and chosen neighbor
   - Recursively visit the chosen neighbor
3. Backtrack when no unvisited neighbors remain

This creates a "perfect" maze with exactly one path between any two cells.

### Maze Solving (Depth-First Search)

1. Start at the entry point
2. Mark current cell as visited
3. Try each direction (left, right, up, down):
   - If the path is open and unvisited, recursively explore
   - If goal is reached, stop
   - Otherwise, backtrack (shown in gray)
4. The final red path shows the solution

## Visual Indicators

- **Magenta square**: Starting position
- **Blue square**: Goal position
- **Green lines**: Maze walls
- **Red line**: Current solution path
- **Gray line**: Backtracked (dead end) paths
