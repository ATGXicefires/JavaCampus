package campus.ui;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeCompletionTest {

    @Test
    void qualifierIsTheDottedChainBeforeTheWord() {
        String text = "    System.out.pri";
        assertEquals("System.out", CodeCompletion.qualifier(text, text.length() - 3));
        assertNull(CodeCompletion.qualifier("int gacha", 4));
    }

    @Test
    void staysQuietInsideStringsAndComments() {
        for (String inside : List.of("System.out.println(\"扭蛋", "// 盒子 box", "char c = 'a")) {
            assertTrue(CodeCompletion.inLiteral(inside, inside.length()), inside);
        }
        for (String outside : List.of("String box = \"扭蛋\";\nbo", "/* 註解 */ in", "\"a\" + b")) {
            assertFalse(CodeCompletion.inLiteral(outside, outside.length()), outside);
        }
    }

    @Test
    void membersOfThePlayersOwnClassSkipLocalsAndConstructors() {
        String code = """
                class Student {
                    private String name;
                    int score = 0;

                    Student(String name) {
                        this.name = name;
                    }

                    String greet(String to) {
                        String local = "嗨";
                        return local + to;
                    }

                    int total() {
                        return score;
                    }
                }
                """;
        List<String> labels = CodeCompletion.classMembers(code, "Student").stream().map(CodeCompletion.Item::label).toList();
        assertEquals(List.of("name", "score", "greet", "total"), labels);
        List<String> inserts = CodeCompletion.classMembers(code, "Student").stream().map(CodeCompletion.Item::insert).toList();
        assertEquals(List.of("name", "score", "greet(§)", "total()"), inserts);
    }

    @Test
    void aliasMatchesLikeTheIdeShortcut() {
        CodeCompletion.Item println = new CodeCompletion.Item("System.out.println", "System.out.println(§);", "印出一行", "sout");
        assertTrue(println.matches("sout"));
        assertTrue(println.matches("sys"));
        assertFalse(println.matches("out"));
    }
}
