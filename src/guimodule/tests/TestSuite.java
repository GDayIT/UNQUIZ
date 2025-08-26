package guimodule.tests;

import dbbl.DbblDelegate;
import dbbl.PersistenceDelegate;
import guimodule.GuiModuleDelegate;
import guimodule.PnlForming;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal test suite without external frameworks. Contains:
 * - UnitTests: delegate wiring and theme operations
 * - FunctionalTests: save/load question flow via delegates
 * - IntegrationTests: persistence across process instances
 * - SmokeTests: instantiate main panels without displaying UI
 *
 * Run with:
 *   javac -encoding UTF-8 -d build (all sources)
 *   java -cp build guimodule.tests.TestSuite
 */
public final class TestSuite {

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

    // ---- Minimal test harness ----
    static final class TestSupport {
        int passed = 0;
        int failed = 0;

        void assertTrue(String msg, boolean cond) {
            if (cond) {
                passed++;
                System.out.println("[PASS] " + msg);
            } else {
                failed++;
                System.err.println("[FAIL] " + msg);
            }
        }

        void assertEquals(String msg, Object exp, Object got) {
            boolean ok = (exp == null ? got == null : exp.equals(got));
            assertTrue(msg + " (expected=\"" + exp + "\", got=\"" + got + "\")", ok);
        }

        void assertNotNull(String msg, Object got) { assertTrue(msg, got != null); }
        void fail(String msg) { assertTrue(msg, false); }
    }

    // ---- Unit tests ----
    static final class UnitTests {
        static void run(TestSupport t) {
            System.out.println("-- UnitTests --");
            DbblDelegate db = DbblDelegate.createDefault();

            // Theme save/load/delete
            String theme = "UT_" + System.currentTimeMillis();
            boolean saved = db.themeSave().apply(new PersistenceDelegate.ThemeData(theme, "desc"));
            t.assertTrue("save theme", saved);
            List<String> themes = db.uiAllTopics().get();
            t.assertTrue("theme appears in list", themes.contains(theme));
            PersistenceDelegate.ThemeData loaded = db.themeLoad().apply(theme);
            t.assertEquals("loaded theme title", theme, loaded.title);
            boolean deleted = db.themeDelete().apply(theme);
            t.assertTrue("delete theme", deleted);
        }
    }

    // ---- Functional tests ----
    static final class FunctionalTests {
        static void run(TestSupport t) {
            System.out.println("-- FunctionalTests --");
            DbblDelegate db = DbblDelegate.createDefault();
            String theme = "FT_" + System.currentTimeMillis();

            // Save theme
            db.themeSave().apply(new PersistenceDelegate.ThemeData(theme, "Functional Test Theme"));

            // Save a question
            String title = "What is 2+2?";
            List<String> answers = new ArrayList<>();
            answers.add("4"); answers.add("5");
            List<Boolean> flags = new ArrayList<>();
            flags.add(Boolean.TRUE); flags.add(Boolean.FALSE);
            boolean qSaved = db.questionSave().apply(new PersistenceDelegate.QuestionData(
                theme, title, "2+2?", "Simple arithmetic", answers, flags
            ));
            t.assertTrue("save question", qSaved);

            // Verify question title available
            List<String> titles = db.uiQuestionTitles().apply(theme);
            t.assertTrue("question is listed", titles.contains(title));
        }
    }

    // ---- Integration tests ----
    static final class IntegrationTests {
        static void run(TestSupport t) {
            System.out.println("-- IntegrationTests --");
            String theme = "IT_" + System.currentTimeMillis();
            {
                DbblDelegate db = DbblDelegate.createDefault();
                db.themeSave().apply(new PersistenceDelegate.ThemeData(theme, "Integration Theme"));
                List<String> answers = List.of("Blue", "Green");
                List<Boolean> flags = List.of(Boolean.TRUE, Boolean.FALSE);
                db.questionSave().apply(new PersistenceDelegate.QuestionData(
                    theme, "Sky color?", "Sky color?", "", answers, flags
                ));
                // Force persist
                db.persistAll().run();
            }
            // New instance simulates a fresh process
            {
                DbblDelegate db2 = DbblDelegate.createDefault();
                List<String> themes2 = db2.uiAllTopics().get();
                t.assertTrue("theme persisted", themes2.contains(theme));
                List<String> titles2 = db2.uiQuestionTitles().apply(theme);
                t.assertTrue("question persisted", !titles2.isEmpty());
            }
        }
    }

    // ---- Smoke tests ----
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
