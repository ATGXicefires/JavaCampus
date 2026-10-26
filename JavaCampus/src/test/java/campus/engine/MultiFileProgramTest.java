package campus.engine;

import campus.engine.RunResult.Outcome;
import campus.engine.RunResult.Problem;
import campus.lesson.Check;
import campus.lesson.SourceFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static campus.lesson.Step.Form.PROGRAM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiFileProgramTest {

    private static Sandbox sandbox;

    @BeforeAll
    static void start() {
        sandbox = new Sandbox(Duration.ofSeconds(1));
    }

    @AfterAll
    static void stop() {
        sandbox.close();
    }

    private static final SourceFile MAIN = new SourceFile("Main.java", """
            public class Main {
                public static void main(String[] args) {
                    Student s = new Student("梁亭瑄", 92);
                    System.out.println(s.line());
                }
            }
            """);

    private static RunResult run(SourceFile... files) {
        return sandbox.runProgram(List.of(files), true, List.of());
    }

    private static Problem error(RunResult r) {
        return r.firstError().orElseThrow(() -> new AssertionError("沒有錯誤：" + r));
    }

    private static void assertMentions(Problem p, String... words) {
        for (String w : words) {
            assertTrue(p.message().contains(w), "訊息應該提到「" + w + "」：" + p.message());
        }
    }

    @Test
    void classFromAnotherFileCanBeUsed() {
        SourceFile student = new SourceFile("Student.java", """
                public class Student {
                    private final String name;
                    private final int score;

                    Student(String name, int score) {
                        this.name = name;
                        this.score = score;
                    }

                    String line() {
                        return name + " " + score;
                    }
                }
                """);
        RunResult r = run(MAIN, student);
        assertEquals(Outcome.OK, r.outcome(), r.toString());
        assertEquals("梁亭瑄 92", r.output().strip());
    }

    @Test
    void publicClassNameMustMatchItsOwnFile() {
        String text = "// 學生資料\npublic class Pupil {\n}\n";
        RunResult r = run(MAIN, new SourceFile("Student.java", text));
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        Problem p = error(r);
        assertEquals("Student.java", p.file());
        assertEquals(text.indexOf("public class"), p.start());
        assertMentions(p, "Pupil.java", "Student.java");
    }

    @Test
    void compileErrorInSecondFileHasItsFileAndOffsets() {
        String text = """
                class Student {
                    String line() {
                        int score = "九十二";
                        return "" + score;
                    }

                    Student(String name, int score) {
                    }
                }
                """;
        RunResult r = run(MAIN, new SourceFile("Student.java", text));
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        Problem p = error(r);
        assertEquals("Student.java", p.file());
        assertEquals(text.indexOf("\"九十二\""), p.start());
    }

    @Test
    void errorInClassThatNeedsALaterFileIsReportedThere() {
        SourceFile main = new SourceFile("Main.java", """
                public class Main {
                    public static void main(String[] args) {
                        new Badge().draw();
                    }
                }
                """);
        String text = "public class Badge implements Drawable {\n    void draw() {\n    }\n}\n";
        RunResult r = run(main, new SourceFile("Badge.java", text),
                new SourceFile("Drawable.java", "public interface Drawable {\n    void draw();\n}\n"));
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        Problem p = error(r);
        assertEquals("Badge.java", p.file());
        assertMentions(p, "`public`");
    }

    @Test
    void runtimeErrorInSecondFileMapsToThatFile() {
        String text = """
                class Student {
                    Student(String name, int score) {
                    }

                    String line() {
                        int[] scores = new int[2];
                        return "" + scores[5];
                    }
                }
                """;
        RunResult r = run(MAIN, new SourceFile("Student.java", text));
        assertEquals(Outcome.RUNTIME_ERROR, r.outcome());
        Problem p = error(r);
        assertEquals("Student.java", p.file());
        assertEquals(text.indexOf("return \"\" + scores[5]"), p.start());
        assertMentions(p, "索引 5");
    }

    @Test
    void duplicateClassAcrossFilesIsReportedInTheLaterFile() {
        SourceFile main = new SourceFile("Main.java", MAIN.text() + "\nclass Helper {\n}\n");
        String text = "class Helper {\n}\n";
        RunResult r = run(main, new SourceFile("Helper.java", text));
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        Problem p = error(r);
        assertEquals("Helper.java", p.file());
        assertEquals(0, p.start());
        assertMentions(p, "`Helper`", "兩次");
    }

    @Test
    void importsOnlyCoverTheirOwnFile() {
        SourceFile main = new SourceFile("Main.java", "import java.util.*;\n\n" + MAIN.text());
        String text = """
                class Student {
                    Student(String name, int score) {
                    }

                    String line() {
                        List<String> parts = new ArrayList<>();
                        return parts.toString();
                    }
                }
                """;
        RunResult r = run(main, new SourceFile("Student.java", text));
        assertEquals(Outcome.COMPILE_ERROR, r.outcome());
        Problem p = error(r);
        assertEquals("Student.java", p.file());
        assertEquals(text.indexOf("List"), p.start());
        assertMentions(p, "import java.util.List;");
    }

    @Test
    void singleSourceIsMainJava() {
        RunResult r = sandbox.runProgram("public class Helper {\n}\n", true, List.of());
        assertEquals("Main.java", error(r).file());
        assertNull(sandbox.run(List.of(), "int x = \"a\";", List.of()).firstError().orElseThrow().file());
    }

    @Test
    void verifierComparesWholeProjects() {
        Verifier verifier = new Verifier(sandbox);
        Check check = new Check(Check.Mode.VALUES, List.of("Grade.of(59)", "Grade.of(60)"), null);
        List<SourceFile> solution = List.of(
                new SourceFile("Main.java", "public class Main {\n    public static void main(String[] args) {\n    }\n}\n"),
                new SourceFile("Grade.java", "public class Grade {\n    static boolean of(int s) {\n        return s >= 60;\n    }\n}\n"));
        List<SourceFile> mine = List.of(solution.getFirst(),
                new SourceFile("Grade.java", "public class Grade {\n    static boolean of(int score) {\n        return score > 59;\n    }\n}\n"));
        List<SourceFile> wrong = List.of(solution.getFirst(),
                new SourceFile("Grade.java", "public class Grade {\n    static boolean of(int score) {\n        return score > 60;\n    }\n}\n"));
        assertTrue(verifier.verify("mf1", PROGRAM, List.of(), check, solution, mine).passed());
        assertFalse(verifier.verify("mf1", PROGRAM, List.of(), check, solution, wrong).passed());
    }
}
