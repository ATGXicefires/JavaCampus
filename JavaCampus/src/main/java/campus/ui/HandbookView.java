package campus.ui;

import campus.fx.Motion;
import campus.lesson.Handbook;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** 「程式基礎教學筆記」：左邊依章列出條目，沒解鎖的只顯示「⋯⋯」；右邊先是一句重點，再接完整內容。 */
final class HandbookView extends HBox {

    private static final String[] NUMERALS = {"零", "一", "二", "三", "四", "五", "六", "七"};

    private final Game game;
    private final VBox detail = new VBox(14);
    private final ScrollPane detailScroll = new ScrollPane(detail);
    private Label selected;

    HandbookView(Game game) {
        this.game = game;
        getStyleClass().addAll("overlay-panel", "handbook");
        setSpacing(24);
        setPrefSize(1000, 640);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        VBox list = new VBox(4);
        Map<Integer, List<Handbook.Entry>> byChapter = game.lessons.handbook().entries().stream()
                .collect(Collectors.groupingBy(Handbook.Entry::chapter, TreeMap::new, Collectors.toList()));
        Handbook.Entry first = null;
        for (Map.Entry<Integer, List<Handbook.Entry>> ch : byChapter.entrySet()) {
            Label head = new Label("第" + NUMERALS[Math.min(ch.getKey(), NUMERALS.length - 1)] + "章");
            head.getStyleClass().add("handbook-chapter");
            head.setPadding(new Insets(10, 0, 4, 0));
            list.getChildren().add(head);
            for (Handbook.Entry e : ch.getValue()) {
                boolean open = game.progress.notes().contains(e.id());
                Label item = new Label(open ? e.title() : "⋯⋯");
                item.getStyleClass().add("handbook-item");
                item.setMaxWidth(Double.MAX_VALUE);
                if (open) {
                    item.getStyleClass().add("pressable");
                    item.setOnMouseClicked(x -> select(item, e));
                    if (first == null) {
                        first = e;
                        selected = item;
                    }
                } else {
                    item.getStyleClass().add("locked");
                }
                list.getChildren().add(item);
            }
        }
        ScrollPane listScroll = new ScrollPane(list);
        listScroll.setFitToWidth(true);
        listScroll.setPrefWidth(280);
        listScroll.setMinWidth(280);
        detailScroll.setFitToWidth(true);
        HBox.setHgrow(detailScroll, Priority.ALWAYS);
        detail.getStyleClass().add("handbook-detail");
        getChildren().addAll(listScroll, detailScroll);
        if (first != null) {
            select(selected, first);
        } else {
            Label empty = new Label("⋯⋯");
            empty.getStyleClass().add("muted");
            detail.getChildren().setAll(empty);
        }
    }

    private void select(Label item, Handbook.Entry e) {
        if (selected != null) {
            selected.getStyleClass().remove("current");
        }
        selected = item;
        item.getStyleClass().add("current");
        Label title = new Label(e.title());
        title.getStyleClass().add("handbook-title");
        TextFlow lead = MiniMarkdown.inline(e.summary());
        lead.getStyleClass().add("handbook-lead");
        detail.getChildren().setAll(title, lead, MiniMarkdown.render(e.body()));
        // 彈簧會衝過頭一點，動 detail 的話超出捲動區的部分會被切掉；整個捲動區一起動，多出來的落在面板內距裡
        Motion.rise(detailScroll, Duration.ZERO);
    }
}
