package campus.engine;

import campus.lesson.Check;
import campus.lesson.SetupVar;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static campus.lesson.Step.Form.PROGRAM;
import static campus.lesson.Step.Form.SNIPPET;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerifierTest {

    private static Sandbox sandbox;
    private static Verifier verifier;

    private static final Check OUTPUT = new Check(Check.Mode.OUTPUT, null, null);
    private static final List<SetupVar> HOUR = List.of(new SetupVar("callHour", "int", "20", null));

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
        Verdict v = verifier.verify("o1", SNIPPET, List.of(), OUTPUT, "System.out.println(\"報到\");", "System.out.println( \"報到\" );   ");
        assertTrue(v.passed(), v.message());
    }

    @Test
    void trailingWhitespaceDoesNotCount() {
        Verdict v = verifier.verify("o2", SNIPPET, List.of(), OUTPUT, "System.out.println(\"報到\");", "System.out.println(\"報到  \");");
        assertTrue(v.passed(), v.message());
    }

    @Test
    void reportsLineCount() {
        Verdict v = verifier.verify("o3", SNIPPET, List.of(), OUTPUT, "System.out.println(\"報到\");",
                "System.out.println(\"報到\");\nSystem.out.println(\"報到\");");
        assertFails(v, "預期輸出 1 行", "2 行");
    }

    @Test
    void reportsNothingPrinted() {
        Verdict v = verifier.verify("o4", SNIPPET, List.of(), OUTPUT, "System.out.println(\"報到\");", "String s = \"報到\";");
        assertFails(v, "沒有印出");
    }

    @Test
    void revealsExpectedWhenOneCharacterOff() {
        Verdict v = verifier.verify("o5", SNIPPET, List.of(), OUTPUT, "System.out.println(\"證人：林雅婷\");",
                "System.out.println(\"證人:林雅婷\");");
        assertFails(v, "把全形的「：」打成了半形的「:」", "應該是「證人：林雅婷」");
    }

    @Test
    void namesTheSpaceThatIsMissingOrExtra() {
        String solution = "System.out.println(\"第 1 遍：罰寫\");";
        assertFails(verifier.verify("o7", SNIPPET, List.of(), OUTPUT, solution, "System.out.println(\"第 1遍：罰寫\");"),
                "第 1 行在「1」後面少了一個空格", "應該是「第␣1␣遍：罰寫」", "你印的是「第␣1遍：罰寫」");
        assertFails(verifier.verify("o7", SNIPPET, List.of(), OUTPUT, solution, "System.out.println(\" 第 1 遍：罰寫\");"),
                "第 1 行開頭多了一個空格");
        assertFails(verifier.verify("o7", SNIPPET, List.of(), OUTPUT, solution, "System.out.println(\"第1遍：罰寫\");"),
                "第 1 行有 2 個地方不一樣：在「第」後面少了一個空格，在「1」後面少了一個空格。");
    }

    @Test
    void reportsBrokenProgram() {
        Verdict v = verifier.verify("o6", SNIPPET, List.of(), OUTPUT, "System.out.println(1);", "System.out.println(1)");
        assertFails(v, "錯誤");
    }

    @Test
    void valuesPass() {
        Check check = new Check(Check.Mode.VALUES, List.of("total"), null);
        Verdict v = verifier.verify("v1", SNIPPET, HOUR, check, "int total = callHour * 60;", "int total = 60 * callHour;");
        assertTrue(v.passed(), v.message());
    }

    @Test
    void valuesReportMissingVariable() {
        Check check = new Check(Check.Mode.VALUES, List.of("gap"), null);
        Verdict v = verifier.verify("v2", SNIPPET, List.of(), check, "int gap = 14;", "int gapp = 14;");
        assertFails(v, "找不到 `gap`");
    }

    @Test
    void valuesReportWrongType() {
        Check check = new Check(Check.Mode.VALUES, List.of("gap"), null);
        Verdict v = verifier.verify("v3", SNIPPET, List.of(), check, "int gap = 14;", "String gap = \"14\";");
        assertFails(v, "應該是 int", "String");
    }

    @Test
    void valuesReportWrongValue() {
        Check check = new Check(Check.Mode.VALUES, List.of("gap"), null);
        Verdict v = verifier.verify("v4", SNIPPET, List.of(), check, "int gap = 14;", "int gap = 15;");
        assertFails(v, "值不對", "15");
    }

    @Test
    void variantCatchesHardcodedAnswer() {
        Check check = new Check(Check.Mode.VALUES, List.of("total"), List.of(Map.of("callHour", "9")));
        Verdict v = verifier.verify("v5", SNIPPET, HOUR, check, "int total = callHour * 60;", "int total = 1200;");
        assertFails(v, "`callHour` 改成 9 再算一次", "`total` 應該是 540", "你的還是 1200，沒有跟著 `callHour` 變");
        assertTrue(v.variant().containsKey("callHour"));
    }

    @Test
    void variantCountsTheLinesAProgramDidNotFollow() {
        List<SetupVar> times = List.of(new SetupVar("times", "int", "5", null));
        Check check = new Check(Check.Mode.OUTPUT, null, List.of(Map.of("times", "100")));
        String solution = "for (int i = 1; i <= times; i++) {\n    System.out.println(\"罰寫\");\n}";
        String fixed = "for (int i = 0; i < 5; i++) {\n    System.out.println(\"罰寫\");\n}";
        assertFails(verifier.verify("v7", SNIPPET, times, check, solution, fixed),
                "`times` 改成 100 再跑一次，應該印 100 行，你的程式還是印 5 行，沒有跟著 `times` 變。");
    }

    @Test
    void variantTypoLeadsWithTheTypo() {
        Check check = new Check(Check.Mode.OUTPUT, null, List.of(Map.of("callHour", "9")));
        String solution = "if (callHour > 12) {\n    System.out.println(\"晚上好\");\n} else {\n    System.out.println(\"早上好\");\n}";
        String typo = "if (callHour > 12) {\n    System.out.println(\"晚上好\");\n} else {\n    System.out.println(\" 早上好\");\n}";
        Verdict v = verifier.verify("v8", SNIPPET, HOUR, check, solution, typo);
        assertFails(v, "第 1 行開頭多了一個空格", "你印的是「␣早上好」", "這一行是 `callHour` 改成 9 再跑一次時印出來的。");
        assertTrue(v.message().startsWith("第 1 行"), v.message());
    }

    @Test
    void variantCoversBothBranches() {
        Check check = new Check(Check.Mode.OUTPUT, null, List.of(Map.of("callHour", "9")));
        String solution = "if (callHour > 12) {\n    System.out.println(\"晚上\");\n} else {\n    System.out.println(\"早上\");\n}";
        Verdict v = verifier.verify("v6", SNIPPET, HOUR, check, solution, "System.out.println(\"晚上\");");
        assertFails(v, "`callHour` 改成 9 再跑一次", "第 1 行應該是「早上」", "你印的還是「晚上」");
        assertTrue(verifier.verify("v6", SNIPPET, HOUR, check, solution, solution).passed());
    }

    @Test
    void methodCallsAsHiddenTests() {
        Check check = new Check(Check.Mode.VALUES, List.of("isLate(2058)", "isLate(2113)"), null);
        String solution = "boolean isLate(int t) {\n    return t > 2112;\n}";
        assertTrue(verifier.verify("m1", SNIPPET, List.of(), check, solution, "boolean isLate(int time) { return time >= 2113; }").passed());
        assertFails(verifier.verify("m1", SNIPPET, List.of(), check, solution, "boolean isLate(int time) { return false; }"),
                "`isLate(2113)`", "false");
    }

    @Test
    void outputCheckAlsoComparesExprs() {
        Check check = new Check(Check.Mode.OUTPUT, List.of("gacha"), null);
        String solution = "int gacha = 7;\nSystem.out.println(gacha);\ngacha = 8;\nSystem.out.println(gacha);";
        assertTrue(verifier.verify("b1", SNIPPET, List.of(), check, solution, solution).passed());
        assertFails(verifier.verify("b1", SNIPPET, List.of(), check, solution,
                "int gacha = 7;\nSystem.out.println(gacha);\nSystem.out.println(8);"), "`gacha` 的值不對", "7");
        assertFails(verifier.verify("b1", SNIPPET, List.of(), check, solution, "int gacha = 7;\ngacha = 8;"), "沒有印出任何東西");
    }

    @Test
    void voidCallIsJudgedByWhatItPrints() {
        Check check = new Check(Check.Mode.OUTPUT, List.of("say(\"阿成\", \"OH！\")"), null);
        String solution = "static void say(String who, String line) {\n    System.out.println(who + \"：「\" + line + \"」\");\n}\n\n"
                + "say(\"滷肉\", \"加油\");";
        assertTrue(verifier.verify("b2", SNIPPET, List.of(), check, solution,
                "static void say(String w, String l) { System.out.println(w + \"：「\" + l + \"」\"); }\nsay(\"滷肉\", \"加油\");").passed());
        assertFails(verifier.verify("b2", SNIPPET, List.of(), check, solution, "System.out.println(\"滷肉：「加油」\");"),
                "沒辦法執行 `say(\"阿成\", \"OH！\")`");
        assertFails(verifier.verify("b2", SNIPPET, List.of(), check, solution,
                "static void say(String who, String line) {}\nSystem.out.println(\"滷肉：「加油」\");"),
                "應該印出「阿成：「OH！」」", "什麼都沒印");
    }

    @Test
    void variantVoidCallNamesWhatItPrinted() {
        Check check = new Check(Check.Mode.VALUES, List.of("remind()"), List.of(Map.of("callHour", "9")));
        String solution = "static void remind() {\n    System.out.println(\"還有 \" + (21 - callHour) + \" 小時\");\n}";
        assertTrue(verifier.verify("b3", SNIPPET, HOUR, check, solution, solution).passed());
        assertFails(verifier.verify("b3", SNIPPET, HOUR, check, solution, "static void remind() { System.out.println(\"還有 1 小時\"); }"),
                "`callHour` 改成 9", "應該印出「還有␣12␣小時」", "你的印出「還有␣1␣小時」");
    }

    private static final String LEAVE_SOLUTION = """
            public class Main {
                static int leaveTime(int hour, int minute, int drive) {
                    return hour * 60 + minute - drive;
                }

                public static void main(String[] args) {
                    System.out.println(leaveTime(22, 20, 25));
                }
            }
            """;

    @Test
    void programPassesHiddenTests() {
        Check check = new Check(Check.Mode.VALUES, List.of("Main.leaveTime(22, 20, 25)", "Main.leaveTime(21, 10, 40)"), null);
        String mine = """
                import java.util.*;

                public class Main {
                    static int leaveTime(int h, int m, int d) {
                        int total = h * 60 + m;
                        return total - d;
                    }

                    public static void main(String[] args) {
                    }
                }
                """;
        assertTrue(verifier.verify("p1", PROGRAM, List.of(), check, LEAVE_SOLUTION, mine).passed());
    }

    @Test
    void programThatNeverEndsFails() {
        String solution = "public class Main {\n    public static void main(String[] args) {\n"
                + "        System.out.println(\"第 30 天\");\n    }\n}\n";
        String endless = "public class Main {\n    public static void main(String[] args) {\n"
                + "        int day = 0;\n        while (true) {\n            day++;\n        }\n    }\n}\n";
        assertFails(verifier.verify("p3", PROGRAM, List.of(), OUTPUT, solution, endless), "跑太久");
    }

    @Test
    void programHardcodedAnswerFailsHiddenTest() {
        Check check = new Check(Check.Mode.VALUES, List.of("Main.leaveTime(22, 20, 25)", "Main.leaveTime(21, 10, 40)"), null);
        String hardcoded = """
                public class Main {
                    static int leaveTime(int h, int m, int d) {
                        return 1315;
                    }

                    public static void main(String[] args) {
                    }
                }
                """;
        assertFails(verifier.verify("p2", PROGRAM, List.of(), check, LEAVE_SOLUTION, hardcoded),
                "`Main.leaveTime(21, 10, 40)`", "1315");
    }
}
