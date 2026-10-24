package detective.ui;

import detective.content.CaseDef;
import detective.content.Step;
import detective.fx.Motion;
import detective.fx.Sfx;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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

        Label title = new Label("提示");
        title.getStyleClass().add("detail-title");
        Button close = new Button("關閉");
        close.getStyleClass().add("btn-ghost");
        close.setOnAction(e -> toggle());
        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        HBox header = new HBox(title, gap, close);
        header.setAlignment(Pos.CENTER_LEFT);

        Label note = new Label("看提示不會扣分，但結案報告會記下你用了幾個。卡住的時候，先看「線索」就好。");
        note.getStyleClass().add("muted");
        note.setWrapText(true);

        ScrollPane scroll = new ScrollPane(tiers);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().addAll(header, note, scroll);
    }

    void bind(CaseDef c, Step s) {
        caseDef = c;
        step = s;
        rebuild(-1);
    }

    void toggle() {
        open = !open;
        setVisible(true);
        TranslateTransition t = new TranslateTransition(Duration.millis(260), this);
        t.setToX(open ? 0 : WIDTH + 40);
        t.setInterpolator(Interpolator.EASE_BOTH);
        t.setOnFinished(e -> setVisible(open));
        t.play();
        game.sfx.play(Sfx.Sound.PAPER);
    }

    private void rebuild(int justRevealed) {
        tiers.getChildren().clear();
        if (step == null) {
            return;
        }
        int revealed = game.progress.hintsRevealed(caseDef.id(), step.id());
        String[] chips = {"線索", "提示 1", "提示 2", "答案"};
        String[] titles = {"往哪裡看", "白話說明", "程式骨架（____ 要填空）", "參考答案"};
        for (int i = 0; i < TIERS; i++) {
            Label chip = new Label(chips[i]);
            chip.getStyleClass().add("hint-chip");
            if (i == TIERS - 1) {
                chip.getStyleClass().add("answer");
            }
            Label name = new Label(titles[i]);
            name.getStyleClass().add("step-title");
            HBox head = new HBox(8, chip, name);
            head.setAlignment(Pos.CENTER_LEFT);
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
                Label locked = new Label("先看上一個提示");
                locked.getStyleClass().add("muted");
                tier.getChildren().add(locked);
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
