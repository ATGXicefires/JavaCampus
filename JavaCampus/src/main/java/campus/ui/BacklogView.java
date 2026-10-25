package campus.ui;

import campus.story.Cast;
import campus.story.Op;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

import java.util.List;

/** 這一章讀過的對話，最新的在最下面。每一句旁邊有個回去的按鈕，可以從那一句重新播。 */
final class BacklogView extends VBox {

    /**
     * @param choice 玩家選過的選項，顯示成「→ 選項文字」
     * @param jump   回到這一句重新播；選項是 null
     */
    record Entry(Op.Kind kind, String who, String text, boolean choice, Runnable jump) {

        Entry(Op.Kind kind, String who, String text, Runnable jump) {
            this(kind, who, text, false, jump);
        }

        static Entry choice(String text) {
            return new Entry(Op.Kind.NARRATION, null, text, true, null);
        }
    }

    /** @param canJump 劇情正在演暗場、等待這類指令時不能回去，按鈕就不出現 */
    BacklogView(List<Entry> entries, boolean canJump) {
        getStyleClass().addAll("overlay-panel", "backlog");
        setPrefSize(820, 620);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        VBox list = new VBox(14);
        for (int i = 0; i < entries.size(); i++) {
            // 最後一句就是畫面上正在看的那一句，回去等於沒動
            list.getChildren().add(row(entries.get(i), canJump && i < entries.size() - 1));
        }
        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(580);
        getChildren().add(scroll);
        Platform.runLater(() -> scroll.setVvalue(1));
    }

    private static HBox row(Entry e, boolean canJump) {
        Label who = new Label(e.kind() == Op.Kind.SAY ? e.who() : "");
        who.getStyleClass().add("backlog-who");
        if (e.kind() == Op.Kind.SAY) {
            who.setStyle("-fx-text-fill: " + Cast.colorOf(e.who()) + ";");
        }
        String shown = e.choice() ? "→ " + e.text() : e.kind() == Op.Kind.OS ? "（" + e.text() + "）" : e.text();
        Label text = new Label(shown);
        text.setWrapText(true);
        text.getStyleClass().add("backlog-text");
        if (e.kind() == Op.Kind.OS) {
            text.getStyleClass().add("os");
        }
        if (e.choice()) {
            text.getStyleClass().add("choice-made");
        }
        text.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(text, Priority.ALWAYS);
        HBox row = new HBox(16, who, text);
        who.setMinWidth(90);
        if (canJump && e.jump() != null) {
            Button back = new Button();
            back.setGraphic(returnArrow());
            back.getStyleClass().add("backlog-jump");
            back.setFocusTraversable(false);
            back.setOnAction(x -> e.jump().run());
            row.getChildren().add(back);
        }
        return row;
    }

    /** 往左回頭的箭頭；字型裡不一定有 ↩，所以自己畫。 */
    private static SVGPath returnArrow() {
        SVGPath p = new SVGPath();
        p.setContent("M7 3 L2 8 L7 13 M2 8 H12 C15 8 17 10 17 13 C17 16 15 18 12 18 H8");
        p.getStyleClass().add("backlog-jump-icon");
        return p;
    }
}
