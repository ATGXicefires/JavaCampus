package detective.engine;

import detective.content.Check;
import detective.content.SetupVar;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerifierTest {

    private static Sandbox sandbox;
    private static Verifier verifier;

    private static final Check OUTPUT = new Check(Check.Mode.OUTPUT, null, null);
    private static final List<SetupVar> HOUR = List.of(new SetupVar("callHour", "int", "20"));

    @BeforeAll
    static void start() {
        sandbox = new Sandbox(Duration.ofSeconds(2));
        verifier = new Verifier(sandbox);
    }

    @AfterAll
    static void stop() {
        sandbox.close();
    }

    private static void assertFails(Verdict v, String... words) {
        assertFalse(v.passed(), "應該不通過");
        for (String w : words) {
            assertTrue(v.message().contains(w), "訊息應該提到「" + w + "」：" + v.message());
        }
    }

    @Test
    void outputPassesWithDifferentSpacing() {
        Verdict v = verifier.verify("o1", List.of(), OUTPUT, "System.out.println(\"報到\");", "System.out.println( \"報到\" );   ");
        assertTrue(v.passed(), v.message());
    }

    @Test
    void trailingWhitespaceDoesNotCount() {
        Verdict v = verifier.verify("o2", List.of(), OUTPUT, "System.out.println(\"報到\");", "System.out.println(\"報到  \");");
        assertTrue(v.passed(), v.message());
    }

    @Test
    void reportsLineCount() {
        Verdict v = verifier.verify("o3", List.of(), OUTPUT, "System.out.println(\"報到\");",
                "System.out.println(\"報到\");\nSystem.out.println(\"報到\");");
        assertFails(v, "預期輸出 1 行", "2 行");
    }

    @Test
    void reportsNothingPrinted() {
        Verdict v = verifier.verify("o4", List.of(), OUTPUT, "System.out.println(\"報到\");", "String s = \"報到\";");
        assertFails(v, "沒有印出");
    }

    @Test
    void revealsExpectedWhenOneCharacterOff() {
        Verdict v = verifier.verify("o5", List.of(), OUTPUT, "System.out.println(\"證人：林雅婷\");",
                "System.out.println(\"證人:林雅婷\");");
        assertFails(v, "差了 1 個字", "證人：林雅婷");
    }

    @Test
    void reportsBrokenProgram() {
        Verdict v = verifier.verify("o6", List.of(), OUTPUT, "System.out.println(1);", "System.out.println(1)");
        assertFails(v, "錯誤");
    }

    @Test
    void valuesPass() {
        Check check = new Check(Check.Mode.VALUES, List.of("total"), null);
        Verdict v = verifier.verify("v1", HOUR, check, "int total = callHour * 60;", "int total = 60 * callHour;");
        assertTrue(v.passed(), v.message());
    }

    @Test
    void valuesReportMissingVariable() {
        Check check = new Check(Check.Mode.VALUES, List.of("gap"), null);
        Verdict v = verifier.verify("v2", List.of(), check, "int gap = 14;", "int gapp = 14;");
        assertFails(v, "找不到 `gap`");
    }

    @Test
    void valuesReportWrongType() {
        Check check = new Check(Check.Mode.VALUES, List.of("gap"), null);
        Verdict v = verifier.verify("v3", List.of(), check, "int gap = 14;", "String gap = \"14\";");
        assertFails(v, "應該是 int", "String");
    }

    @Test
    void valuesReportWrongValue() {
        Check check = new Check(Check.Mode.VALUES, List.of("gap"), null);
        Verdict v = verifier.verify("v4", List.of(), check, "int gap = 14;", "int gap = 15;");
        assertFails(v, "值不對", "15");
    }

    @Test
    void variantCatchesHardcodedAnswer() {
        Check check = new Check(Check.Mode.VALUES, List.of("total"), List.of(Map.of("callHour", "9")));
        Verdict v = verifier.verify("v5", HOUR, check, "int total = callHour * 60;", "int total = 1200;");
        assertFails(v, "換一組資料", "callHour = 9");
        assertTrue(v.variant().containsKey("callHour"));
    }

    @Test
    void variantCoversBothBranches() {
        Check check = new Check(Check.Mode.OUTPUT, null, List.of(Map.of("callHour", "9")));
        String solution = "if (callHour > 12) {\n    System.out.println(\"晚上\");\n} else {\n    System.out.println(\"早上\");\n}";
        Verdict v = verifier.verify("v6", HOUR, check, solution, "System.out.println(\"晚上\");");
        assertFails(v, "換一組資料");
        assertTrue(verifier.verify("v6", HOUR, check, solution, solution).passed());
    }

    @Test
    void methodCallsAsHiddenTests() {
        Check check = new Check(Check.Mode.VALUES, List.of("isLate(2058)", "isLate(2113)"), null);
        String solution = "boolean isLate(int t) {\n    return t > 2112;\n}";
        assertTrue(verifier.verify("m1", List.of(), check, solution, "boolean isLate(int time) { return time >= 2113; }").passed());
        assertFails(verifier.verify("m1", List.of(), check, solution, "boolean isLate(int time) { return false; }"),
                "`isLate(2113)`", "false");
    }
}
