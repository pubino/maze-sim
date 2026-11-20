export type Step = [source: number, target: number, backtracking: number];
export interface Result {
  size: number;
  seed: number;
  start: number;
  end: number;
  openings: number[];
  steps: Step[];
  route_length: number;
}
export type Request = { id: number; parameters: { size: number; seed: number } };
export type Reply =
  | { type: "ready" | "fatal" }
  | { type: "result"; id: number; result: Result }
  | { type: "error"; id: number; message: string };
export interface WorkerScope {
  importScripts(url: string): void;
  location: Location;
  loadPyodide(options: { indexURL: string }): Promise<{
    FS: { writeFile(path: string, contents: string): void };
    runPython(code: string): (parameters: string) => {
      toJs(options: { create_pyproxies: false; dict_converter: typeof Object.fromEntries }): Result;
      destroy(): void;
    };
  }>;
  onmessage: (event: MessageEvent<Request>) => void;
  postMessage(message: Reply): void;
}
