package campus.ui;

import campus.fx.Motion;
import campus.lesson.Handbook;
import javafx.animation.PauseTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.List;

/**
 * 程式題右側的筆記。由簡入繁：平常只有標題和一句重點；滑鼠停一下，旁邊浮出完整內容；點一下就在原地展開，再點收起。
 * 浮出來的內容畫在 {@code layer} 上，跟遊戲畫面一起縮放，不用另外開視窗。
 */
final class NoteList extends ScrollPane {

    private static final Duration PEEK_DELAY = Duration.millis(300);
    private static final double PEEK_WIDTH = 380;
    private static final double PEEK_GAP = 12;

    private final Pane layer;
    private final PauseTransition wait = new PauseTransition(PEEK_DELAY);
    private VBox peek;

    /** @param layer 疊在整個程式題上面、不接收滑鼠的圖層 */
    NoteList(List<Handbook.Entry> entries, Pane layer) {
        this.layer = layer;
        VBox box = new VBox(10);
        box.setPadding(new Insets(14));
        for (Handbook.Entry e : entries) {
            box.getChildren().add(new Card(e));
        }
        setContent(box);
        setFitToWidth(true);
        // 捲動時卡片跑走了，浮出來的內容就對不上
        vvalueProperty().addListener((obs, old, now) -> hidePeek());
    }

    private void showPeek(Card card) {
        hidePeek();
        Label title = new Label(card.entry.title());
        title.getStyleClass().add("note-title");
        peek = new VBox(8, title, MiniMarkdown.render(card.entry.body()));
        peek.getStyleClass().addAll("note-card", "note-peek");
        peek.setPrefWidth(PEEK_WIDTH);
        peek.setMaxWidth(PEEK_WIDTH);
        layer.getChildren().add(peek);
        peek.applyCss();
        double height = peek.prefHeight(PEEK_WIDTH);
        Bounds at = layer.sceneToLocal(card.localToScene(card.getBoundsInLocal()));
        double y = Math.max(PEEK_GAP, Math.min(at.getMinY(), layer.getHeight() - height - PEEK_GAP));
        peek.relocate(at.getMinX() - PEEK_WIDTH - PEEK_GAP, y);
        Motion.rise(peek, Duration.ZERO);
    }

    private void hidePeek() {
        wait.stop();
        if (peek != null) {
            layer.getChildren().remove(peek);
            peek = null;
        }
    }

    /** 一個條目：標題和重點，展開後接著完整內容。 */
    private final class Card extends VBox {
        final Handbook.Entry entry;
        private final Label chevron = new Label("▸");
        private VBox body;

        Card(Handbook.Entry entry) {
            this.entry = entry;
            Label title = new Label(entry.title());
            title.getStyleClass().add("note-title");
            title.setWrapText(true);
            Region gap = new Region();
            HBox.setHgrow(gap, Priority.ALWAYS);
            chevron.getStyleClass().add("note-chevron");
            HBox head = new HBox(8, title, gap, chevron);
            head.setAlignment(Pos.CENTER_LEFT);
            TextFlow summary = MiniMarkdown.inline(entry.summary());
            summary.getStyleClass().add("note-summary");
            setSpacing(6);
            getChildren().addAll(head, summary);
            getStyleClass().addAll("note-card", "pressable");
            setOnMouseEntered(e -> {
                if (body == null) {
                    wait.setOnFinished(f -> showPeek(this));
                    wait.playFromStart();
                }
            });
            setOnMouseExited(e -> hidePeek());
            setOnMouseClicked(e -> toggle());
        }

        private void toggle() {
            hidePeek();
            if (body == null) {
                body = MiniMarkdown.render(entry.body());
                body.getStyleClass().add("note-body");
                getChildren().add(body);
                chevron.setText("▾");
                getStyleClass().add("open");
                Motion.rise(body, Duration.ZERO);
            } else {
                getChildren().remove(body);
                body = null;
                chevron.setText("▸");
                getStyleClass().remove("open");
            }
        }
    }
}
