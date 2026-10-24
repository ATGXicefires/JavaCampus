package campus.story;

import campus.progress.Progress;
import campus.progress.SaveStore;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StoryStateTest {

    private static final String COMMON = """
            [共通]
            旁白。
            """;

    private static final String POV = """
            [開場]
            @背景 電腦教室 傍晚
            @登場 阿成 中 笑
            @特效 夕陽
            【選項】
            A. 好 AP+10 TP+20 ★
            B. 不好
            @旗標 選過了
            @章末 Day 1
            """;

    private static Chapter chapter() {
        return StoryRunnerTest.chapter(COMMON, POV, POV);
    }

    @Test
    void replayingAChapterRestoresTheNumbersAtItsStart() {
        Chapter ch = chapter();
        StoryState s = new StoryState();
        s.begin(ch, Route.SENPAI, false);
        new StoryRunner(ch, Route.SENPAI, s, new HashSet<>(), new StoryRunnerTest.AutoStage().pick(0)).start();
        assertEquals(20, s.ap());
        assertEquals(1, s.keyCount());
        assertTrue(s.hasFlag("選過了"));

        s.begin(ch, Route.SENPAI, true);
        assertEquals(10, s.ap());
        assertEquals(10, s.tp());
        assertEquals(0, s.keyCount());
        assertTrue(!s.hasFlag("選過了"));
        assertNull(s.staging().background());
    }

    @Test
    void reachingAChapterNormallyOverwritesItsStartRecord() {
        Chapter ch = chapter();
        StoryState s = new StoryState();
        s.begin(ch, Route.SENPAI, false);
        new StoryRunner(ch, Route.SENPAI, s, new HashSet<>(), new StoryRunnerTest.AutoStage().pick(0)).start();
        s.begin(ch, Route.SENPAI, false);
        s.begin(ch, Route.SENPAI, true);
        assertEquals(20, s.ap());
        assertEquals(30, s.tp());
    }

    @Test
    void oldSavesWithoutTheSelfExpressionShowANeutralFace() {
        Staging old = new Gson().fromJson("{\"background\":\"電腦教室\",\"actors\":{}}", Staging.class);
        assertEquals("一般", old.selfExpression());
    }

    @Test
    void stateSurvivesSaveAndLoad(@TempDir Path dir) {
        Chapter ch = chapter();
        Progress p = new Progress();
        StoryState s = p.startRoute(Route.KOUHAI, List.of());
        s.begin(ch, Route.KOUHAI, false);
        new StoryRunner(ch, Route.KOUHAI, s, p.read(), new StoryRunnerTest.AutoStage().pick(0)).start();
        p.setLastRoute(Route.KOUHAI);
        p.notes().add("println");
        p.markSolved("ch01-k1", "say");

        SaveStore store = new SaveStore(dir.resolve("progress.json"));
        store.save(p);
        Progress loaded = store.load();

        assertEquals(Route.KOUHAI, loaded.lastRoute());
        StoryState back = loaded.route(Route.KOUHAI);
        assertEquals(s.at(), back.at());
        assertEquals(20, back.ap());
        assertEquals(1, back.keyCount());
        assertTrue(back.atChapterEnd());
        assertEquals("電腦教室", back.staging().background());
        assertEquals("傍晚", back.staging().variant());
        assertEquals("笑", back.staging().actors().get("阿成").expression());
        assertTrue(back.staging().filters().contains("夕陽"));
        assertTrue(loaded.notes().contains("println"));
        assertTrue(loaded.isSolved("ch01-k1", "say"));
        assertEquals(p.read(), loaded.read());

        back.begin(ch, Route.KOUHAI, true);
        assertEquals(10, back.ap());
    }

    @Test
    void startingARouteAgainClearsOnlyThatRoute() {
        Progress p = new Progress();
        p.startRoute(Route.SENPAI, List.of());
        p.startRoute(Route.KOUHAI, List.of());
        p.markSolved("ch01-k1", "say");
        p.markSolved("ch01-s1", "say");
        p.setLastRoute(Route.KOUHAI);

        p.resetRoute(Route.KOUHAI, List.of("ch01-k1"));
        assertNull(p.route(Route.KOUHAI));
        assertNull(p.lastRoute());
        assertTrue(!p.isSolved("ch01-k1", "say"));
        assertTrue(p.isSolved("ch01-s1", "say"));
        assertTrue(p.route(Route.SENPAI) != null);
    }
}
