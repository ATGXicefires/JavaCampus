package campus.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/** 跳過劇情之後，用劇本裡的 {@code @簡介} 交代跳過的那段發生了什麼。 */
final class SummaryView extends VBox {

    SummaryView(Game game, List<String> paragraphs) {
        super(14);
        getStyleClass().addAll("overlay-panel", "summary");
        setPrefWidth(620);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        Label title = new Label("劇情簡介");
        title.getStyleClass().add("summary-title");
        getChildren().add(title);
        for (String p : paragraphs) {
            Label text = new Label(p);
            text.setWrapText(true);
            text.getStyleClass().add("summary-text");
            getChildren().add(text);
        }
        Button ok = new Button("繼續");
        ok.getStyleClass().add("btn-primary");
        ok.setDefaultButton(true);
        ok.setOnAction(e -> game.closeOverlay());
        HBox buttons = new HBox(ok);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        getChildren().add(buttons);
        // 跟看劇情一樣，按 Enter 或空白鍵就能接著玩
        sceneProperty().addListener((obs, was, now) -> {
            if (now != null) {
                ok.requestFocus();
            }
        });
    }
}
