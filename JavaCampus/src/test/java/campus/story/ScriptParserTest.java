package campus.story;

import campus.story.Op.Kind;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptParserTest {

    private static ScriptFile parse(String text) {
        return ScriptParser.parse("test.md", text);
    }

    private static List<Op> ops(String text) {
        return parse(text).scenes().getFirst().ops();
    }

    @Test
    void linesOfEveryKind() {
        List<Op> ops = ops("""
                [抽籤現場]
                亭瑄：「學長，你在跟我說外星語嗎？」
                亭瑄：（瞪）「你再說一次？」
                二弘（OS）：⋯⋯這不像是「做太多」的樣子。
                窗外開始下雨。
                （亭瑄站了一會兒。）
                """);
        assertEquals(5, ops.size());
        Op.Line say = (Op.Line) ops.get(0);
        assertEquals(Kind.SAY, say.kind());
        assertEquals("亭瑄", say.who());
        assertEquals("學長，你在跟我說外星語嗎？", say.text());
        Op.Line action = (Op.Line) ops.get(1);
        assertEquals("瞪", action.action());
        assertEquals("你再說一次？", action.text());
        Op.Line os = (Op.Line) ops.get(2);
        assertEquals(Kind.OS, os.kind());
        assertEquals("二弘", os.who());
        assertEquals(Kind.NARRATION, ((Op.Line) ops.get(3)).kind());
        assertEquals(Kind.DIRECTION, ((Op.Line) ops.get(4)).kind());
        assertEquals(6, ops.get(4).line());
    }

    @Test
    void commentsAndMarkersAreSkipped() {
        ScriptFile f = parse("""
                # Ch01 標題
                > 給自己看的說明

                ---
                [場景]
                --- 旁白 ---
                旁白一句。
                """);
        assertEquals(1, f.scenes().size());
        assertEquals(1, f.scenes().getFirst().ops().size());
    }

    @Test
    void linesBeforeFirstSceneBecomeAnUnnamedScene() {
        ScriptFile f = parse("""
                @共通 [抽籤現場]
                [獨有]
                旁白。
                """);
        assertEquals("", f.scenes().get(0).name());
        assertEquals("獨有", f.scenes().get(1).name());
    }

    @Test
    void menuBodiesAreFlattenedAndMergeAfterTheBlock() {
        List<Op> ops = ops("""
                [留下]
                【選項】
                A. 不會 AP+8 TP+5
                    二弘：「⋯⋯反正已經配對了。」
                B. 會 AP+5 TP+8 ★
                    二弘：「會。」
                    二弘：「意思是⋯⋯我會想辦法。」
                C. 走掉 → [別的場景]
                亭瑄：「⋯⋯嗯。」
                """);
        Op.Menu menu = assertInstanceOf(Op.Menu.class, ops.get(0));
        assertEquals(3, menu.options().size());
        Op.Option a = menu.options().get(0);
        assertEquals("不會", a.text());
        assertEquals(8, a.ap());
        assertEquals(5, a.tp());
        assertEquals(1, a.body());
        assertTrue(menu.options().get(1).key());
        assertEquals("會", menu.options().get(1).text());
        assertEquals("別的場景", menu.options().get(2).target());
        assertEquals(-1, menu.options().get(2).body());
        assertEquals(menu.end(), ((Op.Goto) ops.get(2)).index());
        assertEquals(menu.end(), ((Op.Goto) ops.get(5)).index());
        assertEquals("⋯⋯嗯。", ((Op.Line) ops.get(menu.end())).text());
    }

    @Test
    void onlyBlocksSkipToTheirEnd() {
        List<Op> ops = ops("""
                [場景]
                @只限 學長
                二弘（OS）：我沒有在等。
                @結束
                亭瑄：「那個 PDF，我看了。」
                """);
        Op.Only only = assertInstanceOf(Op.Only.class, ops.get(0));
        assertEquals(Route.SENPAI, only.route());
        assertEquals(2, only.end());
    }

    @Test
    void commandsKeepTheirArguments() {
        List<Op> ops = ops("""
                [場景]
                @背景 電腦教室 傍晚
                @音樂 Debug 日常
                @登場 二弘 右 無言
                @附件 程式基礎教學筆記.pdf → 教學筆記
                @章末 Day 1：教會梁同學什麼是變數。
                @如果 AP>=80 旗標:看到窗戶 → [結局]
                """);
        assertEquals(List.of("電腦教室", "傍晚"), ((Op.Cmd) ops.get(0)).args());
        assertEquals(List.of("Debug 日常"), ((Op.Cmd) ops.get(1)).args());
        assertEquals("無言", ((Op.Cmd) ops.get(2)).arg(2));
        assertEquals(List.of("程式基礎教學筆記.pdf", "教學筆記"), ((Op.Cmd) ops.get(3)).args());
        assertEquals("Day 1：教會梁同學什麼是變數。", ((Op.Cmd) ops.get(4)).arg(0));
        Op.If cond = (Op.If) ops.get(5);
        assertEquals(2, cond.conditions().size());
        assertEquals("結局", cond.scene());
        assertNull(((Op.Cmd) ops.get(0)).arg(2));
    }

    @Test
    void errorsPointAtTheLine() {
        ScriptException unknown = assertThrows(ScriptException.class, () -> parse("""
                [場景]
                旁白。
                @飛走 二弘
                """));
        assertTrue(unknown.getMessage().contains("test.md 第 3 行"), unknown.getMessage());

        ScriptException unclosed = assertThrows(ScriptException.class, () -> parse("""
                [場景]
                @只限 學妹
                旁白。
                """));
        assertTrue(unclosed.getMessage().contains("第 2 行"), unclosed.getMessage());

        assertThrows(ScriptException.class, () -> parse("""
                [場景]
                【選項】
                A. 只有一個
                """));
        assertThrows(ScriptException.class, () -> parse("""
                [場景]
                @跳到 沒有括號
                """));
        assertThrows(ScriptException.class, () -> parse("""
                [場景]
                @登場 二弘 上面
                """));
        assertThrows(ScriptException.class, () -> parse("""
                [場景]
                [場景]
                """));
    }
}
