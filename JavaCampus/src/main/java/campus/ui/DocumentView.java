package campus.ui;

import campus.fx.Motion;
import campus.fx.Sfx;
import campus.story.Doc;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** PDF、術語文件、程式檔、手寫紙條共用的檢視器，依外觀換樣式，可以左右翻頁。 */
final class DocumentView extends VBox {

    private final Game game;
    private final Doc doc;
    private final StackPane page = new StackPane();
    private final Label counter = new Label();
    private final Button prev = new Button("←");
    private final Button next = new Button("→");
    private int index;

    DocumentView(Game game, Doc doc) {
        this.game = game;
        this.doc = doc;
        getStyleClass().addAll("document", "doc-" + doc.skin().name().toLowerCase());
        setSpacing(14);
        setPrefSize(doc.skin() == Doc.Skin.HANDWRITTEN ? 640 : 820, 660);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        Button close = new Button("✕");
        close.getStyleClass().add("btn-ghost");
        close.setOnAction(e -> game.closeOverlay());
        HBox head = new HBox(close);
        head.setAlignment(Pos.CENTER_RIGHT);

        page.getStyleClass().add("doc-page");
        VBox.setVgrow(page, Priority.ALWAYS);

        prev.getStyleClass().add("btn-ghost");
        next.getStyleClass().add("btn-ghost");
        prev.setOnAction(e -> turn(-1));
        next.setOnAction(e -> turn(1));
        counter.getStyleClass().add("doc-counter");
        HBox nav = new HBox(16, prev, counter, next);
        nav.setAlignment(Pos.CENTER);
        nav.setVisible(doc.pages().size() > 1);
        nav.managedProperty().bind(nav.visibleProperty());

        getChildren().addAll(head, page, nav);
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.PAGE_DOWN) {
                turn(1);
            } else if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.PAGE_UP) {
                turn(-1);
            }
        });
        show(0);
        sceneProperty().addListener((obs, was, now) -> {
            if (now != null) {
                requestFocus();
            }
        });
    }

    private void turn(int delta) {
        int to = index + delta;
        if (to < 0 || to >= doc.pages().size()) {
            return;
        }
        game.sfx.play(Sfx.Sound.PAPER);
        show(to);
        Motion.rise(page, Duration.ZERO);
    }

    private void show(int i) {
        index = i;
        VBox body = MiniMarkdown.render(doc.pages().get(i));
        body.getStyleClass().add("doc-body");
        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        page.getChildren().setAll(scroll);
        counter.setText((i + 1) + " / " + doc.pages().size());
        prev.setDisable(i == 0);
        next.setDisable(i == doc.pages().size() - 1);
    }
}
