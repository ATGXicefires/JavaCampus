package campus.ui;

import campus.art.BackdropView;
import campus.art.FigureView;
import campus.fx.Motion;
import campus.story.Route;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Arrays;

/** 標題畫面：傍晚的電腦教室，兩個人的剪影，左邊是選單。 */
final class TitleView extends StackPane {

    static final String TITLE = "JavaCampus";

    private final Game game;
    private final VBox menu = new VBox(6);

    TitleView(Game game) {
        this.game = game;
        getStyleClass().add("title-screen");
        BackdropView bg = new BackdropView(game.art, "電腦教室", "傍晚");
        Region shade = new Region();
        shade.getStyleClass().add("title-shade");

        FigureView erhong = new FigureView(game.art, "二弘", "一般");
        FigureView tingxuan = new FigureView(game.art, "亭瑄", "一般");
        HBox cast = new HBox(-60, erhong, tingxuan);
        cast.setAlignment(Pos.BOTTOM_RIGHT);
        cast.setPadding(new Insets(0, 80, 0, 0));
        cast.setMouseTransparent(true);
        for (FigureView f : new FigureView[] {erhong, tingxuan}) {
            f.prefHeightProperty().bind(heightProperty().multiply(0.82));
            f.setMaxHeight(Region.USE_PREF_SIZE);
        }

        Label title = new Label(TITLE);
        title.getStyleClass().add("title-big");
        menu.getStyleClass().add("title-menu");
        boolean anyCleared = Arrays.stream(Route.values())
                .anyMatch(r -> game.progress.route(r) != null && !game.progress.route(r).cleared().isEmpty());
        if (game.progress.lastRoute() != null) {
            menu.getChildren().add(item("繼續", game::continueGame));
        }
        menu.getChildren().add(item("新遊戲", game::showRouteSelect));
        if (anyCleared) {
            menu.getChildren().add(item("章節", game::showChapterSelect));
        }
        menu.getChildren().addAll(
                item("筆記", () -> game.openOverlay(new HandbookView(game), null)),
                item("設定", game::openSettings),
                item("離開", game::exit));
        if (game.newVersion() != null) {
            addUpdate(game.newVersion());
        }
        VBox left = new VBox(48, title, menu);
        left.setAlignment(Pos.CENTER_LEFT);
        left.setPadding(new Insets(0, 0, 0, 110));
        left.setMaxWidth(Region.USE_PREF_SIZE);
        StackPane.setAlignment(left, Pos.CENTER_LEFT);

        getChildren().addAll(bg, shade, cast, left);
        Motion.rise(title, Duration.millis(120));
        for (int i = 0; i < menu.getChildren().size(); i++) {
            Motion.rise(menu.getChildren().get(i), Duration.millis(260 + 60 * i));
        }
    }

    /** 標題畫面開著時才查到新版本。 */
    void offerUpdate(String version) {
        Motion.rise(addUpdate(version), Duration.ZERO);
    }

    private Button addUpdate(String version) {
        Button b = item("更新到 v" + version, game::update);
        menu.getChildren().add(menu.getChildren().size() - 1, b);
        return b;
    }

    private static Button item(String text, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().add("title-item");
        b.setOnAction(e -> action.run());
        Motion.hoverLift(b);
        return b;
    }
}
