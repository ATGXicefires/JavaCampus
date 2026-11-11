package campus.ui;

import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Scale;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件和筆記只需要 {@code #} 標題、段落、{@code `程式碼`}、{@code **粗體**}、``` 程式區塊和單獨一段的
 * {@code ![說明](圖.svg)}，不值得引入完整的 Markdown 函式庫。
 */
public final class MiniMarkdown {

    private static final Pattern INLINE = Pattern.compile("\\*\\*(.+?)\\*\\*|`([^`]+)`");
    private static final Pattern IMAGE = Pattern.compile("!\\[([^\\]]*)]\\(([^)]+)\\)");

    /** 畫成看得見的空格的字；複製時要換回真的空格（見 TextSelection）。 */
    static final char SPACE_MARK = '␣';
    // 呼叫方法、取陣列的格子或指定：是程式碼，不是要印的字
    private static final Pattern CODE_LIKE = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*[(\\[]| = ");

    private MiniMarkdown() {}

    /** 沒有圖片資料夾，圖片只顯示說明文字。 */
    public static VBox render(String markdown) {
        return render(markdown, null);
    }

    /** @param imageDir 圖片從這個資料夾載入；null 就只顯示說明文字 */
    public static VBox render(String markdown, Path imageDir) {
        VBox box = new VBox(8);
        box.getStyleClass().add("md");
        if (markdown == null) {
            return box;
        }
        String[] parts = markdown.replace("\r\n", "\n").split("```", -1);
        for (int i = 0; i < parts.length; i++) {
            if (i % 2 == 1) {
                // 用 TextFlow 而不是 Label，程式說明裡的範例程式才選取得到（見 TextSelection）
                TextFlow pre = new TextFlow();
                pre.getChildren().addAll(texts(visibleSpaces(parts[i].strip(), false), "md-pre-text"));
                pre.getStyleClass().add("md-pre");
                box.getChildren().add(pre);
                continue;
            }
            for (String para : parts[i].split("\n\\s*\n")) {
                String p = para.strip();
                Matcher image = IMAGE.matcher(p);
                if (image.matches()) {
                    box.getChildren().add(image(image.group(1), image.group(2).strip(), imageDir));
                } else if (p.startsWith("#")) {
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

    /** 找不到圖或畫不出來就退回說明文字，整份文件還是打得開。 */
    private static Node image(String alt, String file, Path dir) {
        if (dir != null) {
            try {
                return new Picture(SvgLoader.load(dir.resolve(file)));
            } catch (IOException | IllegalArgumentException e) {
                System.err.println("圖片載入失敗：" + file + "（" + e.getMessage() + "）");
            }
        }
        TextFlow fallback = inline(alt);
        fallback.getStyleClass().add("md-img-missing");
        return fallback;
    }

    /** 照 viewBox 的比例縮放到可用的寬度。 */
    static final class Picture extends Region {

        private final SvgLoader.Drawing drawing;
        private final Group holder;
        private final Scale scale = new Scale(1, 1);

        Picture(SvgLoader.Drawing drawing) {
            this.drawing = drawing;
            holder = new Group(drawing.root());
            holder.getTransforms().add(scale);
            getChildren().add(holder);
            getStyleClass().add("md-img");
        }

        @Override
        public Orientation getContentBias() {
            return Orientation.HORIZONTAL;
        }

        @Override
        protected double computeMinWidth(double height) {
            return snappedLeftInset() + snappedRightInset();
        }

        @Override
        protected double computePrefWidth(double height) {
            return drawing.width() + snappedLeftInset() + snappedRightInset();
        }

        @Override
        protected double computeMinHeight(double width) {
            return computePrefHeight(width);
        }

        @Override
        protected double computePrefHeight(double width) {
            double inner = width < 0 ? drawing.width() : width - snappedLeftInset() - snappedRightInset();
            return drawing.height() * inner / drawing.width() + snappedTopInset() + snappedBottomInset();
        }

        @Override
        protected void layoutChildren() {
            double k = (getWidth() - snappedLeftInset() - snappedRightInset()) / drawing.width();
            scale.setX(k);
            scale.setY(k);
            // 不用 relocate：它會對齊圖形實際的邊界，viewBox 的原點就跑掉了
            holder.setLayoutX(snappedLeftInset());
            holder.setLayoutY(snappedTopInset());
        }
    }

    public static TextFlow inline(String text) {
        TextFlow flow = new TextFlow();
        flow.getStyleClass().add("md-p");
        Matcher m = INLINE.matcher(text);
        int at = 0;
        while (m.find()) {
            if (m.start() > at) {
                flow.getChildren().addAll(texts(text.substring(at, m.start()), "md-text"));
            }
            if (m.group(1) != null) {
                flow.getChildren().addAll(texts(m.group(1), "md-bold"));
            } else {
                flow.getChildren().addAll(texts(visibleSpaces(m.group(2), true), "md-code"));
            }
            at = m.end();
        }
        if (at < text.length()) {
            flow.getChildren().addAll(texts(text.substring(at), "md-text"));
        }
        return flow;
    }

    /**
     * 要照打的輸出裡，空格和中文的字距分不出來，所以畫成 {@value #SPACE_MARK}：程式碼只換字串引號裡的空格；
     * {@code wholeOutput} 時，沒有引號、有中文、又不像 {@code substring(開始, 結束)} 這種帶中文說明的程式碼，
     * 整段就是要印出來的字，每個空格都換。
     */
    static String visibleSpaces(String code, boolean wholeOutput) {
        if (code.indexOf('"') < 0) {
            return wholeOutput && code.codePoints().anyMatch(MiniMarkdown::isCjk) && !CODE_LIKE.matcher(code).find()
                    ? code.replace(' ', SPACE_MARK) : code;
        }
        StringBuilder b = new StringBuilder(code.length());
        boolean quoted = false;
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if (c == '"' && (i == 0 || code.charAt(i - 1) != '\\') || c == '\n') {
                quoted = c == '"' && !quoted;
            }
            b.append(quoted && c == ' ' ? SPACE_MARK : c);
        }
        return b.toString();
    }

    private static boolean isCjk(int cp) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(cp);
        return Character.UnicodeScript.of(cp) == Character.UnicodeScript.HAN
                || block == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    /** 一段字切成好幾個 Text：{@value #SPACE_MARK} 單獨一個，淡一點，跟真的字分得開。 */
    static List<Text> texts(String s, String styleClass) {
        List<Text> out = new ArrayList<>();
        int at = 0;
        for (int i = s.indexOf(SPACE_MARK); i >= 0; i = s.indexOf(SPACE_MARK, at)) {
            if (i > at) {
                out.add(text(s.substring(at, i), styleClass));
            }
            Text mark = text(String.valueOf(SPACE_MARK), styleClass);
            mark.getStyleClass().add("md-space");
            out.add(mark);
            at = i + 1;
        }
        if (at < s.length() || out.isEmpty()) {
            out.add(text(s.substring(at), styleClass));
        }
        return out;
    }

    private static Text text(String s, String styleClass) {
        Text t = new Text(s);
        t.getStyleClass().add(styleClass);
        return t;
    }
}
