import json
import subprocess
import tempfile
import unittest
from pathlib import Path

from web.simulation import generate

ROOT = Path(__file__).resolve().parents[1]


class MazeTest(unittest.TestCase):
    def test_java_topology_and_solver_trace(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            # Replace only the random source in a temporary copy of the Java app.
            source = Path(directory) / "Maze.java"
            source.write_text(
                (ROOT / "maze/Maze.java")
                .read_text()
                .replace("Math.random()", "MazeReference.random()")
            )
            subprocess.run(
                [
                    "javac",
                    "--release",
                    "25",
                    "-d",
                    directory,
                    source,
                    *(
                        path
                        for path in sorted((ROOT / "myutil").glob("*.java"))
                        if path.name != "GL.java"
                    ),
                    ROOT / "tests/GL.java",
                    ROOT / "tests/MazeReference.java",
                ],
                check=True,
                timeout=30,
            )
            for size, seed in ((1, 0), (3, 119), (10, 42), (24, 2147483647)):
                with self.subTest(size=size, seed=seed):
                    output = subprocess.check_output(
                        [
                            "java",
                            "-Djava.awt.headless=true",
                            "-cp",
                            directory,
                            "maze.MazeReference",
                            str(size),
                            str(seed),
                        ],
                        text=True,
                        timeout=30,
                    ).splitlines()
                    maze = generate(json.dumps({"size": size, "seed": seed}))
                    self.assertEqual(
                        (maze["start"], maze["end"]),
                        tuple(map(int, output[0].split(","))),
                    )
                    self.assertEqual(
                        maze["openings"], list(map(int, output[1].split(",")))
                    )
                    walk = [
                        tuple(map(int, step.split(",")))
                        for step in output[2].split(";")
                        if step
                    ]
                    if walk or maze["steps"]:
                        # Java returns at the exit before drawing the last move.
                        self.assertEqual(maze["steps"][:-1], walk)
                        self.assertEqual(maze["steps"][-1][1:], (maze["end"], 0))

    def test_largest_grid_is_a_tree_and_route_is_open(self) -> None:
        maze = generate('{"size":50,"seed":2147483647}')
        self.assertEqual(
            sum(cell.bit_count() for cell in maze["openings"]), 2 * (50**2 - 1)
        )
        for source, target, _ in maze["steps"]:
            delta = target - source
            direction = {-1: 0, 1: 1, 50: 2, -50: 3}[delta]
            self.assertTrue(maze["openings"][source] & (1 << direction))
            self.assertTrue(maze["openings"][target] & (1 << (direction ^ 1)))
        self.assertEqual(maze["steps"][-1][1], maze["end"])

    def test_rejects_invalid_dimensions_and_seeds(self) -> None:
        invalid: list[object] = [
            {"size": 51, "seed": 0},
            {"size": True, "seed": 0},
            {"size": 5, "seed": -1},
            {"size": 10.5, "seed": 8},
            [],
        ]
        for arguments in invalid:
            with self.subTest(arguments=arguments), self.assertRaises(ValueError):
                generate(json.dumps(arguments))
