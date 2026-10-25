package campus.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoPairTest {

    /** {@code |} 是游標；照編輯器的做法打一個字，回傳打完的樣子。 */
    private static String type(String before, char c) {
        int caret = before.indexOf('|');
        String text = before.replace("|", "");
        AutoPair.Edit edit = AutoPair.typed(text, caret, caret, c);
        if (edit == null) {
            return text.substring(0, caret) + c + "|" + text.substring(caret);
        }
        String after = text.substring(0, edit.start()) + edit.insert() + text.substring(edit.end());
        return after.substring(0, edit.caret()) + "|" + after.substring(edit.caret());
    }

    @Test
    void openingBracketsAndQuotesBringTheirPartner() {
        assertEquals("System.out.println(|)", type("System.out.println|", '('));
        assertEquals("int[|]", type("int|", '['));
        assertEquals("if (ok) {|}", type("if (ok) |", '{'));
        assertEquals("System.out.println(\"|\")", type("System.out.println(|)", '"'));
        assertEquals("char c = '|';", type("char c = |;", '\''));
    }

    @Test
    void typingThePartnerAgainStepsOverIt() {
        assertEquals("System.out.println()|", type("System.out.println(|)", ')'));
        assertEquals("System.out.println(\"吃控肉飯\"|)", type("System.out.println(\"吃控肉飯|\")", '"'));
        assertEquals("char c = 'a'|;", type("char c = 'a|';", '\''));
        assertEquals("{}|", type("{|}", '}'));
    }

    @Test
    void noPartnerRightBeforeAName() {
        assertEquals("(|gacha + 1", type("|gacha + 1", '('));
        assertEquals("\"|abc\"", type("|abc\"", '"'));
    }

    @Test
    void insideStringsAndCommentsTypesJustOne() {
        assertEquals("System.out.println(\"吃\"|)", type("System.out.println(\"吃|)", '"'));
        assertEquals("\"笑臉 :(|\"", type("\"笑臉 :|\"", '('));
        assertEquals("// 括號 (|", type("// 括號 |", '('));
        assertEquals("\"他說 \\\"|\"", type("\"他說 \\|\"", '"'));
    }

    @Test
    void selectionGetsWrapped() {
        AutoPair.Edit edit = AutoPair.typed("x = a + b;", 4, 9, '(');
        assertEquals("(a + b)", edit.insert());
        assertEquals(5, edit.anchor());
        assertEquals(10, edit.caret());
        assertNull(AutoPair.typed("x = a + b;", 4, 9, 'z'));
    }

    @Test
    void backspaceRemovesAnEmptyPairTogether() {
        assertTrue(AutoPair.deletesPair("println()", 8));
        assertTrue(AutoPair.deletesPair("println(\"\")", 9));
        assertTrue(AutoPair.deletesPair("{}", 1));
        assertFalse(AutoPair.deletesPair("println(x)", 8));
        assertFalse(AutoPair.deletesPair("\"a\"\"b\"", 3));
        assertFalse(AutoPair.deletesPair("()", 2));
    }
}
