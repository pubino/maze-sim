"""Generate and solve the Java maze with explicit depth-first stacks."""

import json
from typing import TypedDict

Step = tuple[int, int, int]


class Maze(TypedDict):
    size: int
    seed: int
    start: int
    end: int
    openings: list[int]
    steps: list[Step]
    route_length: int


class JavaRandom:
    def __init__(self, seed: int) -> None:
        self.state = (seed ^ 0x5DEECE66D) & ((1 << 48) - 1)

    def bits(self, width: int) -> int:
        self.state = (self.state * 0x5DEECE66D + 11) & ((1 << 48) - 1)
        return self.state >> (48 - width)

    def random(self) -> float:
        return ((self.bits(26) << 27) + self.bits(27)) / (1 << 53)


def neighbors(cell: int, size: int) -> tuple[int | None, ...]:
    y, x = divmod(cell, size)
    return (
        cell - 1 if x > 0 else None,
        cell + 1 if x + 1 < size else None,
        cell + size if y + 1 < size else None,
        cell - size if y > 0 else None,
    )


def carve(size: int, start: int, rng: JavaRandom) -> list[int]:
    openings = [0] * (size * size)
    seen = {start}
    stack = [start]
    while stack:
        cell = stack[-1]
        sides = neighbors(cell, size)
        if not any(other is not None and other not in seen for other in sides):
            stack.pop()
            continue
        direction = int(rng.random() * 4)
        other = sides[direction]
        if other is not None and other not in seen:
            openings[cell] |= 1 << direction
            openings[other] |= 1 << (direction ^ 1)
            seen.add(other)
            stack.append(other)
    return openings


def solve(
    openings: list[int], size: int, start: int, end: int
) -> tuple[list[Step], int]:
    seen = {start}
    stack = [start]
    steps: list[Step] = []
    while stack[-1] != end:
        cell = stack[-1]
        for direction, other in enumerate(neighbors(cell, size)):
            if (
                other is not None
                and openings[cell] & (1 << direction)
                and other not in seen
            ):
                seen.add(other)
                stack.append(other)
                steps.append((cell, other, 0))
                break
        else:
            stack.pop()
            steps.append((cell, stack[-1], 1))
    return steps, len(stack) - 1


def generate(parameters: str) -> Maze:
    request = json.loads(parameters)
    if not isinstance(request, dict):
        raise ValueError("Enter a grid size and seed.")
    size, seed = request.get("size"), request.get("seed")
    if type(size) is not int or not 1 <= size <= 50:
        raise ValueError("Use a whole number from 1 to 50 for the grid size.")
    if type(seed) is not int or not 0 <= seed < 2**32:
        raise ValueError("Use a whole number from 0 to 4,294,967,295 for the seed.")
    rng = JavaRandom(seed)
    start = int(rng.random() * size)
    end = int(rng.random() * size) + size * (size - 1)
    openings = carve(size, start, rng)
    steps, length = solve(openings, size, start, end)
    return {
        "size": size,
        "seed": seed,
        "start": start,
        "end": end,
        "openings": openings,
        "steps": steps,
        "route_length": length,
    }
