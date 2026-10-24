package campus.lesson;

import campus.engine.Sandbox;
import campus.engine.Verifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 完整程式的步驟可以有好幾個檔案，用暫存資料夾做一份小題目來讀。 */
class MultiFileLessonTest {

    @TempDir
    Path root;

    private static final String LESSON = """
            {
              "id": "fx-roster",
              "title": "名冊",
              "route": "學妹",
              "partner": "二弘",
              "steps": [
                {
                  "id": "roster",
                  "title": "兩個檔案",
                  "goal": "讓 `Main` 用 `Student` 印出名字。",
                  "form": "program",
                  "check": { "type": "output" },
                  "hints": { "lead": "⋯⋯", "plain": "⋯⋯" }
                }
              ]
            }
            """;

    private static final String MAIN = """
            public class Main {
                public static void main(String[] args) {
                    System.out.println(new Student("梁亭瑄").name());
                }
            }
            """;

    private void write(String name, String text) throws IOException {
        Files.writeString(root.resolve("fx-roster").resolve(name), text, UTF_8);
    }

    private void writeLesson() throws IOException {
        Files.createDirectories(root.resolve("fx-roster"));
        Files.writeString(root.resolve("handbook.json"), "{ \"entries\": [] }", UTF_8);
        Files.writeString(root.resolve("reactions.json"), "{ \"二弘\": {} }", UTF_8);
        write("lesson.json", LESSON);
        write("roster.starter.Student.java", "public class Student {\n}\n");
        write("roster.starter.Main.java", MAIN);
        write("roster.skeleton.Main.java", MAIN);
        write("roster.skeleton.Student.java", "public class Student {\n    ____\n}\n");
        write("roster.solution.Main.java", MAIN);
        write("roster.solution.Student.java", """
                public class Student {
                    private final String name;

                    Student(String name) {
                        this.name = name;
                    }

                    String name() {
                        return name;
                    }
                }
                """);
    }

    @Test
    void loadsEveryFileOfTheStepWithMainFirst() throws IOException {
        writeLesson();
        Step step = LessonLibrary.load(root).lesson("fx-roster").steps().getFirst();
        assertEquals(List.of("Main.java", "Student.java"), step.starter().stream().map(SourceFile::name).toList());
        assertEquals(List.of("Main.java", "Student.java"), step.solution().stream().map(SourceFile::name).toList());
        assertEquals("", step.instructions());

        try (Sandbox sandbox = new Sandbox(Duration.ofSeconds(3))) {
            Verifier verifier = new Verifier(sandbox);
            assertTrue(verifier.verify("fx", step.form(), List.of(), step.check(), step.solution(), step.solution()).passed());
            assertFalse(verifier.verify("fx", step.form(), List.of(), step.check(), step.solution(), step.starter()).passed());
        }
    }

    @Test
    void rejectsMixingSingleAndMultiFileForms() throws IOException {
        writeLesson();
        write("roster.starter.jsh", MAIN);
        assertThrows(IllegalStateException.class, () -> LessonLibrary.load(root));
    }
}
