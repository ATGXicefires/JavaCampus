package campus.progress;

import campus.story.Route;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressTest {

    @Test
    void practiceAndStoryKeepSeparateRecords() {
        Progress p = new Progress();
        p.storyLessons().markSolved("ch01-k1", "say");
        p.storyLessons().setCode("ch01-k1", "say", "System.out.println(\"劇情\");");
        p.practiceLessons().setHintsRevealed("ch01-k1", "say", 2);

        assertFalse(p.practiceLessons().isSolved("ch01-k1", "say"));
        assertNull(p.practiceLessons().code("ch01-k1", "say"));
        assertEquals(0, p.storyLessons().hintsRevealed("ch01-k1", "say"));
        assertEquals(2, p.practiceLessons().hintsRevealed("ch01-k1", "say"));
    }

    @Test
    void bothRecordsSurviveSaveAndLoad(@TempDir Path dir) {
        Progress p = new Progress();
        p.storyLessons().markSolved("ch01-k1", "say");
        p.practiceLessons().markSolved("ch01-s1", "box");
        p.practiceLessons().setCode("ch01-s1", "box", "int gacha = 7;");
        SaveStore store = new SaveStore(dir.resolve("progress.json"));
        store.save(p);

        Progress loaded = store.load();
        assertTrue(loaded.storyLessons().isSolved("ch01-k1", "say"));
        assertFalse(loaded.storyLessons().isSolved("ch01-s1", "box"));
        assertTrue(loaded.practiceLessons().isSolved("ch01-s1", "box"));
        assertEquals("int gacha = 7;", loaded.practiceLessons().code("ch01-s1", "box"));
    }

    @Test
    void oldSavesWithoutPracticeStillLoad(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("progress.json");
        Files.writeString(file, """
                {
                  "lastRoute": "KOUHAI",
                  "notes": ["println"],
                  "lessons": { "ch01-k1": { "solved": ["say"], "hints": { "say": 1 }, "code": { "say": "x" } } },
                  "settings": { "volume": 0.5 }
                }
                """, UTF_8);

        Progress old = new SaveStore(file).load();
        assertTrue(old.storyLessons().isSolved("ch01-k1", "say"));
        assertEquals("x", old.storyLessons().code("ch01-k1", "say"));
        assertTrue(old.practiceLessons().isEmpty());
        old.practiceLessons().markSolved("ch01-k1", "say");
        assertTrue(old.practiceLessons().isSolved("ch01-k1", "say"));
    }

    @Test
    void resettingPracticeClearsOnlyPractice() {
        Progress p = new Progress();
        Progress.LessonRecord practice = p.practiceLessons();
        practice.markSolved("ch01-k1", "say");
        p.storyLessons().markSolved("ch01-k1", "say");
        p.notes().add("println");

        p.resetPractice();
        assertTrue(practice.isEmpty());
        assertFalse(p.practiceLessons().isSolved("ch01-k1", "say"));
        assertTrue(p.storyLessons().isSolved("ch01-k1", "say"));
        assertTrue(p.notes().contains("println"));
    }

    @Test
    void newGameAndRouteResetLeavePracticeAlone() {
        Progress p = new Progress();
        p.startRoute(Route.KOUHAI, List.of());
        p.storyLessons().markSolved("ch01-k1", "say");
        p.practiceLessons().markSolved("ch01-k1", "say");

        p.startRoute(Route.KOUHAI, List.of("ch01-k1"));
        assertFalse(p.storyLessons().isSolved("ch01-k1", "say"));
        assertTrue(p.practiceLessons().isSolved("ch01-k1", "say"));

        p.storyLessons().markSolved("ch01-k1", "say");
        p.resetRoute(Route.KOUHAI, List.of("ch01-k1"));
        assertFalse(p.storyLessons().isSolved("ch01-k1", "say"));
        assertTrue(p.practiceLessons().isSolved("ch01-k1", "say"));
    }
}
