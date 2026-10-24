package campus.ui;

import campus.content.CaseDef;
import campus.content.Step;
import campus.fx.Motion;
import campus.fx.Sfx;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** 四層提示：線索 → 白話 → 骨架 → 答案。前一層看過才能打開下一層。 */
final class HintDrawer extends VBox {

    static final int TIERS = 4;
    private static final double WIDTH = 400;

    private final Game game;
    private final Runnable onReveal;
    private final VBox tiers = new VBox(12);
    private CaseDef caseDef;
    private Step step;
    private boolean open;

    HintDrawer(Game game, Runnable onReveal) {
        this.game = game;
        this.onReveal = onReveal;
        getStyleClass().add("hint-drawer");
        setPrefWidth(WIDTH);
        setMaxWidth(WIDTH);
        setSpacing(14);
        setTranslateX(WIDTH + 40);
        setVisible(false);

        Button close = new Button("✕");
        close.getStyleClass().add("btn-ghost");
        close.setOnAction(e -> toggle());
        HBox header = new HBox(close);
        header.setAlignment(Pos.CENTER_RIGHT);

        ScrollPane scroll = new ScrollPane(tiers);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().addAll(header, scroll);
    }

    void bind(CaseDef c, Step s) {
        caseDef = c;
        step = s;
        rebuild(-1);
    }

    void toggle() {
        open = !open;
        setVisible(true);
        Motion.slideX(this, open ? 0 : WIDTH + 40, open, () -> setVisible(open));
        game.sfx.play(Sfx.Sound.PAPER);
    }

    private void rebuild(int justRevealed) {
        tiers.getChildren().clear();
        if (step == null) {
            return;
        }
        int revealed = game.progress.hintsRevealed(caseDef.id(), step.id());
        String[] names = {"線索", "白話", "骨架", "答案"};
        for (int i = 0; i < TIERS; i++) {
            Label chip = new Label(names[i]);
            chip.getStyleClass().add("hint-chip");
            if (i == TIERS - 1) {
                chip.getStyleClass().add("answer");
            }
            HBox head = new HBox(chip);
            VBox tier = new VBox(10, head);
            tier.getStyleClass().add("hint-tier");
            if (i < revealed) {
                Node body = body(i);
                tier.getChildren().add(body);
                if (i == justRevealed) {
                    Motion.rise(body, Duration.ZERO);
                }
            } else if (i == revealed) {
                int tierIndex = i;
                Button show = new Button(i == TIERS - 1 ? "顯示答案" : "顯示");
                show.getStyleClass().add(i == TIERS - 1 ? "btn-ghost" : "btn-primary");
                show.setOnAction(e -> reveal(tierIndex));
                tier.getChildren().add(show);
            } else {
                tier.getStyleClass().add("locked");
            }
            tiers.getChildren().add(tier);
        }
    }

    private Node body(int tier) {
        return switch (tier) {
            case 0 -> MiniMarkdown.render(step.hints().lead());
            case 1 -> MiniMarkdown.render(step.hints().plain());
            case 2 -> code(step.skeleton());
            default -> code(step.solution());
        };
    }

    private static Label code(String source) {
        Label l = new Label(source.strip());
        l.getStyleClass().add("hint-code");
        l.setMaxWidth(Double.MAX_VALUE);
        l.setWrapText(true);
        return l;
    }

    private void reveal(int tier) {
        game.progress.setHintsRevealed(caseDef.id(), step.id(), tier + 1);
        game.save();
        game.sfx.play(Sfx.Sound.PAPER);
        rebuild(tier);
        onReveal.run();
    }
}
