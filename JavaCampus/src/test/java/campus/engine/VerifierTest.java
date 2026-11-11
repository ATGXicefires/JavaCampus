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

    private static final List<SetupVar> SCORE = List.of(new SetupVar("score", "int", "85", null));

    /** ch03-k4 的等第，{@code %s} 是判斷 90 分的比較。 */
    private static final String GRADE = """
            if (score %s 90) {
                System.out.println("A");
            } else if (score >= 80) {
                System.out.println("B");
            } else if (score >= 70) {
                System.out.println("C");
            } else if (score >= 60) {
                System.out.println("D");
            } else {
                System.out.println("F");
            }
            """;

    @Test
    void variantTellsABoundaryMistakeFromAHardcodedAnswer() {
        Check check = new Check(Check.Mode.OUTPUT, null, List.of(Map.of("score", "95"), Map.of("score", "90"),
                Map.of("score", "80"), Map.of("score", "72"), Map.of("score", "70"), Map.of("score", "60"),
                Map.of("score", "59")));
        String solution = GRADE.formatted(">=");
        assertFails(verifier.verify("g1", SNIPPET, SCORE, check, solution, GRADE.formatted(">")),
                "`score` 改成 90 再跑一次，第 1 行應該是「A」，你印的是「B」。");
        assertFails(verifier.verify("g1", SNIPPET, SCORE, check, solution, "System.out.println(\"B\");"),
                "`score` 改成 95 再跑一次，第 1 行應該是「A」，你印的還是「B」，沒有跟著 `score` 變。");
    }

    @Test
    void variantLooksAtTheLaterValuesBeforeSayingItDidNotFollow() {
        List<SetupVar> gacha = List.of(new SetupVar("gacha", "int", "8", null));
        Check check = new Check(Check.Mode.VALUES, List.of("perHook"), List.of(Map.of("gacha", "10"), Map.of("gacha", "12")));
        // 除以 4 在 10 個時剛好也是 2，換成 12 個才看得出有跟著 gacha 變
        assertFails(verifier.verify("h1", SNIPPET, gacha, check, "int perHook = gacha / 3;", "int perHook = gacha / 4;"),
                "`gacha` 改成 10 再算一次，`perHook` 應該是 3，你的是 2。");
    }

    @Test
    void variantOnlyComparesRunsThatChangedTheSameVariable() {
        List<SetupVar> week = List.of(new SetupVar("perDay", "int", "3", null), new SetupVar("days", "int", "5", null));
        Check check = new Check(Check.Mode.VALUES, List.of("perWeek"), List.of(Map.of("perDay", "2"), Map.of("days", "4")));
        assertFails(verifier.verify("w1", SNIPPET, week, check, "int perWeek = perDay * days;", "int perWeek = perDay * 5;"),
                "`days` 改成 4 再算一次，`perWeek` 應該是 12，你的還是 15，沒有跟著 `days` 變。");
    }

    @Test
    void wrongBranchIsNotCalledATypo() {
        Check check = new Check(Check.Mode.OUTPUT, null, List.of(Map.of("score", "90"), Map.of("score", "60")));
        String solution = """
                if (score >= 90) {
                    System.out.println(score + " 分是 A");
                } else if (score >= 80) {
                    System.out.println(score + " 分是 B");
                } else if (score >= 70) {
                    System.out.println(score + " 分是 C");
                } else if (score >= 60) {
                    System.out.println(score + " 分是 D");
                } else {
                    System.out.println(score + " 分是 F");
                }
                """;
        assertFails(verifier.verify("g3", SNIPPET, SCORE, check, solution, solution.replace(">= 90", "> 90")),
                "`score` 改成 90 再跑一次，第 1 行應該是「90␣分是␣A」，你印的是「90␣分是␣B」。");
        assertFails(verifier.verify("g3", SNIPPET, SCORE, check, solution,
                "if (score >= 60) {\n    System.out.println(score + \" 分是 D\");\n}"), "第 1 行不對：你印的是「85␣分是␣D」。");
        assertFails(verifier.verify("g3", SNIPPET, SCORE, check, solution, solution.replace(" 分是 A", " 分是 Ａ")),
                "第 1 行把半形的「A」打成了全形的「Ａ」");
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

    @Test
    void usesIsCheckedOnceTheResultIsRight() {
        Check check = new Check(Check.Mode.VALUES, List.of("count"), null, List.of("while"));
        String solution = "int count = 0;\nwhile (count < 3) {\n    count++;\n}";
        assertTrue(verifier.verify("u1", SNIPPET, List.of(), check, solution, solution).passed());
        assertFails(verifier.verify("u1", SNIPPET, List.of(), check, solution,
                "int count = 0;\nfor (int i = 0; i < 3; i++) {\n    count++;\n}"), "結果都對了", "`while`");
        assertFails(verifier.verify("u1", SNIPPET, List.of(), check, solution, "int count = 2;"), "`count` 的值不對");
    }

    @Test
    void usesSkipsCommentsAndStringsButNotSpacing() {
        Check check = new Check(Check.Mode.OUTPUT, null, null, List.of("private int count"));
        String solution = "class Bag {\n    private int count = 2;\n\n    int count() {\n        return count;\n    }\n}\n\n"
                + "System.out.println(new Bag().count());";
        String spaced = "class Bag {\n    private   int\n        count = 2;\n    int count() { return count; }\n}\n"
                + "System.out.println(new Bag().count());";
        assertTrue(verifier.verify("u2", SNIPPET, List.of(), check, solution, spaced).passed());
        String commented = "// private int count\nclass Bag {\n    int count = 2;\n    int count() { return count; }\n}\n"
                + "String note = \"private int count\";\nSystem.out.println(new Bag().count());";
        assertFails(verifier.verify("u2", SNIPPET, List.of(), check, solution, commented), "`private int count`");
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
