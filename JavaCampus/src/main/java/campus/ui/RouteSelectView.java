package campus.ui;

import campus.art.FigureView;
import campus.fx.Motion;
import campus.story.Cast;
import campus.story.Route;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** 選視角：左右對分，滑過的那一半亮起來、另一半暗下去。 */
final class RouteSelectView extends StackPane {

    private final Game game;
    private final HBox halves = new HBox();

    RouteSelectView(Game game) {
        this.game = game;
        getStyleClass().add("route-select");
        StackPane senpai = half(Route.SENPAI, "二弘", "吳二弘");
        StackPane kouhai = half(Route.KOUHAI, "亭瑄", "梁亭瑄");
        HBox.setHgrow(senpai, Priority.ALWAYS);
        HBox.setHgrow(kouhai, Priority.ALWAYS);
        halves.getChildren().addAll(senpai, kouhai);

        Button back = new Button("←");
        back.getStyleClass().add("btn-ghost");
        back.setOnAction(e -> game.showTitle());
        StackPane.setAlignment(back, Pos.TOP_LEFT);
        StackPane.setMargin(back, new Insets(24));
        getChildren().addAll(halves, back);
    }

    private StackPane half(Route route, String person, String name) {
        StackPane pane = new StackPane();
        pane.getStyleClass().addAll("route-half", "route-" + route.fileSuffix, "pressable");
        pane.setPrefWidth(0);
        FigureView figure = new FigureView(game.art, person, "一般");
        figure.prefHeightProperty().bind(pane.heightProperty().multiply(0.78));
        figure.setMaxHeight(Region.USE_PREF_SIZE);
        figure.setMaxWidth(Region.USE_PREF_SIZE);
        StackPane.setAlignment(figure, Pos.BOTTOM_CENTER);
        Label label = new Label(name);
        label.getStyleClass().add("route-name");
        label.setStyle("-fx-text-fill: " + Cast.colorOf(person) + ";");
        VBox top = new VBox(label);
        top.setAlignment(Pos.TOP_CENTER);
        top.setPadding(new Insets(70, 0, 0, 0));
        top.setMouseTransparent(true);
        pane.getChildren().addAll(figure, top);
        pane.setOnMouseEntered(e -> focus(pane, figure));
        pane.setOnMouseExited(e -> focus(null, null));
        pane.setOnMouseClicked(e -> choose(route));
        return pane;
    }

    private void focus(StackPane hovered, FigureView figure) {
        for (var n : halves.getChildren()) {
            double target = hovered == null || n == hovered ? 1 : 0.45;
            if (Motion.isOff()) {
                n.setOpacity(target);
            } else {
                new Timeline(new KeyFrame(Duration.millis(220), new KeyValue(n.opacityProperty(), target))).play();
            }
        }
        if (figure != null) {
            Motion.bob(figure);
        }
    }

    private void choose(Route route) {
        if (game.progress.route(route) != null) {
            game.confirm("從頭開始" + route.label + "視角，會覆蓋這個視角目前的進度。", "開始", () -> game.newGame(route));
        } else {
            game.newGame(route);
        }
    }
}
