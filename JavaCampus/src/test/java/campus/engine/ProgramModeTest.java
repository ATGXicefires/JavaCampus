package campus.engine;

import campus.engine.RunResult.Outcome;
import campus.engine.RunResult.Problem;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgramModeTest {

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
        return sandbox.runProgram(source, true, List.of());
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

    private static String main(String body) {
        return "public class Main {\n    public static void main(String[] args) {\n" + body + "\n    }\n}\n";
    }

    @Test
    void runsMainWithImports() {
        RunResult r = run("import java.util.*;\n\n" + main("""
                        List<String> names = new ArrayList<>();
                        names.add("林雅婷");
                        System.out.println(names.size() + " " + names.get(0));"""));
        assertEquals(Outcome.OK, r.outcome(), r.toString());
        assertEquals("1 林雅婷", r.output().strip());
    }

    @Test
    void classesCanReferToLaterClasses() {
        RunResult r = run("""
                public class Main {
                    public static void main(String[] args) {
                        System.out.println(new Clock(21, 47).total());
                    }
                }

                class Clock {
                    private final int hour;
                    private final int minute;

                    Clock(int hour, int minute) {
                        this.hour = hour;
                        this.minute = minute;
                    }

                    int total() {
                        return hour * 60 + minute;
                    }
                }
                """);
        assertEquals(Outcome.OK, r.outcome(), r.toString());
        assertEquals("1307", r.output().strip());
    }

    @Test
    void acceptsPackageDeclaration() {
        RunResult r = run("// 案件程式\npackage detective.cases;\n\n" + main("System.out.println(\"報到\");"));
        assertEquals(Outcome.OK, r.outcome(), r.toString());
        assertEquals("報到", r.output().strip());
    }

    @Test
    void uninitializedLocalIsCompileError() {
        RunResult r = run(main("int w;\nSystem.out.println(w);"));
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        assertMentions(r, "`w`", "還沒有給值");
    }

    @Test
    void rejectsStatementOutsideClass() {
        String src = "System.out.println(1);\n" + main("");
        RunResult r = run(src);
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        assertMentions(r, "class");
        assertEquals(0, error(r).start());
    }

    @Test
    void rejectsMethodOutsideClass() {
        RunResult r = run("static int twice(int x) {\n    return x * 2;\n}\n" + main(""));
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        assertMentions(r, "class 的大括號裡面");
    }

    @Test
    void rejectsImportAfterClass() {
        String src = main("") + "import java.util.List;\n";
        RunResult r = run(src);
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        assertMentions(r, "`import`", "最上面");
        assertEquals(src.indexOf("import"), error(r).start());
    }

    @Test
    void publicClassMustBeMain() {
        RunResult r = run("public class Detective {\n    public static void main(String[] args) {\n    }\n}\n");
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        assertMentions(r, "Detective.java", "Main.java");
    }

    @Test
    void rejectsDuplicateClass() {
        RunResult r = run(main("") + "class Clue {}\nclass Clue {}\n");
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        assertMentions(r, "`Clue`", "兩次");
    }

    @Test
    void missingImportNamesThePackage() {
        RunResult r = run(main("List<String> names = new ArrayList<>();"));
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        assertMentions(r, "import java.util.");
    }

    @Test
    void missingImportInSnippetModeToo() {
        RunResult r = sandbox.run(List.of(), "int[] a = {3, 1};\nArrays.sort(a);", List.of());
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        assertMentions(r, "import java.util.Arrays;");
    }

    @Test
    void reportsMissingMainMethod() {
        RunResult r = run("public class Main {\n    static void main() {\n    }\n}\n");
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        assertMentions(r, "public static void main(String[] args)");
        assertEquals(0, error(r).start());
    }

    @Test
    void reportsMissingMainClass() {
        RunResult r = run("class Helper {\n}\n");
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        assertMentions(r, "`Main`");
    }

    @Test
    void explainsNonStaticCallFromMain() {
        RunResult r = run("public class Main {\n    void report() {\n    }\n\n"
                + "    public static void main(String[] args) {\n        report();\n    }\n}\n");
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        assertMentions(r, "`static`", "`report()`");
    }

    @Test
    void runtimeErrorPointsAtLineInsideMethod() {
        String src = """
                public class Main {
                    static int pick(int[] a) {
                        return a[5];
                    }

                    public static void main(String[] args) {
                        System.out.println("開始");
                        System.out.println(pick(new int[2]));
                    }
                }
                """;
        RunResult r = run(src);
        assertEquals(Outcome.RUNTIME_ERROR, r.outcome());
        assertEquals("開始", r.output().strip());
        assertMentions(r, "索引 5");
        assertEquals(src.indexOf("return a[5]"), error(r).start());
    }

    @Test
    void infiniteLoopInMainTimesOut() {
        RunResult r = run(main("while (true) { }"));
        assertEquals(Outcome.TIMEOUT, r.outcome());
        assertEquals(Outcome.OK, run(main("System.out.println(1);")).outcome());
    }

    @Test
    void probesCallStaticMethodsWithoutLaunching() {
        String src = """
                public class Main {
                    static int leaveTime(int hour, int minute, int drive) {
                        return hour * 60 + minute - drive;
                    }
                }
                """;
        RunResult r = sandbox.runProgram(src, false, List.of("Main.leaveTime(22, 20, 25)"));
        assertEquals(Outcome.OK, r.outcome(), r.toString());
        assertEquals("int", r.probes().getFirst().type());
        assertEquals("1315", r.probes().getFirst().value());
    }

    @Test
    void probeTypesStayExactAcrossRuns() {
        String src = "public class Main {\n    static String verdict() {\n        return \"說謊\";\n    }\n}\n";
        for (int i = 0; i < 2; i++) {
            RunResult r = sandbox.runProgram(src, false, List.of("Main.verdict()"));
            assertEquals("String", r.probes().getFirst().type(), r.toString());
        }
        RunResult snippet = sandbox.run(List.of(), "String gap = \"14\";", List.of("gap"));
        assertEquals("String", snippet.probes().getFirst().type());
    }
}
