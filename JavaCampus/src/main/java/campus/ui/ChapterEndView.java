package campus.ui;

import campus.fx.Motion;
import campus.fx.Sfx;
import campus.story.Chapter;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** 章末：二弘的筆記本一個字一個字寫出來，下面是專題進度條。 */
final class ChapterEndView extends StackPane {

    private static final double BAR = 560;

    ChapterEndView(Game game, Chapter chapter, String note, int previousProgress, Runnable onContinue) {
        getStyleClass().add("chapter-end");
        Label written = new Label();
        written.getStyleClass().add("notebook-text");
        written.setWrapText(true);
        VBox notebook = new VBox(written);
        notebook.getStyleClass().add("notebook");
        notebook.setPrefSize(640, 300);
        notebook.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        notebook.setRotate(-1.2);

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
}
