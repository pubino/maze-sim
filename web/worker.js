const scope = /** @type {import("./protocol").WorkerScope} */ (/** @type {unknown} */ (self));

async function initialize() {
  const vendor = new URL("./vendor/", scope.location.href).href;
  scope.importScripts(new URL("pyodide.js", vendor).href);
  const python = await scope.loadPyodide({ indexURL: vendor });
  const response = await fetch(new URL("simulation.py", scope.location.href));
  if (!response.ok) throw new Error("Could not load the Python model.");
  python.FS.writeFile("/home/pyodide/simulation.py", await response.text());
  return python.runPython("from simulation import generate\ngenerate");
}

initialize().then((calculate) => {
  scope.onmessage = ({ data }) => {
    try {
      const proxy = calculate(JSON.stringify(data.parameters));
      try {
        const result = proxy.toJs({ create_pyproxies: false, dict_converter: Object.fromEntries });
        scope.postMessage({ type: "result", id: data.id, result });
      } finally {
        proxy.destroy();
      }
    } catch (error) {
      const message = String(error).match(/ValueError: ([^\r\n]+)/)?.[1];
      scope.postMessage({ type: "error", id: data.id, message: message ?? "The calculation failed." });
    }
  };
  scope.postMessage({ type: "ready" });
}, () => scope.postMessage({ type: "fatal" }));
