const MARGIN = 28;

/** @param {HTMLCanvasElement} canvas */
function context(canvas) {
  const painter = canvas.getContext("2d");
  if (!painter) throw new Error("Canvas is unavailable.");
  return painter;
}

/** @param {HTMLCanvasElement} canvas @param {number} size */
function tileWidth(canvas, size) {
  return (canvas.width - 2 * MARGIN) / size;
}

/** @param {number} cell @param {number} size @param {number} width */
function origin(cell, size, width) {
  return [
    MARGIN + (cell % size) * width,
    MARGIN + (size - 1 - Math.floor(cell / size)) * width,
  ];
}

/** @param {HTMLCanvasElement} canvas @param {import("./protocol").Result} maze */
export function drawMaze(canvas, maze) {
  const painter = context(canvas);
  const { size, start, end, openings } = maze;
  const width = tileWidth(canvas, size);
  painter.fillStyle = "#f5f8fb";
  painter.fillRect(0, 0, canvas.width, canvas.height);
  const [startX, startY] = origin(start, size, width);
  const [endX, endY] = origin(end, size, width);
  painter.fillStyle = "#b9d9f4";
  painter.fillRect(startX, startY, width, width);
  painter.fillStyle = "#bcdfcc";
  painter.fillRect(endX, endY, width, width);
  painter.beginPath();
  openings.forEach((mask, cell) => {
    const [x, y] = origin(cell, size, width);
    if (cell % size === 0) {
      painter.moveTo(x, y);
      painter.lineTo(x, y + width);
    }
    if (!(mask & 2)) {
      painter.moveTo(x + width, y);
      painter.lineTo(x + width, y + width);
    }
    if (cell < size && cell !== start) {
      painter.moveTo(x, y + width);
      painter.lineTo(x + width, y + width);
    }
    if (!(mask & 4) && cell !== end) {
      painter.moveTo(x, y);
      painter.lineTo(x + width, y);
    }
  });
  painter.strokeStyle = "#40586e";
  painter.lineWidth = Math.max(1, Math.min(3, width * 0.08));
  painter.lineCap = "butt";
  painter.stroke();
}

/** @param {HTMLCanvasElement} canvas @param {number} size @param {import("./protocol").Step} step */
export function drawStep(canvas, size, [source, target, backtracking]) {
  const painter = context(canvas);
  const width = tileWidth(canvas, size);
  const [x0, y0] = origin(source, size, width);
  const [x1, y1] = origin(target, size, width);
  painter.beginPath();
  painter.moveTo(x0 + width / 2, y0 + width / 2);
  painter.lineTo(x1 + width / 2, y1 + width / 2);
  painter.lineWidth = Math.max(2, Math.min(8, width * 0.24));
  painter.lineCap = "round";
  painter.strokeStyle = backtracking ? "#92a1ae" : "#1767a3";
  painter.stroke();
}
