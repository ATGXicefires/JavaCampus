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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 每個步驟都要能用自己的解答過關，而且不能不寫新東西就過關。 */
class LessonContentTest {

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
            String previous = null;
            for (Step step : l.steps()) {
                String before = step.starter().isBlank() ? previous : step.starter();
                tests.add(DynamicTest.dynamicTest(l.key(step), () -> checkStep(l, step, before)));
                previous = step.solution();
            }
        }
        return tests;
    }

    private static void checkStep(Lesson l, Step step, String before) {
        Verdict solved = verifier.verify(l.key(step), step.form(), l.vars(), step.check(), step.solution(), step.solution());
        assertTrue(solved.passed(), "解答沒有通過：" + solved.message());

        assertTrue(before != null, "第一個步驟必須有 starter");
        Verdict untouched = verifier.verify(l.key(step), step.form(), l.vars(), step.check(), step.solution(), before);
        assertFalse(untouched.passed(), "不用改程式就能過關");

        assertTrue(step.skeleton().contains("____"), "骨架沒有挖空");
        assertFalse(step.hints().lead().isBlank(), "缺少提點");
        assertFalse(step.hints().plain().isBlank(), "缺少白話提示");
        assertFalse(step.instructions().isBlank(), "缺少說明");
    }
}
