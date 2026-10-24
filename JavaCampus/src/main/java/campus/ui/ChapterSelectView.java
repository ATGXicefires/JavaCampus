package campus.ui;

import campus.fx.Motion;
import campus.story.Chapter;
import campus.story.Route;
import campus.story.StoryState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** 章節選擇：每個視角各自算玩到哪一章，玩過的章節可以從開頭重玩。 */
final class ChapterSelectView extends StackPane {

    private final Game game;
    private final VBox list = new VBox(10);

    ChapterSelectView(Game game) {
        this.game = game;
        getStyleClass().add("chapter-select");
        Button back = new Button("←");
        back.getStyleClass().add("btn-ghost");
        back.setOnAction(e -> game.showTitle());

        ToggleGroup group = new ToggleGroup();
        HBox tabs = new HBox();
        Route initial = game.progress.lastRoute() != null ? game.progress.lastRoute() : Route.SENPAI;
        for (Route r : Route.values()) {
            ToggleButton t = new ToggleButton(r.label);
            t.getStyleClass().add("segment");
            t.setToggleGroup(group);
            t.setUserData(r);
            t.setDisable(game.progress.route(r) == null);
            tabs.getChildren().add(t);
            if (r == initial) {
                t.setSelected(true);
            }
        }
        group.selectedToggleProperty().addListener((obs, old, now) -> {
            if (now == null) {
                group.selectToggle(old);
            } else {
                fill((Route) now.getUserData());
            }
        });

        HBox head = new HBox(24, back, tabs);
        head.setAlignment(Pos.CENTER_LEFT);
        VBox content = new VBox(36, head, list);
        content.setPadding(new Insets(48, 110, 48, 110));
        content.setMaxWidth(900);
        StackPane.setAlignment(content, Pos.TOP_LEFT);
        getChildren().add(content);
        fill(initial);
    }

    private void fill(Route r) {
        list.getChildren().clear();
        StoryState s = game.progress.route(r);
        int i = 0;
        for (Chapter ch : game.story.chapters()) {
            boolean open = s != null && s.hasStarted(ch.id());
            Label number = new Label(String.format("%02d", ch.number()));
            number.getStyleClass().add("chapter-number");
            HBox row = new HBox(24, number);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("chapter-row");
            if (open) {
                Label title = new Label(ch.title());
                title.getStyleClass().add("chapter-name");
                row.getChildren().add(title);
                row.getStyleClass().add("pressable");
                row.setOnMouseClicked(e -> game.confirm("從第 " + ch.number() + " 章開頭重玩，之後的進度以這次為準。", "重玩",
                        () -> game.replayChapter(r, ch)));
                Motion.hoverLift(row);
            } else {
                row.getStyleClass().add("locked");
            }
            row.setMaxWidth(Region.USE_PREF_SIZE);
            row.setMinWidth(520);
            list.getChildren().add(row);
            Motion.rise(row, Duration.millis(60 * i++));
        }
    }
}
