package campus.ui;

import campus.fx.Motion;
import campus.fx.Sfx;
import campus.story.Chapter;
import campus.story.Route;
import javafx.animation.PauseTransition;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 章末：視角角色的筆記本，一個字一個字寫出來，下面是專題進度條。
 * 學長視角是二弘樸素的橫線筆記本、藍黑墨水；學妹視角是亭瑄自己的本子，粉色的紙、點線、她的顏色的筆，角落畫了個小盒子。
 */
final class ChapterEndView extends StackPane {

    private static final double BAR = 560;
    private static final double MARGIN_LINE = 52;
    private static final double TEXT_TOP = 46;

    ChapterEndView(Game game, Route route, Chapter chapter, String note, int previousProgress, Runnable onContinue) {
        getStyleClass().add("chapter-end");
        Label written = new Label();
        written.getStyleClass().add("notebook-text");
        written.setWrapText(true);
        VBox text = new VBox(written);
        text.setPadding(new Insets(TEXT_TOP, 44, 36, MARGIN_LINE + 26));
        Pane rules = new Pane();
        StackPane notebook = new StackPane(rules, text);
        notebook.getStyleClass().addAll("notebook", route.name().toLowerCase(Locale.ROOT));
        notebook.setPrefSize(640, 300);
        notebook.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        notebook.setRotate(route == Route.SENPAI ? -1.2 : 1.1);
        InvalidationListener redraw = o -> drawRules(rules, written);
        written.fontProperty().addListener(redraw);
        written.lineSpacingProperty().addListener(redraw);
        rules.widthProperty().addListener(redraw);
        rules.heightProperty().addListener(redraw);
        if (route == Route.KOUHAI) {
            Node doodle = boxDoodle();
            StackPane.setAlignment(doodle, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(doodle, new Insets(0, 38, 30, 0));
            notebook.getChildren().add(doodle);
        }

        Region track = new Region();
        track.getStyleClass().add("progress-track");
        track.setPrefSize(BAR, 14);
        track.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        Region fill = new Region();
        fill.getStyleClass().add("progress-fill");
        fill.setPrefSize(BAR * previousProgress / 100.0, 14);
        fill.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane bar = new StackPane(track, fill);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);
        bar.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        Label percent = new Label(chapter.progress() + "%");
        percent.getStyleClass().add("progress-label");
        HBox progress = new HBox(16, bar, percent);
        progress.setAlignment(Pos.CENTER);

        Button next = new Button("繼續");
        next.getStyleClass().add("btn-primary");
        next.setOnAction(e -> onContinue.run());
        next.setVisible(false);

        VBox content = new VBox(48, notebook, progress, next);
        content.setAlignment(Pos.CENTER);
        getChildren().add(content);

        Runnable reveal = () -> {
            Motion.grow(fill, BAR * chapter.progress() / 100.0, Duration.millis(200));
            PauseTransition later = new PauseTransition(Duration.millis(Motion.isOff() ? 0 : 900));
            later.setOnFinished(e -> {
                next.setVisible(true);
                Motion.rise(next, Duration.ZERO);
                next.requestFocus();
            });
            later.play();
        };
        if (game.progress.settings().textSpeed().factor == 0) {
            written.setText(note);
            reveal.run();
        } else {
            PauseTransition start = new PauseTransition(Duration.millis(500));
            start.setOnFinished(e -> Motion.typewriter(written, note,
                    Duration.millis(110 * game.progress.settings().textSpeed().factor),
                    n -> game.sfx.play(Sfx.Sound.SCRIBBLE), reveal));
            start.play();
        }
        Motion.rise(notebook, Duration.ZERO);
    }

    /** 橫線照字的行高畫，字才會剛好寫在線上；字型是 CSS 給的，要等套上之後才量得到。 */
    private static void drawRules(Pane rules, Label written) {
        Text probe = new Text("字");
        probe.setFont(written.getFont());
        double lineHeight = probe.getLayoutBounds().getHeight();
        probe.setLineSpacing(written.getLineSpacing());
        probe.setText("字\n字");
        double pitch = probe.getLayoutBounds().getHeight() - lineHeight;
        double width = rules.getWidth();
        double height = rules.getHeight();
        if (pitch <= 0 || width <= 0 || height <= 0) {
            return;
        }
        // 線畫在基線稍下面一點，像真的寫在線上；頁首留一段空白
        double onText = TEXT_TOP + probe.getBaselineOffset() + 5;
        double y = onText - Math.floor((onText - 16) / pitch) * pitch;
        List<Node> lines = new ArrayList<>();
        for (; y < height; y += pitch) {
            Line rule = new Line(0, y, width, y);
            rule.getStyleClass().add("notebook-rule");
            lines.add(rule);
        }
        Line margin = new Line(MARGIN_LINE, 0, MARGIN_LINE, height);
        margin.getStyleClass().add("notebook-margin");
        lines.add(margin);
        rules.getChildren().setAll(lines);
    }

    /** 亭瑄在角落隨手畫的盒子，呼應「變數是盒子」；線條故意歪一點才像手畫的。 */
    private static Node boxDoodle() {
        SVGPath box = new SVGPath();
        box.setContent("M2 10 Q12 9.2 24 10.4 Q23.6 21 24.2 31.6 Q13 32.4 2.4 31.8 Q1.6 21 2 10 Z"
                + " M2 10 Q7 5.6 12.2 2.2 Q23 1.6 34 2.4 Q29 6 24 10.4"
                + " M34 2.4 Q33.6 13 34.2 23.6 Q29 27.8 24.2 31.6");
        box.getStyleClass().add("notebook-doodle");
        box.setRotate(-8);
        box.setMouseTransparent(true);
        return box;
    }
}
