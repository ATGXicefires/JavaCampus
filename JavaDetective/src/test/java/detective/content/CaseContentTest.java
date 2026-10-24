package detective.content;

import detective.engine.Sandbox;
import detective.engine.Verdict;
import detective.engine.Verifier;
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
class CaseContentTest {

    private static Sandbox sandbox;
    private static Verifier verifier;
    private static List<Chapter> chapters;

    @BeforeAll
    static void load() throws IOException {
        sandbox = new Sandbox(Duration.ofSeconds(3));
        verifier = new Verifier(sandbox);
        chapters = CaseLibrary.load(Path.of("cases"));
    }

    @AfterAll
    static void close() {
        sandbox.close();
    }

    @TestFactory
    List<DynamicTest> everyStep() {
        List<DynamicTest> tests = new ArrayList<>();
        for (Chapter chapter : chapters) {
            assertFalse(chapter.cases().isEmpty(), chapter.id() + " 沒有案件");
            for (CaseDef c : chapter.cases()) {
                String previous = null;
                for (Step step : c.steps()) {
                    String before = step.starter().isBlank() ? previous : step.starter();
                    tests.add(DynamicTest.dynamicTest(c.key(step), () -> checkStep(c, step, before)));
                    previous = step.solution();
                }
            }
        }
        return tests;
    }

    private static void checkStep(CaseDef c, Step step, String before) {
        Verdict solved = verifier.verify(c.key(step), step.form(), c.vars(), step.check(), step.solution(), step.solution());
        assertTrue(solved.passed(), "解答沒有通過：" + solved.message());

        assertTrue(before != null, "第一個步驟必須有 starter");
        Verdict untouched = verifier.verify(c.key(step), step.form(), c.vars(), step.check(), step.solution(), before);
        assertFalse(untouched.passed(), "不用改程式就能過關");

        assertTrue(step.skeleton().contains("____"), "骨架沒有挖空");
        assertFalse(step.hints().lead().isBlank(), "缺少 lead 提示");
        assertFalse(step.hints().plain().isBlank(), "缺少白話提示");
        assertFalse(step.instructions().isBlank(), "缺少說明");
    }
}
