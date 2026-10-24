package campus.ui;

import campus.story.Cast;
import campus.story.Op;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/** 這一章讀過的對話，最新的在最下面。 */
final class BacklogView extends VBox {

    /** @param choice 玩家選過的選項，顯示成「→ 選項文字」 */
    record Entry(Op.Kind kind, String who, String text, boolean choice) {

        Entry(Op.Kind kind, String who, String text) {
            this(kind, who, text, false);
        }

        static Entry choice(String text) {
            return new Entry(Op.Kind.NARRATION, null, text, true);
        }
    }

    BacklogView(List<Entry> entries) {
        getStyleClass().addAll("overlay-panel", "backlog");
        setPrefSize(820, 620);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        VBox list = new VBox(14);
        for (Entry e : entries) {
            list.getChildren().add(row(e));
        }
        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(580);
        getChildren().add(scroll);
        Platform.runLater(() -> scroll.setVvalue(1));
    }

    private static HBox row(Entry e) {
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
        HBox row = new HBox(16, who, text);
        who.setMinWidth(90);
        return row;
    }
}
