package detective.ui;

import detective.content.CaseDef;
import detective.content.Line;
import detective.content.Newspaper;
import detective.fx.Motion;
import detective.fx.RainLayer;
import detective.fx.Sfx;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/** 開場：報紙頭條用打字機打出來，接著是組長的台詞。 */
final class BriefingView extends StackPane {

    private final Game game;
    private final List<Runnable> queue = new ArrayList<>();
    private final List<Runnable> finishers = new ArrayList<>();
    private final Button start = new Button("開始調查　→");
    private final double speed;
    private Animation playing;
    private boolean done;

    BriefingView(Game game, CaseDef c) {
        this.game = game;
        this.speed = game.progress.settings().textSpeed().factor;
        Newspaper np = c.newspaper();

        Label masthead = new Label(np.paper());
        masthead.getStyleClass().add("np-masthead");
        Region rule = new Region();
        rule.getStyleClass().add("np-rule");
        Label date = new Label(np.date());
        date.getStyleClass().add("np-date");
        Label headline = new Label();
        headline.getStyleClass().add("np-headline");
        headline.setWrapText(true);
        headline.setMinHeight(Region.USE_PREF_SIZE);
        Label sub = new Label(np.subheadline());
        sub.getStyleClass().add("np-sub");
        sub.setOpacity(0);
        Label body = new Label(np.body());
        body.getStyleClass().add("np-body");
        body.setWrapText(true);
        body.setOpacity(0);

        VBox paper = new VBox(10, masthead, rule, date, headline, sub, body);
        paper.getStyleClass().addAll("paper", "newspaper");
        paper.setMaxWidth(760);
        paper.setRotate(-0.6);

        VBox dialogue = new VBox(10);
        dialogue.setMaxWidth(760);

        Button skip = new Button("跳過");
        skip.getStyleClass().add("btn-ghost");
        skip.setOnAction(e -> revealAll());
        start.getStyleClass().add("btn-primary");
        start.setVisible(false);
        start.setOnAction(e -> game.showWorkspace(c));
        HBox buttons = new HBox(12, skip, start);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setMaxWidth(760);

        VBox content = new VBox(22, paper, dialogue, buttons);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        getChildren().addAll(new RainLayer(), content);

        queue.add(() -> type(headline, np.headline(), 45));
        finishers.add(() -> headline.setText(np.headline()));
        queue.add(() -> {
            fade(sub, 0);
            fade(body, 250);
            game.sfx.play(Sfx.Sound.PAPER);
            next(700);
        });
        finishers.add(() -> {
            sub.setOpacity(1);
            body.setOpacity(1);
        });
        for (Line line : c.briefing()) {
            Label who = new Label(line.who());
            who.getStyleClass().add("dialog-who");
            Label say = new Label();
            say.getStyleClass().add("dialog-say");
            say.setWrapText(true);
            HBox row = new HBox(12, who, say);
            row.getStyleClass().add("dialog-line");
            queue.add(() -> {
                dialogue.getChildren().add(row);
                Motion.rise(row, Duration.ZERO);
                type(say, line.say(), 32);
            });
            finishers.add(() -> {
                if (!dialogue.getChildren().contains(row)) {
                    dialogue.getChildren().add(row);
                }
                row.setOpacity(1);
                row.setTranslateY(0);
                say.setText(line.say());
            });
        }
        queue.add(this::showStart);
        content.setOnMouseClicked(e -> {
            if (playing != null) {
                playing.jumpTo("end");
            }
        });
        if (speed == 0) {
            revealAll();
        } else {
            next(400);
        }
    }

    private void next(double delayMs) {
        if (done || queue.isEmpty()) {
            return;
        }
        Runnable step = queue.removeFirst();
        PauseTransition wait = new PauseTransition(Duration.millis(delayMs));
        wait.setOnFinished(e -> {
            if (!done) {
                step.run();
            }
        });
        wait.play();
    }

    private void type(Label target, String text, double msPerChar) {
        playing = Motion.typewriter(target, text, Duration.millis(msPerChar * speed), () -> game.sfx.play(Sfx.Sound.KEY), () -> {
            playing = null;
            next(350);
        });
    }

    private static void fade(Label label, double delayMs) {
        FadeTransition f = new FadeTransition(Duration.millis(400), label);
        f.setDelay(Duration.millis(delayMs));
        f.setToValue(1);
        f.play();
    }

    private void revealAll() {
        done = true;
        if (playing != null) {
            playing.stop();
        }
        finishers.forEach(Runnable::run);
        showStart();
    }

    private void showStart() {
        if (start.isVisible()) {
            return;
        }
        start.setVisible(true);
        Motion.rise(start, Duration.ZERO);
        start.requestFocus();
    }
}
