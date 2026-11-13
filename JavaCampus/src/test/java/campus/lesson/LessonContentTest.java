package campus.lesson;

import campus.engine.Sandbox;
import campus.engine.Verdict;
import campus.engine.Verifier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 每個步驟都要能用自己的解答過關，而且不能不寫新東西就過關。 */
class LessonContentTest {

    private static final Set<String> GENTLE_PREFIXES = Set.of("ch01-", "ch11-kt", "ch11-sg", "ch12-kt", "ch12-sb");
    private static final Set<String> GENTLE_STEPS = Set.of("ch02-k3/first");

    private static Sandbox sandbox;
    private static Verifier verifier;
    private static LessonLibrary library;

    @BeforeAll
    static void load() throws IOException {
        sandbox = new Sandbox(Duration.ofSeconds(3));
        verifier = new Verifier(sandbox);
        library = LessonLibrary.load(Path.of("lessons"));
    }

    @AfterAll
    static void close() {
        sandbox.close();
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
        // 換資料再跑的事交給沒過時的訊息說明，題目先講只會讓人以為要對付檢查
        for (String text : List.of(step.goal(), step.instructions())) {
            assertFalse(text.contains("按「完成」"), "題目文字預告了隱藏測資：" + text);
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
