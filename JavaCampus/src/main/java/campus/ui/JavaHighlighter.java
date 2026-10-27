package campus.ui;

import campus.engine.JavaTokens;
import campus.engine.JavaTokens.Token;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class JavaHighlighter {

    private static final Pattern ESCAPE = Pattern.compile("\\\\(?:u+[0-9a-fA-F]{4}|[0-7]{1,3}|.)");

    /** 要加底線的範圍，例如編譯錯誤或警告。 */
    record Mark(int start, int end, String styleClass) {}

    private JavaHighlighter() {}

    static StyleSpans<Collection<String>> spans(String text, List<Mark> marks) {
        if (text.isEmpty()) {
            return StyleSpans.singleton(List.of(), 0);
        }
        StyleSpans<Collection<String>> base = syntax(text);
        if (marks.isEmpty()) {
            return base;
        }
        StyleSpansBuilder<Collection<String>> b = new StyleSpansBuilder<>();
        int at = 0;
        for (Mark m : marks.stream().sorted((x, y) -> Integer.compare(x.start(), y.start())).toList()) {
            int start = Math.max(at, Math.min(m.start(), text.length()));
            int end = Math.max(start, Math.min(m.end(), text.length()));
            put(b, List.of(), start - at);
            put(b, List.of(m.styleClass()), end - start);
            at = end;
        }
        b.add(List.of(), text.length() - at);
        return base.overlay(b.create(), (x, y) -> {
            List<String> merged = new ArrayList<>(x);
            merged.addAll(y);
            return merged;
        });
    }

    /** 這個字詞上什麼顏色；不上色是 null。 */
    static String styleOf(Token t) {
        return switch (t.kind()) {
            case KEYWORD -> "kw";
            case STRING, CHAR -> "str";
            case NUMBER -> "num";
            case COMMENT -> "cmt";
            case IDENT -> Character.isUpperCase(t.text().charAt(0)) ? "type" : null;
            case OTHER -> "bad";
            default -> null;
        };
    }

    private static StyleSpans<Collection<String>> syntax(String text) {
        StyleSpansBuilder<Collection<String>> b = new StyleSpansBuilder<>();
        int at = 0;
        for (Token t : JavaTokens.tokenize(text)) {
            String cls = styleOf(t);
            if (cls == null) {
                continue;
            }
            put(b, List.of(), t.start() - at);
            if (cls.equals("str")) {
                // 字串裡的 \n 這類跳脫字元印出來不是那兩個字，換個顏色才看得出來
                Matcher m = ESCAPE.matcher(t.text());
                int from = 0;
                while (m.find()) {
                    put(b, List.of(cls), m.start() - from);
                    put(b, List.of("esc"), m.end() - m.start());
                    from = m.end();
                }
                put(b, List.of(cls), t.text().length() - from);
            } else {
                put(b, List.of(cls), t.end() - t.start());
            }
            at = t.end();
        }
        b.add(List.of(), text.length() - at);
        return b.create();
    }

    private static void put(StyleSpansBuilder<Collection<String>> b, Collection<String> style, int length) {
        if (length > 0) {
            b.add(style, length);
        }
    }
}
