import threading
import unittest
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

from playwright.sync_api import (
    Browser,
    BrowserContext,
    Page,
    Playwright,
    expect,
    sync_playwright,
)

BUILD = Path(__file__).resolve().parents[1] / "build"


class BrowserTest(unittest.TestCase):
    server: ThreadingHTTPServer
    thread: threading.Thread
    playwright: Playwright
    browser: Browser
    context: BrowserContext
    page: Page

    @classmethod
    def setUpClass(cls) -> None:
        if not (BUILD / "site/index.html").is_file():
            raise RuntimeError("Build the site before running browser tests.")
        # Serve from a subdirectory to match a GitHub project Pages URL.
        cls.server = ThreadingHTTPServer(
            ("127.0.0.1", 0), partial(SimpleHTTPRequestHandler, directory=BUILD)
        )
        cls.thread = threading.Thread(target=cls.server.serve_forever, daemon=True)
        cls.thread.start()
        cls.addClassCleanup(cls.server.server_close)
        cls.addClassCleanup(cls.thread.join, 5)
        cls.addClassCleanup(cls.server.shutdown)
        cls.playwright = sync_playwright().start()
        cls.addClassCleanup(cls.playwright.stop)
        cls.browser = cls.playwright.chromium.launch()
        cls.addClassCleanup(cls.browser.close)

    def setUp(self) -> None:
        self.context = self.browser.new_context()
        self.addCleanup(self.context.close)
        self.page = self.context.new_page()
        self.base_url = f"http://127.0.0.1:{self.server.server_port}/site/"

    def test_seeded_generation_and_solver_controls(self) -> None:
        self.page.goto(self.base_url)
        generate = self.page.get_by_role("button", name="Generate", exact=True)
        expect(generate).to_be_enabled(timeout=45_000)
        self.page.get_by_label("Grid size").fill("3")
        self.page.get_by_label("Seed").fill("119")
        generate.click()
        expect(self.page.locator("#seed-used")).to_have_text("119")
        expect(self.page.locator("#moves")).to_have_text("0")
        maze = self.page.locator("#maze")
        original = maze.evaluate("(canvas) => canvas.toDataURL()")

        self.page.get_by_role("button", name="Step", exact=True).click()
        expect(self.page.locator("#moves")).to_have_text("1")
        self.assertNotEqual(maze.evaluate("(canvas) => canvas.toDataURL()"), original)
        self.page.get_by_role("button", name="Resume").click()
        expect(self.page.get_by_role("status")).to_have_text("Exit reached.")
        expect(self.page.locator("#length")).not_to_have_text("—")

        self.page.get_by_role("button", name="Reset solver").click()
        expect(self.page.locator("#moves")).to_have_text("0")
        self.assertEqual(maze.evaluate("(canvas) => canvas.toDataURL()"), original)
        generate.click()
        expect(generate).to_be_enabled()
        self.assertEqual(maze.evaluate("(canvas) => canvas.toDataURL()"), original)

        self.page.get_by_label("Grid size").fill("1")
        generate.click()
        expect(self.page.get_by_role("status")).to_have_text(
            "The entrance is also the exit."
        )
        expect(self.page.locator("#length")).to_have_text("0")
        self.page.set_viewport_size({"width": 390, "height": 844})
        self.assertTrue(
            self.page.evaluate("document.documentElement.scrollWidth <= innerWidth")
        )

    def test_runtime_failure_shows_retry(self) -> None:
        self.page.route("**/vendor/pyodide.js", lambda route: route.abort())
        self.page.goto(self.base_url)
        expect(self.page.get_by_role("alert")).to_contain_text("Python could not load")
        expect(self.page.get_by_role("button", name="Reload")).to_be_visible()
