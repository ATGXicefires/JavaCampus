package campus.ui;

import campus.lesson.Lesson;
import campus.lesson.Step;
import campus.progress.Progress;
import campus.story.StoryLibrary;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PracticeShellTest {

    private static List<StoryLibrary.Listing> chapters;

    private final Progress progress = new Progress();

    @BeforeAll
    static void loadChapters() throws IOException {
        chapters = StoryLibrary.load(Path.of("..", "story", "scripts")).contents();
    }

    private static Lesson lesson(String id, String route, String title) {
        Step step = new Step("only", "唯一的一步", "照做", "", "…", Step.Form.SNIPPET, null, null, "", List.of(),
                List.of(), List.of());
        return new Lesson(id, title, route, "二弘", List.of(), List.of(), List.of(step));
    }

    private static final Lesson K1 = lesson("ch02-k1", "學妹", "int 是什麼");
    private static final Lesson K2 = lesson("ch02-k2", "學妹", "蠢事做一百遍");
    private static final Lesson S1 = lesson("ch02-s1", "學長", "白板上的 int");
    private static final Lesson ENDING = lesson("ch11-kt", "學妹", "結局小題");
    private static final Lesson CH11 = lesson("ch11-k1", "學妹", "平常的題目");

    private PracticeShell shell(Lesson... lessons) {
        return new PracticeShell(chapters, List.of(lessons), progress.storyLessons(), progress.practiceLessons());
    }

    private static List<String> texts(PracticeShell.Result r) {
        return r.lines().stream().map(PracticeShell.Line::text).toList();
    }

    @Test
    void lsAtTheRootListsAllTwelveChaptersWithTheirTitles() {
        PracticeShell.Result r = shell(K1).execute("ls");
        assertEquals("eh@xiaobai:~/lessons$ ls", texts(r).getFirst());
        List<PracticeShell.Line> listed = r.lines().subList(1, r.lines().size());
        assertEquals(12, listed.size());
        assertEquals("  ch01  編譯錯誤", listed.getFirst().text());
        assertEquals("  ch02  Hello World", listed.get(1).text());
        assertEquals("  ch12  Release", listed.getLast().text());
        assertEquals("ch02", listed.get(1).link());
    }

    @Test
    void aChapterFolderListsBothRoutesAndTicksWhatPracticeFinished() {
        progress.practiceLessons().markSolved("ch02-k1", "only");
        progress.storyLessons().markSolved("ch02-k2", "only");
        PracticeShell s = shell(S1, K2, K1);
        s.execute("cd ch02");
        assertEquals("eh@xiaobai: ~/lessons/ch02", s.title());
        PracticeShell.Result r = s.execute("ls");
        assertEquals(List.of(
                "eh@xiaobai:~/lessons/ch02$ ls",
                "  學妹",
                "    k1  int 是什麼    ✓",
                "    k2  蠢事做一百遍",
                "  學長",
                "    s1  白板上的 int"), texts(r));
        assertEquals("ch02-k1", r.lines().get(2).link());
        assertNull(r.lines().get(1).link());
    }

    @Test
    void ticksLineUpInOneColumnWhateverTheTitleWidth() {
        progress.practiceLessons().markSolved("ch02-k1", "only");
        progress.practiceLessons().markSolved("ch02-k2", "only");
        PracticeShell s = shell(K1, K2);
        s.execute("cd ch02");
        List<String> lines = texts(s.execute("ls"));
        assertEquals("    k1  int 是什麼    ✓", lines.get(2));
        assertEquals("    k2  蠢事做一百遍  ✓", lines.get(3));
    }

    @Test
    void runTakesTheShortNameInsideAChapterOrTheFullIdAnywhere() {
        PracticeShell s = shell(K1, K2, S1);
        s.execute("cd ch02");
        PracticeShell.Result inside = s.execute("run k2");
        assertEquals(PracticeShell.Action.RUN, inside.action());
        assertEquals(K2, inside.lesson());
        assertEquals("eh@xiaobai:~/lessons/ch02$ run k2", texts(inside).getFirst());

        s.execute("cd ..");
        PracticeShell.Result fromRoot = s.execute("run ch02-k2");
        assertEquals(PracticeShell.Action.RUN, fromRoot.action());
        assertEquals(K2, fromRoot.lesson());

        s.execute("cd ch05");
        assertEquals(S1, s.execute("run ch02-s1").lesson());
    }

    @Test
    void endingLessonsStayHiddenUntilFinishedInTheStory() {
        PracticeShell s = shell(ENDING, CH11);
        progress.practiceLessons().markSolved("ch11-kt", "only");
        s.execute("cd ch11");
        assertEquals(List.of("eh@xiaobai:~/lessons/ch11$ ls", "  學妹", "    k1  平常的題目"), texts(s.execute("ls")));
        assertEquals(PracticeShell.Action.NONE, s.execute("run kt").action());
        assertEquals("run k1 ", s.complete("run k").input());
        s.execute("cd ~");
        assertEquals(PracticeShell.Action.NONE, s.execute("run ch11-kt").action());

        progress.storyLessons().markSolved("ch11-kt", "only");
        s.execute("cd ch11");
        assertTrue(texts(s.execute("ls")).contains("    kt  結局小題    ✓"));
        assertEquals(ENDING, s.execute("run kt").lesson());
    }

    @Test
    void mistakesPrintOneErrorLine() {
        PracticeShell s = shell(K1);
        for (String typo : List.of("foo", "cd ch13", "ls nowhere", "run", "run k1", "run ch02-k9")) {
            PracticeShell.Result r = s.execute(typo);
            assertEquals(2, r.lines().size(), typo);
            assertEquals(PracticeShell.Style.ERROR, r.lines().get(1).spans().getFirst().style(), typo);
            assertEquals(PracticeShell.Action.NONE, r.action(), typo);
        }
        assertEquals("eh@xiaobai: ~/lessons", s.title());
        s.execute("cd ch02");
        PracticeShell.Result missing = s.execute("run k9");
        assertEquals("run: k9: 沒有這一題", missing.lines().get(1).text());
    }

    @Test
    void cdMovesBetweenChaptersAndBackUp() {
        PracticeShell s = shell(K1);
        s.execute("cd ch02");
        assertEquals("eh@xiaobai: ~/lessons/ch02", s.title());
        s.execute("cd ..");
        assertEquals("eh@xiaobai: ~/lessons", s.title());
        PracticeShell.Result stay = s.execute("cd ..");
        assertEquals(1, stay.lines().size());
        assertEquals("eh@xiaobai: ~/lessons", s.title());

        s.execute("cd ch02");
        s.execute("cd ../ch03");
        assertEquals("eh@xiaobai: ~/lessons/ch03", s.title());
        s.execute("cd ~");
        assertEquals("eh@xiaobai: ~/lessons", s.title());
        s.execute("cd ch04/");
        s.execute("cd");
        assertEquals("eh@xiaobai: ~/lessons", s.title());
    }

    @Test
    void tabCompletesCommandsChaptersAndLessons() {
        PracticeShell s = shell(K1, K2, S1);
        assertEquals("run ", s.complete("r").input());
        assertEquals("clear ", s.complete("cl").input());
        PracticeShell.Completion c = s.complete("c");
        assertEquals("c", c.input());
        assertEquals(List.of("cd", "clear"), c.options());
        assertEquals("cd ch12 ", s.complete("cd ch12").input());
        assertEquals(List.of("ch10", "ch11", "ch12"), s.complete("cd ch1").options());
        assertEquals("run ch02-s1 ", s.complete("run ch02-s").input());
        assertEquals(List.of(), s.complete("ls x").options());

        s.execute("cd ch02");
        assertEquals("run s1 ", s.complete("run s").input());
        assertEquals(List.of("k1", "k2"), s.complete("run k").options());
        assertEquals("run ch02-", s.complete("run c").input());
    }

    @Test
    void arrowKeysWalkThroughTypedCommands() {
        PracticeShell s = shell(K1);
        s.execute("ls");
        s.execute("cd ch02");
        s.execute("cd ch02");
        s.execute("ls");
        assertEquals("ls", s.older("ru"));
        assertEquals("cd ch02", s.older("ls"));
        assertEquals("ls", s.older("cd ch02"));
        assertEquals("ls", s.older("ls"));
        assertEquals("cd ch02", s.newer("ls"));
        assertEquals("ls", s.newer("cd ch02"));
        assertEquals("ru", s.newer("ls"));
        assertEquals("ru", s.newer("ru"));
    }

    @Test
    void clickingAListedItemTypesTheMatchingCommand() {
        PracticeShell s = shell(K1, K2);
        PracticeShell.Result chapter = s.click("ch02");
        assertEquals("eh@xiaobai:~/lessons$ cd ch02", texts(chapter).getFirst());
        assertEquals("eh@xiaobai:~/lessons/ch02$ ls", texts(chapter).get(1));
        assertEquals("ls", s.older(""));
        assertEquals("cd ch02", s.older("ls"));

        PracticeShell.Result inside = s.click("ch02-k1");
        assertEquals("eh@xiaobai:~/lessons/ch02$ run k1", texts(inside).getFirst());
        assertEquals(K1, inside.lesson());

        s.execute("cd ..");
        assertEquals("eh@xiaobai:~/lessons$ run ch02-k1", texts(s.click("ch02-k1")).getFirst());
    }

    @Test
    void comingBackFromAFinishedLessonPrintsATick() {
        PracticeShell s = shell(K2);
        assertEquals(List.of(), s.back(K2));
        progress.practiceLessons().markSolved("ch02-k2", "only");
        assertEquals("✓ ch02-k2 蠢事做一百遍", s.back(K2).getFirst().text());
    }

    @Test
    void clearAndExitAreLeftToTheScreen() {
        PracticeShell s = shell(K1);
        PracticeShell.Result clear = s.execute("clear");
        assertEquals(PracticeShell.Action.CLEAR, clear.action());
        assertTrue(clear.lines().isEmpty());
        assertEquals(PracticeShell.Action.EXIT, s.execute("exit").action());
        assertTrue(s.execute("help").lines().size() > 1);
    }
}
