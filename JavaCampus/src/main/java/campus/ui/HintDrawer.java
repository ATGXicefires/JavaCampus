package campus.ui;

import campus.lesson.Lesson;
import campus.lesson.SourceFile;
import campus.lesson.Step;
import campus.fx.Motion;
import campus.fx.Sfx;
import campus.progress.Progress;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.List;

/** 四層提示：提點 → 白話 → 骨架 → 答案。前一層看過才能打開下一層。 */
final class HintDrawer extends VBox {

    static final int TIERS = 4;
    private static final double WIDTH = 400;

    private final Game game;
    private final Progress.LessonRecord record;
    private final Runnable onReveal;
    private final VBox tiers = new VBox(12);
    private Lesson lesson;
    private Step step;
    private boolean open;

    HintDrawer(Game game, Progress.LessonRecord record, Runnable onReveal) {
        this.game = game;
        this.record = record;
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

        TextSelection.install(tiers);
        ScrollPane scroll = new ScrollPane(tiers);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().addAll(header, scroll);
    }

    void bind(Lesson l, Step s) {
        lesson = l;
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
        int revealed = record.hintsRevealed(lesson.id(), step.id());
        String[] names = {"提點", "白話", "骨架", "答案"};
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

    /** 好幾個檔案時，每段程式碼上面標出檔名。 */
    private static Node code(List<SourceFile> files) {
        if (files.size() == 1) {
            return codeBlock(files.getFirst().text());
        }
        VBox box = new VBox(6);
        for (SourceFile f : files) {
            Label name = new Label(f.name());
            name.getStyleClass().add("hint-file");
            box.getChildren().addAll(name, codeBlock(f.text()));
        }
        return box;
    }

    /** 用 TextFlow 而不是 Label，骨架和答案才選取得到；字串引號裡的空格跟題目一樣畫成看得見的。 */
    private static TextFlow codeBlock(String source) {
        TextFlow flow = new TextFlow();
        flow.getChildren().addAll(MiniMarkdown.texts(MiniMarkdown.visibleSpaces(source.strip(), false), "hint-code-text"));
        flow.getStyleClass().add("hint-code");
        return flow;
    }

    private void reveal(int tier) {
        record.setHintsRevealed(lesson.id(), step.id(), tier + 1);
        game.save();
        game.sfx.play(Sfx.Sound.PAPER);
        rebuild(tier);
        onReveal.run();
    }
}
