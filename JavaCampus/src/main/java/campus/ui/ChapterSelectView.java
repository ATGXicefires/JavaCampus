package campus.ui;

import campus.fx.Motion;
import campus.story.Chapter;
import campus.story.Route;
import campus.story.StoryLibrary;
import campus.story.StoryState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;
import java.util.Locale;

/**
 * 章節選擇，做成攤開的筆記本目錄：前一半的章節寫在左頁，其餘寫在右頁。
 * 每頁紅線左邊是章號，右邊是章名；視角切換是左頁頂上的索引標籤。
 * 每個視角各自算玩到哪一章，玩過的章節可以從開頭重玩；沒玩到和還沒寫的章節變淡、加鎖，不能點。
 */
final class ChapterSelectView extends StackPane {

    private static final double PAGE_WIDTH = 540;
    /** 一列的高度，也是 CSS 裡 {@code .toc-page} 橫線的間距，兩邊要一起改。 */
    private static final double ROW = 64;
    private static final double MARGIN_LINE = 96;
    private static final double NUMBER_WIDTH = 78;
    /** 中縫的陰影寬度，從裝訂線往兩頁各暈開一半。 */
    private static final double SEAM = 40;

    private final Game game;
    private final VBox sheet = new VBox();
    private final VBox left = new VBox();
    private final VBox right = new VBox();

    ChapterSelectView(Game game) {
        this.game = game;
        getStyleClass().add("chapter-select");
        Button back = new Button("←");
        back.getStyleClass().add("btn-ghost");
        back.setOnAction(e -> game.showTitle());
        StackPane.setAlignment(back, Pos.TOP_LEFT);
        StackPane.setMargin(back, new Insets(48, 0, 0, 110));

        ToggleGroup group = new ToggleGroup();
        HBox tabs = new HBox(6);
        tabs.setPadding(new Insets(0, 0, 0, MARGIN_LINE + 22));
        Route initial = game.progress.lastRoute() != null ? game.progress.lastRoute() : Route.SENPAI;
        for (Route r : Route.values()) {
            ToggleButton t = new ToggleButton(r.label);
            t.getStyleClass().addAll("toc-tab", styleOf(r));
            t.setToggleGroup(group);
            t.setUserData(r);
            t.setDisable(game.progress.route(r) == null);
            tabs.getChildren().add(t);
            if (r == initial) {
                t.setSelected(true);
            }
        }
        group.selectedToggleProperty().addListener((obs, old, now) -> {
            if (now == null) {
                group.selectToggle(old);
            } else {
                fill((Route) now.getUserData());
            }
        });

        Region under = new Region();
        under.getStyleClass().add("toc-under");
        under.setRotate(1.3);
        Region seam = new Region();
        seam.getStyleClass().add("toc-seam");
        seam.setPrefWidth(SEAM);
        seam.setMaxWidth(Region.USE_PREF_SIZE);
        seam.setMouseTransparent(true);
        StackPane spread = new StackPane(under, new HBox(page(left), page(right)), seam);

        // 墊紙轉了一點角度，左上角會翹到標籤底下；標籤要畫在墊紙上面，選中的那個才會跟頁面接在一起
        tabs.setViewOrder(-1);
        sheet.getChildren().addAll(tabs, spread);
        sheet.getStyleClass().add("toc-sheet");
        sheet.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        getChildren().addAll(sheet, back);
        fill(initial);
    }

    /** 一頁橫線紙，左邊畫紅線。 */
    private static StackPane page(VBox list) {
        list.getStyleClass().add("toc-page");
        // 底下只留半列，最後一條橫線才不會壓在紙的邊上
        list.setPadding(new Insets(ROW, 56, 40, 0));
        list.setPrefWidth(PAGE_WIDTH);
        Region margin = new Region();
        margin.getStyleClass().add("toc-margin");
        margin.setPrefWidth(2);
        margin.setMaxWidth(Region.USE_PREF_SIZE);
        margin.setMouseTransparent(true);
        StackPane.setAlignment(margin, Pos.TOP_LEFT);
        StackPane.setMargin(margin, new Insets(0, 0, 0, MARGIN_LINE));
        return new StackPane(list, margin);
    }

    private static String styleOf(Route r) {
        return r.name().toLowerCase(Locale.ROOT);
    }

    private void fill(Route r) {
        for (Route other : Route.values()) {
            sheet.getStyleClass().remove(styleOf(other));
        }
        sheet.getStyleClass().add(styleOf(r));
        left.getChildren().clear();
        right.getChildren().clear();
        StoryState s = game.progress.route(r);
        List<StoryLibrary.Listing> contents = game.story.contents();
        int half = (contents.size() + 1) / 2;
        int i = 0;
        for (StoryLibrary.Listing listing : contents) {
            Chapter ch = listing.chapter();
            boolean open = ch != null && s != null && s.hasStarted(ch.id());
            Label number = new Label(String.format("%02d", listing.number()));
            number.getStyleClass().add("toc-number");
            number.setMinWidth(NUMBER_WIDTH);
            number.setAlignment(Pos.BOTTOM_RIGHT);
            Label title = new Label(listing.title());
            title.getStyleClass().add("toc-title");
            HBox.setMargin(title, new Insets(0, 0, 0, MARGIN_LINE + 22 - NUMBER_WIDTH));
            Region rest = new Region();
            HBox.setHgrow(rest, Priority.ALWAYS);
            HBox row = new HBox(number, title, rest);
            row.setAlignment(Pos.BOTTOM_LEFT);
            row.setMinHeight(ROW);
            row.setPrefHeight(ROW);
            row.setMaxHeight(ROW);
            row.getStyleClass().add("toc-row");
            if (open && s.cleared().contains(ch.id())) {
                row.getStyleClass().add("cleared");
            }
            if (open) {
                row.getStyleClass().addAll("open", "pressable");
                row.setOnMouseClicked(e -> game.confirm("從第 " + ch.number() + " 章開頭重玩，之後的進度以這次為準。", "重玩",
                        () -> game.replayChapter(r, ch)));
                Motion.hoverLift(row);
            } else {
                row.getStyleClass().add("locked");
                Node lock = lock();
                // 鎖的底邊對齊字的基線，而不是對齊含下伸部的字框底
                HBox.setMargin(lock, new Insets(0, 0, 9, 0));
                row.getChildren().add(lock);
            }
            (i < half ? left : right).getChildren().add(row);
            Motion.rise(row, Duration.millis(50 * i++));
        }
    }

    /** 方的鎖頭，和全直角的介面一致；顏色交給 CSS。 */
    private static Node lock() {
        Polyline shackle = new Polyline(3.5, 8, 3.5, 2, 10.5, 2, 10.5, 8);
        shackle.getStyleClass().add("toc-lock-shackle");
        Rectangle body = new Rectangle(0, 7.5, 14, 9.5);
        body.getStyleClass().add("toc-lock-body");
        Pane lock = new Pane(shackle, body);
        lock.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        return lock;
    }
}
