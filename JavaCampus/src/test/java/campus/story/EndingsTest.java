package campus.story;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 門檻照 {@code story/outline/endings.md}。 */
class EndingsTest {

    @Test
    void trueEndNeedsBothHighAndTwoKeyChoices() {
        assertEquals(Endings.TRUE, Endings.of(80, 80, 2));
        assertEquals(Endings.GOOD, Endings.of(80, 80, 1));
        assertEquals(Endings.GOOD, Endings.of(79, 100, 2));
        assertEquals(Endings.GOOD, Endings.of(100, 79, 2));
    }

    @Test
    void goodEndNeedsBothAtSixty() {
        assertEquals(Endings.GOOD, Endings.of(60, 60, 0));
        assertEquals(Endings.NORMAL, Endings.of(59, 100, 0));
    }

    @Test
    void normalEndNeedsEitherAtForty() {
        assertEquals(Endings.NORMAL, Endings.of(40, 0, 0));
        assertEquals(Endings.NORMAL, Endings.of(0, 40, 0));
        assertEquals(Endings.BAD, Endings.of(39, 39, 2));
    }

    @Test
    void scriptsCanTestTheEnding() {
        StoryState s = new StoryState();
        assertEquals(Endings.BAD, Endings.of(s.ap(), s.tp(), s.keyCount()));
        assertEquals(true, Condition.parse("結局=Bad").test(s));
        assertEquals(false, Condition.parse("結局=True").test(s));
        assertEquals(true, Condition.parse("AP<=10").test(s));
        assertEquals(false, Condition.parse("旗標:沒有").test(s));
    }
}
