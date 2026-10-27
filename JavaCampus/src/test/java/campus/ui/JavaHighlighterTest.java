package campus.ui;

import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaHighlighterTest {

    @Test
    void escapesInsideStringsGetTheirOwnColor() {
        String code = "System.out.printf(\"第%d遍\\n\", i);";
        StyleSpans<Collection<String>> spans = JavaHighlighter.spans(code, List.of());
        int escape = code.indexOf("\\n");
        assertEquals(List.of("esc"), List.copyOf(styleAt(spans, escape)));
        assertEquals(List.of("esc"), List.copyOf(styleAt(spans, escape + 1)));
        assertEquals(List.of("str"), List.copyOf(styleAt(spans, code.indexOf('第'))));
        assertEquals(List.of("str"), List.copyOf(styleAt(spans, escape + 2)));
    }

    private static Collection<String> styleAt(StyleSpans<Collection<String>> spans, int position) {
        int at = 0;
        for (StyleSpan<Collection<String>> s : spans) {
            if (position < at + s.getLength()) {
                return s.getStyle();
            }
            at += s.getLength();
        }
        throw new AssertionError("超出範圍：" + position);
    }
}
