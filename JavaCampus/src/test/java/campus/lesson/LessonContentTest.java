package campus.lesson;

import campus.engine.Sandbox;
import campus.engine.Verdict;
import campus.engine.Verifier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 每個步驟都要能用自己的解答過關，而且不能不寫新東西就過關。 */
@Execution(ExecutionMode.CONCURRENT)
class LessonContentTest {

    private static final Set<String> GENTLE_PREFIXES = Set.of("ch11-kt", "ch11-sg", "ch12-kt", "ch12-sb");
    private static final Set<String> GENTLE_STEPS = Set.of(
            "ch01-k1/say", "ch01-k1/two-lines", "ch01-s1/rewrite", "ch02-k3/first");

    private static final Pattern MINI_MD_INLINE = Pattern.compile("\\*\\*(.+?)\\*\\*|`([^`]+)`");

    private record Worker(Sandbox sandbox, Verifier verifier) {
    }

    private static final List<Sandbox> sandboxes = new CopyOnWriteArrayList<>();
    private static final ThreadLocal<Worker> worker = ThreadLocal.withInitial(() -> {
        Sandbox s = new Sandbox(Duration.ofSeconds(3));
        sandboxes.add(s);
        return new Worker(s, new Verifier(s));
    });
    private static LessonLibrary library;

    @BeforeAll
    static void load() throws IOException {
        library = LessonLibrary.load(Path.of("lessons"));
    }

    @AfterAll
    static void close() {
        sandboxes.forEach(Sandbox::close);
        sandboxes.clear();
    }

    @TestFactory
    List<DynamicTest> everyStep() {
        List<DynamicTest> tests = new ArrayList<>();
        for (Lesson l : library.lessons().values()) {
            List<SourceFile> previous = null;
            for (Step step : l.steps()) {
                List<SourceFile> before = step.starter().isEmpty() ? previous : step.starter();
                tests.add(DynamicTest.dynamicTest(l.key(step), () -> checkStep(l, step, before)));
                previous = step.solution();
            }
        }
        return tests;
    }

    private static void checkStep(Lesson l, Step step, List<SourceFile> before) {
        Verifier verifier = worker.get().verifier();
        Verdict solved = verifier.verify(l.key(step), step.form(), l.vars(), step.check(), step.solution(), step.solution());
        assertTrue(solved.passed(), "解答沒有通過：" + solved.message());

        assertTrue(before != null, "第一個步驟必須有 starter");
        Verdict untouched = verifier.verify(l.key(step), step.form(), l.vars(), step.check(), step.solution(), before);
        assertFalse(untouched.passed(), "不用改程式就能過關");

        assertTrue(step.skeleton().stream().anyMatch(f -> f.text().contains("____")), "骨架沒有挖空");

        assertFalse(step.hints().lead().isBlank(), "缺少提點");
        assertFalse(step.hints().plain().isBlank(), "缺少白話提示");
        assertFalse(step.goal().isBlank(), "缺少這一步要做的事");
        assertFalse(step.instructions().isBlank(), "缺少題目說明");
        assertTrue(step.expectedShape().contains("…"), "預期的樣子直接寫出答案，玩家要寫的地方要換成 …");
        assertAntiBypass(l.key(step), step.check());
        // 換資料再跑的事交給沒過時的訊息說明，題目先講只會讓人以為要對付檢查；除錯題也不直接數出地雷或劇透定位
        List<String> forbidden = List.of(
                "按「完成」", "隱藏測資", "隱藏的", "隱藏 Bug", "隱藏Bug", "隱藏 bug",
                "邏輯地雷", "常見地雷", "三個陷阱", "四種陷阱");
        for (String text : List.of(step.goal(), step.instructions())) {
            for (String word : forbidden) {
                assertFalse(text.contains(word), l.key(step) + " 題目文字不應劇透或提及隱藏檢查（命中「" + word + "」）");
            }
            for (String para : text.replace("\r\n", "\n").split("\n\\s*\n")) {
                Matcher m = MINI_MD_INLINE.matcher(para);
                while (m.find()) {
                    String bold = m.group(1);
                    assertFalse(bold != null && bold.contains("`"),
                            l.key(step) + " 粗體 **...** 內不可再嵌套 `...`，會導致畫面印出反引號：" + bold);
                }
            }
        }
    }

    private static void assertAntiBypass(String key, Check check) {
        boolean gentle = GENTLE_STEPS.contains(key)
                || GENTLE_PREFIXES.stream().anyMatch(key::startsWith);
        if (gentle) {
            return;
        }
        if (check.type() == Check.Mode.OUTPUT) {
            boolean guarded = !check.variants().isEmpty()
                    || !check.exprs().isEmpty()
                    || !check.uses().isEmpty();
            assertTrue(guarded, "不可僅靠靜態 output 過關，請補 variants / exprs / uses：" + key);
        }
        if (check.type() == Check.Mode.VALUES
                && check.variants().isEmpty()
                && check.uses().isEmpty()
                && check.exprs().stream().anyMatch(e -> e.contains("("))) {
            assertTrue(check.exprs().size() >= 2,
                    "僅靠單一方法/建構式 expr 易被寫死繞過，請補第二組邊界 expr 或 uses：" + key);
        }
    }

    /** 題目變數寫在編輯器最上面，行尾註解說它裝的是什麼。 */
    @TestFactory
    List<DynamicTest> everyGivenVariableSaysWhatItHolds() {
        return library.lessons().values().stream().flatMap(l -> l.vars().stream()
                .map(v -> DynamicTest.dynamicTest(l.id() + "/" + v.name(),
                        () -> assertFalse(v.note() == null || v.note().isBlank(), "缺少 note")))).toList();
    }

    /** 筆記欄平常只顯示這一句，太長就失去「先看重點」的意思了。 */
    @TestFactory
    List<DynamicTest> everyNoteHasShortSummary() {
        return library.handbook().entries().stream().map(e -> DynamicTest.dynamicTest(e.id(), () -> {
            assertTrue(e.summary() != null && !e.summary().isBlank(), "缺少重點");
            assertTrue(e.summary().replace("`", "").length() <= 25, "重點太長：" + e.summary());
        })).toList();
    }
}
