package detective.ui;

import detective.content.CaseDef;
import detective.content.Chapter;
import detective.fx.Motion;
import detective.fx.RainLayer;
import detective.fx.Sfx;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

final class CaseSelectView extends StackPane {

    CaseSelectView(Game game) {
        Label title = new Label("Java 偵探社");
        title.getStyleClass().add("title-big");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(title, spacer, SoundMenu.create(game));
        header.setAlignment(Pos.TOP_LEFT);

        VBox content = new VBox(28, header);
        content.setPadding(new Insets(48, 64, 48, 64));
        int delay = 0;
        for (Chapter chapter : game.chapters) {
            Label chapterTitle = new Label(chapter.title());
            chapterTitle.getStyleClass().add("chapter-title");
            HBox cards = new HBox(24);
            for (CaseDef c : chapter.cases()) {
                VBox card = card(game, c);
                HBox.setHgrow(card, Priority.ALWAYS);
                cards.getChildren().add(card);
                Motion.rise(card, Duration.millis(120 + 90 * delay++));
            }
            content.getChildren().addAll(chapterTitle, cards);
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        getChildren().addAll(new RainLayer(), scroll);
    }

    private static VBox card(Game game, CaseDef c) {
        boolean unlocked = game.isUnlocked(c);
        boolean closed = game.isClosed(c);
        Label num = new Label("第 " + game.caseNumber(c) + " 案");
        num.getStyleClass().add("case-num");
        Label title = new Label(c.title());
        title.getStyleClass().add("case-title");
        FlowPane chips = new FlowPane(6, 6);
        for (String concept : c.concepts()) {
            Label chip = new Label(concept);
            chip.getStyleClass().add("chip");
            chips.getChildren().add(chip);
        }
        Label summary = new Label(c.summary());
        summary.getStyleClass().add("case-summary");
        summary.setWrapText(true);

        Region headerGap = new Region();
        HBox.setHgrow(headerGap, Priority.ALWAYS);
        HBox header = new HBox(num, headerGap);
        if (closed) {
            Label stamp = new Label("已結案");
            stamp.getStyleClass().add("mini-stamp");
            header.getChildren().add(stamp);
        }

        VBox card = new VBox(10, header, title, chips, summary);
        card.getStyleClass().add("case-card");
        card.setPrefWidth(340);
        card.setMinHeight(250);
        if (!unlocked) {
            card.getStyleClass().add("locked");
            card.setOpacity(0.5);
            return card;
        }
        card.getStyleClass().add("pressable");
        Motion.hoverLift(card);
        card.setOnMouseClicked(e -> {
            game.sfx.play(Sfx.Sound.PAPER);
            if (closed) {
                game.showWorkspace(c);
            } else {
                game.showBriefing(c);
            }
        });
        return card;
    }
}
