package campus.ui;

import campus.fx.Motion;
import campus.fx.Sfx;
import campus.story.Cast;
import campus.story.Route;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 劇情裡的手機聊天。外殼畫成 iPhone，聊天介面是原創的，不模仿任何通訊軟體；視角角色自己的訊息靠右。 */
final class PhonePanel extends StackPane {

    /** 機身照 iPhone 的長寬比。 */
    private static final double WIDTH = 360;
    private static final double HEIGHT = 748;
    /** 加上 {@link #SCREEN_RADIUS} 要等於 .phone-body 的外圓角，螢幕的圓角才會和機身同心。 */
    private static final double BEZEL = 12;
    private static final double SCREEN_RADIUS = 46;
    private static final double ISLAND_W = 106;
    private static final double ISLAND_H = 31;
    private static final double ISLAND_TOP = 10;
    private static final Color GLYPH = Color.web("#1b1a20");

    private final Game game;
    private final Route route;
    private final Label clock = new Label();
    private final Label contact = new Label();
    private final VBox messages = new VBox(10);
    private final ScrollPane scroll = new ScrollPane(messages);
    private final Map<String, List<Node>> threads = new HashMap<>();
    private String current;
    private HBox unsent;
    private PauseTransition reply;

    PhonePanel(Game game, Route route) {
        this.game = game;
        this.route = route;
        contact.getStyleClass().add("phone-contact");
        HBox header = new HBox(contact);
        header.setAlignment(Pos.CENTER);
        header.getStyleClass().add("phone-header");
        // 底下多留一點，最後一則訊息才不會壓在底部橫條下面
        messages.setPadding(new Insets(14, 14, 26, 14));
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("phone-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        VBox screen = new VBox(statusBar(), header, scroll);
        screen.getStyleClass().add("phone-screen");
        Rectangle round = new Rectangle();
        round.widthProperty().bind(screen.widthProperty());
        round.heightProperty().bind(screen.heightProperty());
        round.setArcWidth(SCREEN_RADIUS * 2);
        round.setArcHeight(SCREEN_RADIUS * 2);
        screen.setClip(round);

        Region island = pill("phone-island", ISLAND_W, ISLAND_H);
        Region homeBar = pill("phone-home-bar", 112, 5);
        StackPane body = new StackPane(screen, island, homeBar);
        body.getStyleClass().add("phone-body");
        body.setPadding(new Insets(BEZEL));
        StackPane.setAlignment(island, Pos.TOP_CENTER);
        StackPane.setMargin(island, new Insets(ISLAND_TOP, 0, 0, 0));
        StackPane.setAlignment(homeBar, Pos.BOTTOM_CENTER);
        StackPane.setMargin(homeBar, new Insets(0, 0, 8, 0));

        // 側鍵排在機身前面，被機身蓋住一半，只露出邊緣
        StackPane frame = new StackPane(sideKey(true, 118, 28), sideKey(true, 172, 50), sideKey(true, 236, 50),
                sideKey(false, 190, 80), body);
        frame.getStyleClass().add("phone");
        frame.setPrefSize(WIDTH, HEIGHT);
        frame.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        getChildren().add(frame);
        StackPane.setAlignment(frame, Pos.CENTER_RIGHT);
        setPadding(new Insets(0, 120, 60, 0));
        setPickOnBounds(false);
    }

    /** 左右兩塊等寬，時間和訊號圖示各自置中在動態島兩側。 */
    private Node statusBar() {
        clock.getStyleClass().add("phone-clock");
        HBox left = new HBox(clock);
        HBox right = new HBox(5, signal(), wifi(), battery());
        for (HBox side : List.of(left, right)) {
            side.setAlignment(Pos.CENTER);
            side.setMinWidth(0);
            side.setPrefWidth(0);
            side.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(side, Priority.ALWAYS);
        }
        Region island = new Region();
        island.setMinSize(ISLAND_W + 8, ISLAND_H);
        HBox bar = new HBox(left, island, right);
        bar.getStyleClass().add("phone-status");
        bar.setPadding(new Insets(ISLAND_TOP, 6, 2, 6));
        return bar;
    }

    private static Node signal() {
        Group g = new Group();
        for (int i = 0; i < 4; i++) {
            double h = 4 + i * 2.4;
            Rectangle bar = new Rectangle(i * 4.5, 11.5 - h, 3, h);
            bar.setArcWidth(1.5);
            bar.setArcHeight(1.5);
            bar.setFill(GLYPH);
            g.getChildren().add(bar);
        }
        return g;
    }

    private static Node wifi() {
        Arc dot = new Arc(8, 11.5, 3.4, 3.4, 45, 90);
        dot.setType(ArcType.ROUND);
        dot.setFill(GLYPH);
        Group g = new Group(dot);
        for (double r : new double[] {6.6, 9.9}) {
            Arc ring = new Arc(8, 11.5, r, r, 45, 90);
            ring.setType(ArcType.OPEN);
            ring.setFill(null);
            ring.setStroke(GLYPH);
            ring.setStrokeWidth(2.1);
            ring.setStrokeLineCap(StrokeLineCap.ROUND);
            g.getChildren().add(ring);
        }
        return g;
    }

    private static Node battery() {
        Rectangle shell = new Rectangle(0, 0, 23, 11.5);
        shell.setArcWidth(5);
        shell.setArcHeight(5);
        shell.setFill(null);
        shell.setStroke(GLYPH);
        shell.setOpacity(0.4);
        Rectangle level = new Rectangle(2, 2, 15, 7.5);
        level.setArcWidth(3);
        level.setArcHeight(3);
        level.setFill(GLYPH);
        Rectangle nub = new Rectangle(24, 4, 1.6, 3.5);
        nub.setFill(GLYPH);
        nub.setOpacity(0.4);
        return new Group(shell, level, nub);
    }

    private static Region pill(String style, double w, double h) {
        Region r = new Region();
        r.getStyleClass().add(style);
        r.setPrefSize(w, h);
        r.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        r.setMouseTransparent(true);
        return r;
    }

    private static Region sideKey(boolean left, double top, double length) {
        Region key = new Region();
        key.getStyleClass().add("phone-key");
        key.setPrefSize(5, length);
        key.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane.setAlignment(key, left ? Pos.TOP_LEFT : Pos.TOP_RIGHT);
        key.setTranslateX(left ? -3 : 3);
        key.setTranslateY(top);
        return key;
    }

    /**
     * 每個聯絡人各留一串對話，切去跟別人聊再切回來，之前的訊息和附件都還在。
     *
     * @param time 劇情裡現在幾點，不是玩家電腦的時間
     */
    void open(String who, String time) {
        clock.setText(time);
        if (!who.equals(current)) {
            if (current != null) {
                threads.put(current, new ArrayList<>(messages.getChildren()));
            }
            current = who;
            contact.setText(who);
            messages.getChildren().setAll(threads.getOrDefault(who, List.of()));
            unsent = null;
        }
    }

    /** 對方傳來的訊息先出現「⋯」一下，再換成內容；{@code onShown} 在訊息完整出現後呼叫。 */
    void message(String who, String text, boolean mine, boolean instant, Runnable onShown) {
        if (unsent != null) {
            unsent.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            unsent = null;
        }
        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(240);
        bubble.getStyleClass().addAll("bubble", mine ? "mine" : "theirs");
        if (mine) {
            bubble.setStyle("-fx-background-color: " + Cast.colorOf(route.self) + ";");
        }
        if (mine || instant) {
            add(bubble, mine);
            if (!mine) {
                game.sfx.play(Sfx.Sound.NOTIFY);
            }
            onShown.run();
            return;
        }
        Label dots = new Label("⋯");
        dots.getStyleClass().addAll("bubble", "theirs", "typing");
        Node row = add(dots, false);
        reply = new PauseTransition(Duration.millis(520));
        reply.setOnFinished(e -> {
            messages.getChildren().remove(row);
            add(bubble, false);
            game.sfx.play(Sfx.Sound.NOTIFY);
            onShown.run();
        });
        reply.play();
    }

    /** 手機當下的樣子：正在跟誰聊、每個人的對話到哪裡。 */
    record Memory(String current, Map<String, List<Node>> threads, HBox unsent) {}

    Memory remember() {
        Map<String, List<Node>> all = new HashMap<>();
        threads.forEach((who, nodes) -> all.put(who, List.copyOf(nodes)));
        if (current != null) {
            all.put(current, List.copyOf(messages.getChildren()));
        }
        return new Memory(current, all, unsent);
    }

    /** 回到 {@link #remember()} 那一刻，之後才收到的訊息都收回去；還在「⋯」的那則也不會再冒出來。 */
    void restore(Memory m) {
        if (reply != null) {
            reply.stop();
        }
        threads.clear();
        m.threads().forEach((who, nodes) -> threads.put(who, new ArrayList<>(nodes)));
        current = m.current();
        contact.setText(current);
        messages.getChildren().setAll(current == null ? List.of() : threads.get(current));
        unsent = m.unsent();
    }

    /** 附件算是緊接在後面那則訊息的人傳的，等那則訊息出現才決定靠左還是靠右。 */
    void attachment(String fileName, boolean instant, Runnable open) {
        Button file = new Button("📄  " + fileName);
        file.getStyleClass().add("attachment");
        file.setOnAction(e -> open.run());
        unsent = (HBox) add(file, false);
        if (!instant) {
            game.sfx.play(Sfx.Sound.NOTIFY);
        }
    }

    private Node add(Node bubble, boolean mine) {
        HBox row = new HBox(bubble);
        row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messages.getChildren().add(row);
        Motion.rise(row, Duration.ZERO);
        scroll.layout();
        scroll.setVvalue(1);
        return row;
    }
}
