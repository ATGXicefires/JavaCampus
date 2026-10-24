package campus.ui;

import campus.content.CaseDef;
import campus.content.Chapter;
import campus.fx.Motion;
import campus.fx.Sfx;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Optional;

/** 結案報告：答案蓋上「結案」章，推理鏈一條一條浮現。 */
final class VerdictView extends StackPane {

    VerdictView(Game game, CaseDef c) {
        Label kicker = new Label("第 " + game.caseNumber(c) + " 案");
        kicker.getStyleClass().add("vp-kicker");
        Label title = new Label(c.title());
        title.getStyleClass().add("vp-title");
        Label question = new Label(c.openQuestion());
        question.getStyleClass().add("vp-question");
        question.setWrapText(true);
        Label answer = new Label(c.conclusion().answer());
        answer.getStyleClass().add("vp-answer");
        Label stamp = new Label("結案");
        stamp.getStyleClass().add("stamp");
        stamp.setOpacity(0);
        StackPane answerRow = new StackPane(answer, stamp);
        StackPane.setAlignment(answer, Pos.CENTER_LEFT);
        StackPane.setAlignment(stamp, Pos.CENTER_RIGHT);
        Label detail = new Label(c.conclusion().detail());
        detail.getStyleClass().add("vp-detail");
        detail.setWrapText(true);

        VBox chain = new VBox(10);
        for (int i = 0; i < c.explanation().size(); i++) {
            Label num = new Label(String.valueOf(i + 1));
            num.getStyleClass().add("explain-num");
            HBox row = new HBox(10, num, MiniMarkdown.inline(c.explanation().get(i)));
            row.setAlignment(Pos.TOP_LEFT);
            chain.getChildren().add(row);
        }
        int used = game.progress.totalHints(c.id());
        Label stats = new Label(used == 0 ? "沒有使用任何提示。" : "這個案件看了 " + used + " 個提示。");
        stats.getStyleClass().add("vp-question");

        VBox paper = new VBox(14, kicker, title, question, answerRow, detail, new Separator(), chain, stats);
        paper.getStyleClass().addAll("paper", "verdict-paper");
        paper.setMaxWidth(720);

        Button back = new Button("案件列表");
        back.getStyleClass().add("btn-ghost");
        back.setOnAction(e -> game.showCases());
        Button review = new Button("調查畫面");
        review.getStyleClass().add("btn-ghost");
        review.setOnAction(e -> game.showWorkspace(c));
        HBox buttons = new HBox(12, back, review);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setMaxWidth(720);
        Chapter chapter = game.chapterOf(c);
        if (chapter.cases().getLast() == c) {
            Label end = new Label(chapter.title() + "　完");
            end.getStyleClass().add("chapter-end");
            buttons.getChildren().addFirst(end);
        }
        Optional<CaseDef> next = game.nextCase(c);
        next.ifPresent(n -> {
            Button go = new Button("下一案：" + n.title() + "　→");
            go.getStyleClass().add("btn-primary");
            go.setOnAction(e -> game.showBriefing(n));
            buttons.getChildren().add(go);
        });

        VBox content = new VBox(20, paper, buttons);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(36));
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        getChildren().add(scroll);

        PauseTransition beforeStamp = new PauseTransition(Duration.millis(650));
        beforeStamp.setOnFinished(e -> Motion.stamp(stamp, paper, () -> game.sfx.play(Sfx.Sound.STAMP)));
        beforeStamp.play();
        for (int i = 0; i < chain.getChildren().size(); i++) {
            Duration at = Duration.millis(1300 + 420 * i);
            Motion.rise(chain.getChildren().get(i), at);
            PauseTransition sound = new PauseTransition(at);
            sound.setOnFinished(e -> game.sfx.play(Sfx.Sound.PAPER));
            sound.play();
        }
    }
}
