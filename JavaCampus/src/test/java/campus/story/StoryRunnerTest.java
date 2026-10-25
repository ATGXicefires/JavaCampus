package campus.story;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StoryRunnerTest {

    /** 自動往下播，選項照預先排好的順序選。 */
    static final class AutoStage implements StoryRunner.Stage {
        final List<String> shown = new ArrayList<>();
        final List<String> commands = new ArrayList<>();
        final Deque<Integer> picks = new ArrayDeque<>();
        final List<Boolean> readFlags = new ArrayList<>();
        String chapterEnd;
        int checkpoints;
        boolean holdLines;
        Runnable held;

        AutoStage pick(Integer... choices) {
            picks.addAll(List.of(choices));
            return this;
        }

        @Override
        public void line(Op.Line line, boolean read, Runnable next) {
            shown.add(line.text());
            readFlags.add(read);
            if (holdLines) {
                held = next;
            } else {
                next.run();
            }
        }

        @Override
        public void menu(Op.Menu menu, IntConsumer pick) {
            pick.accept(picks.removeFirst());
        }

        @Override
        public void chose(Op.Option option) {
        }

        @Override
        public void command(Op.Cmd cmd, Runnable next) {
            commands.add(cmd.name() + " " + String.join(" ", cmd.args()));
            next.run();
        }

        @Override
        public void chapterEnd(String note) {
            chapterEnd = note;
        }

        @Override
        public void checkpoint() {
            checkpoints++;
        }
    }

    static Chapter chapter(String common, String senpai, String kouhai) {
        Chapter ch = new Chapter("ch01", 1, "編譯錯誤", 5,
                ScriptParser.parse("ch01-common.md", common),
                ScriptParser.parse("ch01-senpai.md", senpai),
                ScriptParser.parse("ch01-kouhai.md", kouhai));
        StoryLibrary.validate(ch);
        return ch;
    }

    private static final String COMMON = """
            [抽籤現場]
            @登場 二弘 右
            @登場 亭瑄 左
            亭瑄：「學長？」
            @只限 學長
            二弘（OS）：⋯⋯一年級？
            @結束
            @只限 學妹
            亭瑄（OS）：制服好皺。
            @結束
            二弘：「⋯⋯嗯。」
            """;

    private static final String SENPAI = """
            [開場]
            @共通 [抽籤現場]
            [留下]
            【選項】
            A. 不會 AP+8 TP+5 ★
                二弘：「我不習慣留 bug 不修。」
            B. 會 AP+5 TP+8
            二弘：「明天見。」
            @如果 AP>=18 → [高分]
            旁白：普通。
            @章末 Day 1
            [高分]
            旁白：高分。
            @章末 Day 1
            """;

    private static final String KOUHAI = """
            @共通 [抽籤現場]
            @章末 Day 1
            """;

    private static StoryState begin(Chapter ch, Route route) {
        StoryState s = new StoryState();
        s.begin(ch, route, false);
        return s;
    }

    @Test
    void commonScenesPlayTheRouteOnlyBlocksAndReturn() {
        Chapter ch = chapter(COMMON, SENPAI, KOUHAI);
        AutoStage stage = new AutoStage().pick(0);
        StoryState s = begin(ch, Route.SENPAI);
        new StoryRunner(ch, Route.SENPAI, s, new HashSet<>(), stage).start();

        assertEquals(List.of("學長？", "⋯⋯一年級？", "⋯⋯嗯。", "我不習慣留 bug 不修。", "明天見。", "高分。"), stage.shown);
        assertEquals("Day 1", stage.chapterEnd);
        assertTrue(s.atChapterEnd());
        assertTrue(s.cleared().contains("ch01"));
    }

    @Test
    void choicesAddPointsAndKeys() {
        Chapter ch = chapter(COMMON, SENPAI, KOUHAI);
        StoryState s = begin(ch, Route.SENPAI);
        new StoryRunner(ch, Route.SENPAI, s, new HashSet<>(), new AutoStage().pick(1)).start();
        assertEquals(15, s.ap());
        assertEquals(18, s.tp());
        assertEquals(0, s.keyCount());

        StoryState a = begin(ch, Route.SENPAI);
        new StoryRunner(ch, Route.SENPAI, a, new HashSet<>(), new AutoStage().pick(0)).start();
        assertEquals(1, a.keyCount());
    }

    @Test
    void conditionsDecideTheBranch() {
        Chapter ch = chapter(COMMON, SENPAI, KOUHAI);
        AutoStage stage = new AutoStage().pick(1);
        new StoryRunner(ch, Route.SENPAI, begin(ch, Route.SENPAI), new HashSet<>(), stage).start();
        assertEquals("普通。", stage.shown.getLast());
    }

    @Test
    void theRouteCharacterNeverEntersTheStage() {
        Chapter ch = chapter(COMMON, SENPAI, KOUHAI);
        AutoStage senpai = new AutoStage().pick(0);
        new StoryRunner(ch, Route.SENPAI, begin(ch, Route.SENPAI), new HashSet<>(), senpai).start();
        assertEquals(List.of("登場 亭瑄 左"), senpai.commands);

        AutoStage kouhai = new AutoStage();
        StoryState k = begin(ch, Route.KOUHAI);
        new StoryRunner(ch, Route.KOUHAI, k, new HashSet<>(), kouhai).start();
        assertEquals(List.of("登場 二弘 右"), kouhai.commands);
        assertEquals(List.of("學長？", "制服好皺。", "⋯⋯嗯。"), kouhai.shown);
        assertEquals(Set.of("二弘"), k.staging().actors().keySet());
    }

    @Test
    void theRouteCharactersExpressionIsRememberedWithoutPuttingHimOnStage() {
        String common = """
                [抽籤現場]
                @登場 二弘 右 冒汗
                二弘：「一。」
                @表情 吳二弘 害羞
                二弘：「二。」
                @退場 二弘
                二弘：「三。」
                @登場 二弘 左
                二弘：「四。」
                """;
        Chapter ch = chapter(common, SENPAI, KOUHAI);
        StoryState s = begin(ch, Route.SENPAI);
        AutoStage stage = new AutoStage();
        stage.holdLines = true;
        new StoryRunner(ch, Route.SENPAI, s, new HashSet<>(), stage).start();
        assertEquals("冒汗", s.staging().selfExpression());
        stage.held.run();
        assertEquals("害羞", s.staging().selfExpression());
        stage.held.run();
        assertEquals("害羞", s.staging().selfExpression());
        stage.held.run();
        assertEquals("一般", s.staging().selfExpression());
        assertTrue(s.staging().actors().isEmpty());
        assertTrue(stage.commands.isEmpty());

        StoryState k = begin(ch, Route.KOUHAI);
        new StoryRunner(ch, Route.KOUHAI, k, new HashSet<>(), new AutoStage()).start();
        assertEquals("一般", k.staging().selfExpression());
        assertEquals("一般", k.staging().actors().get("二弘").expression());
    }

    @Test
    void commonLinesReadInOneRouteCountAsReadInTheOther() {
        Chapter ch = chapter(COMMON, SENPAI, KOUHAI);
        Set<String> read = new HashSet<>();
        new StoryRunner(ch, Route.SENPAI, begin(ch, Route.SENPAI), read, new AutoStage().pick(0)).start();
        AutoStage kouhai = new AutoStage();
        new StoryRunner(ch, Route.KOUHAI, begin(ch, Route.KOUHAI), read, kouhai).start();
        assertEquals(List.of(true, false, true), kouhai.readFlags);
    }

    @Test
    void skipStopsBeforeTheNextLessonOrChoiceAndCollectsSummaries() {
        String senpai = """
                [開場]
                二弘：「一。」
                @登場 阿成 左
                @簡介 第一段。
                二弘：「二。」
                @簡介 第二段。
                @程式 ch01-s1
                二弘：「三。」
                @簡介 第三段。
                【選項】
                A. 好 AP+1
                B. 不好
                @章末 Day 1
                """;
        Chapter ch = chapter(COMMON, senpai, KOUHAI);
        StoryState s = begin(ch, Route.SENPAI);
        Set<String> read = new HashSet<>();
        AutoStage stage = new AutoStage().pick(0);
        stage.holdLines = true;
        StoryRunner runner = new StoryRunner(ch, Route.SENPAI, s, read, stage);
        runner.start();

        assertEquals(List.of("第一段。", "第二段。"), runner.skip());
        assertEquals(List.of("一。"), stage.shown);
        assertTrue(stage.commands.isEmpty());
        assertEquals(Set.of("阿成"), s.staging().actors().keySet());
        assertEquals(5, s.at().index());

        runner.start();
        assertEquals(List.of("程式 ch01-s1"), stage.commands);
        assertEquals(List.of("一。", "三。"), stage.shown);

        assertEquals(List.of("第三段。"), runner.skip());
        assertEquals(1, stage.picks.size());
        // 跳過的「二。」不算讀過
        assertEquals(2, read.size());
        assertFalse(s.atChapterEnd());

        runner.start();
        assertEquals(11, s.ap());
        assertEquals("Day 1", stage.chapterEnd);
    }

    @Test
    void positionStaysOnALineUntilThePlayerMovesOn() {
        Chapter ch = chapter(COMMON, SENPAI, KOUHAI);
        StoryState s = begin(ch, Route.KOUHAI);
        AutoStage stage = new AutoStage();
        stage.holdLines = true;
        new StoryRunner(ch, Route.KOUHAI, s, new HashSet<>(), stage).start();
        assertEquals(List.of("學長？"), stage.shown);
        StoryState.Pos waiting = s.at();
        assertEquals("ch01-common.md", waiting.file());
        assertEquals(2, waiting.index());
        assertFalse(s.atChapterEnd());

        stage.held.run();
        assertEquals(2, stage.shown.size());
        assertEquals(6, s.at().index());
    }
}
