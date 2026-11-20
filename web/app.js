import { drawMaze, drawStep } from "./view.js";

/**
 * @template {Element} T
 * @param {string} id
 * @param {{new(): T}} constructor
 */
function find(id, constructor) {
  const node = document.getElementById(id);
  if (!(node instanceof constructor)) throw new Error(`Missing element ${id}`);
  return node;
}

const form = find("settings", HTMLFormElement);
const generator = find("generator", HTMLFieldSetElement);
const solver = find("solver", HTMLFieldSetElement);
const size = find("size", HTMLInputElement);
const seed = find("seed", HTMLInputElement);
const seedUsed = find("seed-used", HTMLElement);
const canvas = find("maze", HTMLCanvasElement);
const moves = find("moves", HTMLElement);
const length = find("length", HTMLElement);
const status = find("status", HTMLElement);
const error = find("error", HTMLElement);
const retry = find("retry", HTMLButtonElement);
const play = find("play", HTMLButtonElement);
const step = find("step", HTMLButtonElement);
const reset = find("reset", HTMLButtonElement);

const worker = new Worker(new URL("./worker.js", import.meta.url));
/** @type {import("./protocol").Result | null} */
let maze = null;
let cursor = 0;
let id = 0;
let timer = 0;

function updateControls() {
  if (!maze) return;
  const finished = cursor === maze.steps.length;
  moves.textContent = cursor.toLocaleString();
  length.textContent = finished ? maze.route_length.toLocaleString() : "—";
  play.disabled = finished;
  play.textContent = timer ? "Pause" : (cursor && !finished ? "Resume" : "Solve");
  step.disabled = finished || Boolean(timer);
  reset.disabled = !cursor;
}

function stop() {
  window.clearInterval(timer);
  timer = 0;
}

function resetSolver() {
  if (!maze) return;
  stop();
  cursor = 0;
  drawMaze(canvas, maze);
  updateControls();
  status.textContent = maze.steps.length ? "Ready to solve." : "The entrance is also the exit.";
}

function advance() {
  if (!maze || cursor >= maze.steps.length) return;
  drawStep(canvas, maze.size, maze.steps[cursor++]);
  if (cursor === maze.steps.length) {
    stop();
    status.textContent = "Exit reached.";
  }
  updateControls();
}

function generate() {
  stop();
  generator.disabled = solver.disabled = true;
  error.hidden = true;
  status.textContent = "Generating the maze…";
  /** @type {import("./protocol").Request} */
  const request = {
    id: ++id,
    parameters: { size: size.valueAsNumber, seed: seed.value
      ? seed.valueAsNumber : crypto.getRandomValues(new Uint32Array(1))[0] },
  };
  worker.postMessage(request);
}

function showFatal() {
  stop();
  worker.terminate();
  generator.disabled = solver.disabled = true;
  status.textContent = "";
  error.textContent = "Python could not load. Reload to try again.";
  error.hidden = retry.hidden = false;
}

worker.onerror = showFatal;
worker.onmessage = (/** @type {MessageEvent<import("./protocol").Reply>} */ { data }) => {
  if (data.type === "ready") generate();
  if (data.type === "fatal") showFatal();
  if (!("id" in data) || data.id !== id) return;
  generator.disabled = false;
  if (data.type === "error") {
    status.textContent = "";
    error.textContent = data.message;
    error.hidden = false;
    solver.disabled = !maze;
    updateControls();
    return;
  }
  maze = data.result;
  canvas.hidden = false;
  solver.disabled = false;
  seedUsed.textContent = String(maze.seed);
  resetSolver();
};

form.addEventListener("submit", (event) => {
  event.preventDefault();
  if (!generator.disabled && form.reportValidity()) generate();
});
play.addEventListener("click", () => {
  if (timer) {
    stop();
    status.textContent = "Paused.";
  } else {
    timer = window.setInterval(advance, 25);
    status.textContent = "Solving…";
  }
  updateControls();
});
step.addEventListener("click", () => {
  status.textContent = "Paused.";
  advance();
});
reset.addEventListener("click", resetSolver);
retry.addEventListener("click", () => window.location.reload());
