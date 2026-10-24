package campus.ui;

import campus.art.FigureView;
import campus.art.Figures;
import campus.fx.Motion;
import campus.story.Cast;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * 劇情的對話框，畫成一頁橫線筆記本。橫線間距固定，各種字體都把行距補到剛好一格，
 * 所以換人說話、換成內心 OS 時橫線不會跳，每一行字都坐在線上。
 */
final class NotebookPage extends Region {

    /** 整句出現後右下角的記號：平常是 ▼，自動播放時是 ▶，快進時是 ▶▶。 */
    enum Mark { NEXT, AUTO, SKIP }

    private static final double RULE = 40;
    private static final int MIN_ROWS = 3;
    /** 紅色邊線到文字的距離。 */
    private static final double MARGIN_GAP = 24;
    private static final double HOLE_RADIUS = 7;
    private static final double HOLE_TOP = 36;
    private static final double HOLE_STEP = 80;
    private static final int HOLES = 6;
    /** 大頭照裡剪影那一格的邊長。 */
    private static final double FACE = 104;
    /** 大頭照的上緣，讓開跨在紙上緣的名牌膠帶。 */
    private static final double FACE_TOP = 30;

    final Label plate = new Label();
    final Label action = new Label();
    final Label text = new Label();
    private final Path rules = new Path();
    private final Region margin = new Region();
    private final List<Circle> holes = new ArrayList<>();
    private final SVGPath mark = new SVGPath();
    private final Pane lens = new Pane();
    private final StackPane photo = new StackPane(lens);
    private Mark shown;
    private Animation blink;

    NotebookPage() {
        getStyleClass().add("dialog-box");
        lens.getStyleClass().add("dialog-face-lens");
        lens.setPrefSize(FACE, FACE);
        lens.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        lens.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        lens.setClip(new Rectangle(FACE, FACE));
        photo.getStyleClass().add("dialog-face");
        photo.setManaged(false);
        photo.setVisible(false);
        rules.getStyleClass().add("dialog-rules");
        margin.getStyleClass().add("dialog-margin");
        mark.getStyleClass().add("dialog-next");
        plate.getStyleClass().add("name-plate");
        action.getStyleClass().add("dialog-action");
        text.getStyleClass().add("dialog-text");
        text.setWrapText(true);
        // 標籤比內容高時也要貼齊上緣，基線位置才不會跟著高度變
        action.setAlignment(Pos.TOP_LEFT);
        text.setAlignment(Pos.TOP_LEFT);
        // 只有 managed 改變會通知這頁重新排版，單純切換 visible 不會
        action.managedProperty().bind(action.visibleProperty());
        // 橫線、孔和記號在排版時才重畫，設成 unmanaged，重畫時才不會又要求整頁重排
        rules.setManaged(false);
        margin.setManaged(false);
        mark.setManaged(false);
        for (int i = 0; i < HOLES; i++) {
            Circle hole = new Circle(HOLE_RADIUS);
            hole.getStyleClass().add("dialog-hole");
            hole.setManaged(false);
            holes.add(hole);
        }
        getChildren().addAll(rules, margin);
        getChildren().addAll(holes);
        getChildren().addAll(action, text, mark, photo, plate);
        mark.setVisible(false);
        setMinHeight(USE_PREF_SIZE);
        setMaxHeight(USE_PREF_SIZE);
        text.fontProperty().addListener((obs, was, font) -> fitRules(font));
        fitRules(text.getFont());
        sceneProperty().addListener((obs, was, now) -> {
            if (now == null) {
                showMark(null);
            }
        });
    }

    /** 名牌是一段角色代表色的紙膠帶；{@code who} 是 null 表示這行沒有人開口。 */
    void speaker(String who) {
        plate.setVisible(who != null);
        if (who == null) {
            return;
        }
        String color = Cast.colorOf(who);
        // 相對亮度約 0.22 時白字和深色字的對比度相同，比它亮的底色改用深色字
        String ink = luminance(Color.web(color)) > 0.22 ? "#2b2420" : "white";
        plate.setText(who);
        plate.setStyle("-fx-background-color: " + color + "; -fx-text-fill: " + ink + ";");
    }

    /**
     * 說話的人不在台上時，在左邊留白處貼一張他的大頭照；{@code null} 收起。
     * 照片只蓋住孔和紅線，不佔文字的位置，有沒有照片換行和高度都一樣。
     */
    void face(FigureView figure) {
        boolean was = photo.isVisible();
        photo.setVisible(figure != null);
        if (figure == null) {
            return;
        }
        boolean changed = lens.getChildren().isEmpty() || lens.getChildren().getFirst() != figure;
        if (changed) {
            double[] crop = Figures.bust(figure.person());
            double k = FACE / crop[2];
            figure.setManaged(false);
            lens.getChildren().setAll(figure);
            figure.resizeRelocate(-crop[0] * k, -crop[1] * k, Figures.UNIT_W * k, Figures.UNIT_H * k);
        }
        if (!was || changed) {
            Motion.pop(photo);
        }
    }

