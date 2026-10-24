package campus.ui;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 文件和筆記只需要 {@code #} 標題、段落、{@code `程式碼`}、{@code **粗體**} 與 ``` 程式區塊，不值得引入完整的 Markdown 函式庫。 */
public final class MiniMarkdown {

    private static final Pattern INLINE = Pattern.compile("\\*\\*(.+?)\\*\\*|`([^`]+)`");

    private MiniMarkdown() {}

    public static VBox render(String markdown) {
        VBox box = new VBox(8);
        box.getStyleClass().add("md");
        if (markdown == null) {
            return box;
        }
        String[] parts = markdown.replace("\r\n", "\n").split("```", -1);
        for (int i = 0; i < parts.length; i++) {
            if (i % 2 == 1) {
                Label pre = new Label(parts[i].strip());
                pre.getStyleClass().add("md-pre");
                pre.setMaxWidth(Double.MAX_VALUE);
                box.getChildren().add(pre);
                continue;
            }
            for (String para : parts[i].split("\n\\s*\n")) {
                String p = para.strip();
                if (p.startsWith("#")) {
                    TextFlow heading = inline(p.replaceFirst("^#+\\s*", ""));
                    heading.getStyleClass().add("md-h");
                    box.getChildren().add(heading);
                } else if (!p.isEmpty()) {
                    box.getChildren().add(inline(p));
                }
            }
        }
        return box;
    }

    public static TextFlow inline(String text) {
        TextFlow flow = new TextFlow();
        flow.getStyleClass().add("md-p");
        Matcher m = INLINE.matcher(text);
        int at = 0;
        while (m.find()) {
            if (m.start() > at) {
                flow.getChildren().add(text(text.substring(at, m.start()), "md-text"));
            }
            if (m.group(1) != null) {
                flow.getChildren().add(text(m.group(1), "md-bold"));
            } else {
                flow.getChildren().add(text(m.group(2), "md-code"));
            }
            at = m.end();
        }
        if (at < text.length()) {
            flow.getChildren().add(text(text.substring(at), "md-text"));
        }
        return flow;
    }

    private static Node text(String s, String styleClass) {
        Text t = new Text(s);
        t.getStyleClass().add(styleClass);
        return t;
    }
}
