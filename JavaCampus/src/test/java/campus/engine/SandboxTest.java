package campus.engine;

import campus.engine.RunResult.Outcome;
import campus.engine.RunResult.Problem;
import campus.engine.RunResult.Severity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SandboxTest {

    private static Sandbox sandbox;

    @BeforeAll
    static void start() {
        sandbox = new Sandbox(Duration.ofSeconds(1));
    }

    @AfterAll
    static void stop() {
        sandbox.close();
    }

    private static RunResult run(String source) {
        return sandbox.run(List.of(), source, List.of());
    }

    private static Problem error(RunResult r) {
        return r.firstError().orElseThrow(() -> new AssertionError("沒有錯誤：" + r));
    }

    private static void assertMentions(RunResult r, String... words) {
        String message = error(r).message();
        for (String w : words) {
            assertTrue(message.contains(w), "訊息應該提到「" + w + "」：" + message);
        }
    }

    @Test
    void printsChineseOutput() {
        RunResult r = run("System.out.println(\"中文輸出\");");
        assertEquals(Outcome.OK, r.outcome());
        assertEquals("中文輸出\n", r.output().replace("\r\n", "\n"));
    }

    @Test
    void ignoresComments() {
        RunResult r = run("// 註解\nint a = 1; // 尾巴\n/* 區塊 */\nSystem.out.println(a);\n// 最後");
        assertEquals(Outcome.OK, r.outcome(), r.toString());
        assertEquals("1", r.output().strip());
    }

    @Test
    void missingSemicolonOnDeclaration() {
        RunResult r = run("int x = 5");
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        assertMentions(r, "分號");
    }

    @Test
    void missingSemicolonThatJShellWouldAccept() {
        RunResult r = run("int a = 1;\nSystem.out.println(a)");
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        assertMentions(r, "分號");
        assertTrue(error(r).start() > "int a = 1;\n".length(), "錯誤應該標在第二行");
    }

    @Test
    void compileErrorRunsNothing() {
        RunResult r = run("System.out.println(\"a\");\nint x = \"b\";");
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        assertEquals("", r.output());
        assertMentions(r, "String", "int");
    }

    @Test
    void errorPositionMapsToSecondLine() {
        String src = "int a = 1;\nSystem.out.println(b);";
        RunResult r = run(src);
        assertEquals(src.indexOf("b)"), error(r).start());
    }

    @Test
    void suggestsSystemForTypo() {
        assertMentions(run("Sytem.out.println(1);"), "`System`");
    }

    @Test
    void suggestsPrintlnForTypo() {
        assertMentions(run("System.out.printn(1);"), "`println`");
    }

    @Test
    void explainsCaseSensitivity() {
        assertMentions(run("string s = \"a\";"), "`String`", "大小寫");
    }

    @Test
    void suggestsDeclaredVariable() {
        assertMentions(run("int count = 1;\nSystem.out.println(cout);"), "`count`");
    }

    @Test
    void fullWidthSemicolon() {
        assertMentions(run("int z = 1；"), "全形", "`;`");
    }

    @Test
    void chineseQuotes() {
        assertMentions(run("System.out.println(“abc”);"), "引號");
    }

    @Test
    void unclosedString() {
        assertMentions(run("System.out.println(\"abc);"), "雙引號");
    }

    @Test
    void stringArithmetic() {
        assertMentions(run("int n = \"a\" * 2;"), "`+`");
    }

    @Test
    void runtimeErrorKeepsEarlierOutput() {
        String src = "System.out.println(\"before\");\nint q = 10 / 0;";
        RunResult r = run(src);
        assertEquals(Outcome.RUNTIME_ERROR, r.outcome());
        assertEquals("before", r.output().strip());
        assertMentions(r, "除以零");
        assertEquals(src.indexOf("int q"), error(r).start());
    }

    @Test
    void runtimeErrorInsideMethodPointsAtLine() {
        String src = "int pick(int[] a) {\n    return a[5];\n}\nSystem.out.println(pick(new int[2]));";
        RunResult r = run(src);
        assertEquals(Outcome.RUNTIME_ERROR, r.outcome());
        assertMentions(r, "索引 5", "0 到 1");
        assertEquals(src.indexOf("return"), error(r).start());
    }

    @Test
    void runtimeErrorAfterCommentPointsAtCode() {
        String src = "// 註解\n\n// 再一行\nint q = 10 / 0;";
        RunResult r = run(src);
        assertEquals(Outcome.RUNTIME_ERROR, r.outcome());
        assertEquals(src.indexOf("int q"), error(r).start());
    }

    @Test
    void timeoutSkipsLeadingComment() {
        String src = "// 註解\nwhile (true) { }";
        RunResult r = run(src);
        assertEquals(Outcome.TIMEOUT, r.outcome());
        assertEquals(src.indexOf("while"), error(r).start());
    }

    @Test
    void rejectsDuplicateDeclaration() {
        assertMentions(run("int a = 1;\nint a = 2;"), "已經宣告過");
    }

    @Test
    void rejectsRedeclaringCaseVariable() {
        RunResult r = sandbox.run(List.of("String witness = \"x\";"), "String witness = \"y\";", List.of());
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        assertMentions(r, "案件提供");
    }

    @Test
    void reportsUnresolvedNameInMethod() {
        RunResult r = run("void m() { foo(); }");
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        assertMentions(r, "`foo`");
    }

    @Test
    void warnsStringComparedWithEquals() {
        RunResult r = run("String a = \"hi\";\nif (a == \"hi\") {\n    System.out.println(1);\n}");
        assertEquals(Outcome.OK, r.outcome());
        assertTrue(r.problems().stream().anyMatch(p -> p.severity() == Severity.WARNING && p.message().contains("equals")));
    }

    @Test
    void warnsSemicolonAfterIf() {
        RunResult r = run("int a = 1;\nif (a > 5);\nSystem.out.println(a);");
        assertEquals(Outcome.OK, r.outcome());
        assertTrue(r.problems().stream().anyMatch(p -> p.severity() == Severity.WARNING && p.message().contains("多了分號")));
    }

    @Test
    void stateDoesNotLeakBetweenRuns() {
        assertEquals(Outcome.OK, run("int leak = 1;").outcome());
        assertEquals(Outcome.COMPILE_ERROR, run("System.out.println(leak);").outcome());
    }

    @Test
    void timesOutAndRecovers() {
        RunResult r = run("while (true) {}");
        assertEquals(Outcome.TIMEOUT, r.outcome());
        assertEquals(Outcome.OK, run("System.out.println(1);").outcome());
    }

    @Test
    void capsEndlessOutput() {
        RunResult r = run("while (true) System.out.println(\"x\");");
        assertEquals(Outcome.TIMEOUT, r.outcome());
        assertTrue(r.truncated());
    }

    @Test
    void recoversAfterSystemExit() {
        assertEquals(Outcome.EXITED, run("System.exit(0);").outcome());
        RunResult r = run("System.out.println(2);");
        assertEquals(Outcome.OK, r.outcome());
        assertEquals("2", r.output().strip());
    }

    @Test
    void probesReportTypeAndValue() {
        RunResult r = sandbox.run(List.of("int h = 20;"), "int total = h * 60;", List.of("total", "missing"));
        assertEquals("int", r.probes().get(0).type());
        assertEquals("1200", r.probes().get(0).value());
        assertTrue(r.probes().get(1).error() != null);
    }
}