    /** {@code null} 收起記號；動效關閉時記號不閃。 */
    void showMark(Mark m) {
        if (m == shown) {
            return;
        }
        shown = m;
        if (blink != null) {
            blink.stop();
            blink = null;
        }
        mark.setVisible(m != null);
        mark.setOpacity(1);
        mark.setTranslateY(0);
        if (m == null) {
            return;
        }
        mark.setContent(switch (m) {
            case NEXT -> "M0 0 H16 L8 10 Z";
            case AUTO -> "M0 0 L11 6 L0 12 Z";
            case SKIP -> "M0 0 L9 6 L0 12 Z M9 0 L18 6 L9 12 Z";
        });
        requestLayout();
        if (Motion.isOff()) {
            return;
        }
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(560),
                new KeyValue(mark.opacityProperty(), 0.25, Interpolator.EASE_BOTH),
                new KeyValue(mark.translateYProperty(), m == Mark.NEXT ? 3 : 0, Interpolator.EASE_BOTH)));
        tl.setAutoReverse(true);
        tl.setCycleCount(Animation.INDEFINITE);
        tl.play();
        blink = tl;
    }

    /** 行距補到剛好一格橫線；說話、OS、舞台指示的字體不同，每換一次就重算。 */
    private void fitRules(Font font) {
        Text probe = new Text("中");
        probe.setFont(font);
        // Label 內部的文字用這種邊界量行高，比預設的 LOGICAL 高一些，量法要跟它一樣
        probe.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);
        text.setLineSpacing(Math.max(0, RULE - probe.getLayoutBounds().getHeight()));
    }

    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    @Override
    protected double computePrefWidth(double height) {
        Insets in = getInsets();
        return in.getLeft() + text.prefWidth(-1) + in.getRight();
    }

    @Override
    protected double computePrefHeight(double width) {
        Insets in = getInsets();
        double inner = width < 0 ? -1 : Math.max(0, width - in.getLeft() - in.getRight());
        int rows = (action.isVisible() ? 1 : 0) + rowsOf(text, inner);
        return in.getTop() + Math.max(MIN_ROWS, rows) * RULE + in.getBottom();
    }

    /** 行距已經補成一格，高度除以格距就是行數；先減 1 是不管最後一行有沒有算進行距都對。 */
    private static int rowsOf(Label l, double width) {
        return Math.max(1, (int) Math.ceil((l.prefHeight(width) - 1) / RULE));
    }

    @Override
    protected void layoutChildren() {
        Insets in = getInsets();
        double w = getWidth();
        double h = getHeight();
        double inner = w - in.getLeft() - in.getRight();
        int row = 0;
        if (action.isVisible()) {
            writeOn(action, row++, inner);
        }
        writeOn(text, row, inner);

        rules.getElements().clear();
        for (double y = in.getTop() + RULE; y < h - 4; y += RULE) {
            rules.getElements().addAll(new MoveTo(0, y), new LineTo(w, y));
        }
        double marginX = in.getLeft() - MARGIN_GAP;
        margin.resizeRelocate(marginX, 0, 2, h);
        for (int i = 0; i < holes.size(); i++) {
            Circle hole = holes.get(i);
            double y = HOLE_TOP + i * HOLE_STEP;
            hole.setCenterX(marginX / 2);
            hole.setCenterY(y);
            hole.setVisible(y < h - HOLE_TOP);
        }
        Bounds m = mark.getLayoutBounds();
        mark.relocate(w - (in.getRight() + m.getWidth()) / 2, h - in.getBottom() - m.getHeight() - 5);
        // 照片右緣停在文字前面，左邊伸出紙外一點
        photo.autosize();
        photo.relocate(in.getLeft() - 10 - photo.getWidth(), FACE_TOP);
        // 名牌壓在紙的上緣，一半在紙外；它不跟著文字排，大小要自己量
        plate.autosize();
        plate.relocate(in.getLeft() - 14, -plate.getHeight() / 2);
    }

    /** 擺好標籤，讓它的第一行字坐在第 {@code row} 條橫線上。 */
    private void writeOn(Label l, int row, double width) {
        Insets in = getInsets();
        // 中文字的字面比基線再低一點，線要再往下，字才像寫在線上而不是被線劃過
        double baseline = in.getTop() + (row + 1) * RULE - l.getFont().getSize() * 0.2;
        l.resizeRelocate(in.getLeft(), baseline - l.getBaselineOffset(), width, Math.ceil(l.prefHeight(width)) + 1);
    }

    /** WCAG 的相對亮度。 */
    private static double luminance(Color c) {
        return 0.2126 * linear(c.getRed()) + 0.7152 * linear(c.getGreen()) + 0.0722 * linear(c.getBlue());
    }

    private static double linear(double v) {
        return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }
}
