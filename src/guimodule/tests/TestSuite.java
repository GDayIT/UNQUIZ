package guimodule.tests;

import dbbl.DbblDelegate;
import dbbl.PersistenceDelegate;
import guimodule.GuiModuleDelegate;
import guimodule.PnlForming;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TestSuite provides a minimal, standalone testing framework without
 * external dependencies or frameworks.
 *
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>UnitTests: verifies delegate wiring and theme CRUD operations</li>
 *   <li>FunctionalTests: tests question save/load flows via delegates</li>
 *   <li>IntegrationTests: verifies persistence across process instances</li>
 *   <li>SmokeTests: ensures GUI panels can be instantiated without rendering</li>
 * </ul>
 * </p>
 *
 * <p>
 * Test harness prints a concise summary of passes/failures and measures
 * overall execution time. It terminates with a non-zero exit code if any
 * test fails.
 * </p>
 *
 * Usage:
 * <pre>
 * javac -encoding UTF-8 -d build (all sources)
 * java -cp build guimodule.tests.TestSuite
 * </pre>
 *
 * Dependencies:
 * <ul>
 *   <li>DbblDelegate / PersistenceDelegate for backend operations</li>
 *   <li>GuiModuleDelegate for UI-related construction</li>
 *   <li>PnlForming panel for GUI smoke testing</li>
 * </ul>
 *
 * @author D.
 * @version 1.0
 */
public final class TestSuite {

    /**
     * Entry point to run the complete test suite.
     * Executes unit, functional, integration, and smoke tests sequentially.
     *
     * @param args command line arguments (ignored)
     */
    public static void main(String[] args) {
        TestSupport t = new TestSupport();
        long startedAt = System.currentTimeMillis();
        System.out.println("=== Running Test Suite ===");
        try {
            UnitTests.run(t);
            FunctionalTests.run(t);
            IntegrationTests.run(t);
            SmokeTests.run(t);
        } catch (Throwable ex) {
            t.fail("Unhandled exception in test suite: " + ex.getMessage());
            ex.printStackTrace();
        }
        long dur = System.currentTimeMillis() - startedAt;
        System.out.println();
        System.out.println("=== Test Summary ===");
        System.out.println("Passed: " + t.passed + ", Failed: " + t.failed + ", Duration: " + dur + " ms");
        if (t.failed > 0) System.exit(1);
    }

    // ============================================================
    // Minimal Test Harness (inline)
    // ============================================================
    /**
     * Provides assertion methods, pass/fail counters, and failure reporting.
     */
    static final class TestSupport {
        /** Count of passed tests */
        int passed = 0;

        /** Count of failed tests */
        int failed = 0;

        /** Asserts a boolean condition, increments counters and prints results. */
        void assertTrue(String msg, boolean cond) {
            if (cond) {
                passed++;
                System.out.println("[PASS] " + msg);
            } else {
                failed++;
                System.err.println("[FAIL] " + msg);
            }
        }

        /** Asserts equality of expected vs actual objects. */
        void assertEquals(String msg, Object exp, Object got) {
            boolean ok = (exp == null ? got == null : exp.equals(got));
            assertTrue(msg + " (expected=\"" + exp + "\", got=\"" + got + "\")", ok);
        }

        /** Asserts that an object is not null. */
        void assertNotNull(String msg, Object got) { assertTrue(msg, got != null); }

        /** Immediately fails the test with a message. */
        void fail(String msg) { assertTrue(msg, false); }
    }

    // ============================================================
    // Unit Tests
    // ============================================================
    /**
     * Validates delegate wiring, CRUD operations for themes, and simple backend correctness.
     */
    static final class UnitTests {
        static void run(TestSupport t) {
            System.out.println("-- UnitTests --");
            DbblDelegate db = DbblDelegate.createDefault();

            // Test theme save
            String theme = "UT_" + System.currentTimeMillis();
            boolean saved = db.themeSave().apply(new PersistenceDelegate.ThemeData(theme, "desc"));
            t.assertTrue("save theme", saved);

            // Test theme appears in list
            List<String> themes = db.uiAllTopics().get();
            t.assertTrue("theme appears in list", themes.contains(theme));

            // Test load theme
            PersistenceDelegate.ThemeData loaded = db.themeLoad().apply(theme);
            t.assertEquals("loaded theme title", theme, loaded.title);

            // Test delete theme
            boolean deleted = db.themeDelete().apply(theme);
            t.assertTrue("delete theme", deleted);
        }
    }

    // ============================================================
    // Functional Tests
    // ============================================================
    /**
     * Validates question save/load functionality via delegate, including answer flags.
     */
    static final class FunctionalTests {
        static void run(TestSupport t) {
            System.out.println("-- FunctionalTests --");
            DbblDelegate db = DbblDelegate.createDefault();
            String theme = "FT_" + System.currentTimeMillis();

            db.themeSave().apply(new PersistenceDelegate.ThemeData(theme, "Functional Test Theme"));

            // Save question
            String title = "What is 2+2?";
            List<String> answers = new ArrayList<>();
            answers.add("4"); answers.add("5");
            List<Boolean> flags = new ArrayList<>();
            flags.add(Boolean.TRUE); flags.add(Boolean.FALSE);

            boolean qSaved = db.questionSave().apply(new PersistenceDelegate.QuestionData(
                theme, title, "2+2?", "Simple arithmetic", answers, flags
            ));
            t.assertTrue("save question", qSaved);

            // Verify question title is available
            List<String> titles = db.uiQuestionTitles().apply(theme);
            t.assertTrue("question is listed", titles.contains(title));
        }
    }

    // ============================================================
    // Integration Tests
    // ============================================================
    /**
     * Simulates persistence across process instances.
     * Validates that data saved in one instance is accessible in a new instance.
     */
    static final class IntegrationTests {
        static void run(TestSupport t) {
            System.out.println("-- IntegrationTests --");
            String theme = "IT_" + System.currentTimeMillis();

            // First instance: save theme and question
            {
                DbblDelegate db = DbblDelegate.createDefault();
                db.themeSave().apply(new PersistenceDelegate.ThemeData(theme, "Integration Theme"));
                List<String> answers = List.of("Blue", "Green");
                List<Boolean> flags = List.of(Boolean.TRUE, Boolean.FALSE);
                db.questionSave().apply(new PersistenceDelegate.QuestionData(
                    theme, "Sky color?", "Sky color?", "", answers, flags
                ));
                db.persistAll().run();
            }

            // Second instance: simulate fresh process, validate persistence
            {
                DbblDelegate db2 = DbblDelegate.createDefault();
                List<String> themes2 = db2.uiAllTopics().get();
                t.assertTrue("theme persisted", themes2.contains(theme));

                List<String> titles2 = db2.uiQuestionTitles().apply(theme);
                t.assertTrue("question persisted", !titles2.isEmpty());
            }
        }
    }

    // ============================================================
    // Smoke Tests
    // ============================================================
    /**
     * Validates that panels can be instantiated without rendering or runtime errors.
     */
    static final class SmokeTests {
        static void run(TestSupport t) {
            System.out.println("-- SmokeTests --");
            try {
                GuiModuleDelegate gm = GuiModuleDelegate.createDefault();
                PnlForming panel = new PnlForming(gm);
                t.assertNotNull("panel constructed", panel);
            } catch (Throwable ex) {
                t.fail("failed to construct PnlForming: " + ex.getMessage());
            }
        }
    }
}