package campus.ui;

import campus.art.BackdropView;
import campus.art.FigureView;
import campus.art.Figures;
import campus.fx.Motion;
import campus.story.Chapter;
import campus.story.Route;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;

/** 標題畫面：傍晚的電腦教室，左邊是作品名和選單，右邊兩個人的剪影隨劇情進度出現、靠近、換表情。 */
final class TitleView extends StackPane {

    /**
     * 標題畫面上兩個人的樣子。
     *
     * @param afterChapter 玩完這一章之後換成這個樣子
     * @param gap          兩人之間的距離，負的是疊在一起
     */
    record Pose(int afterChapter, double gap, String erhong, String tingxuan) {}

    /** 照 {@code story/outline/story-overview.md} 的感情節奏；還沒玩完第一章時兩個人還不認識，教室裡沒有人。 */
    private static final List<Pose> POSES = List.of(
            new Pose(1, 60, "一般", "一般"),
            new Pose(6, -30, "一般", "笑"),
            new Pose(8, -30, "一般", "害羞"),
            new Pose(9, -30, "害羞", "害羞"),
            new Pose(11, -90, "心動", "心動"));

    private static final Duration PER_CHAR = Duration.millis(32);
    private static final Duration BLINK = Duration.millis(530);

    private final Game game;
    private final VBox menu = new VBox(6);

    TitleView(Game game) {
        this.game = game;
        getStyleClass().add("title-screen");
        BackdropView bg = new BackdropView(game.art, "電腦教室", "傍晚");
        Region shade = new Region();
        shade.getStyleClass().add("title-shade");
        getChildren().addAll(bg, shade);

        Pose pose = poseAfter(furthestCleared(game));
        if (pose != null) {
            FigureView erhong = new FigureView(game.art, "二弘", pose.erhong());
            FigureView tingxuan = new FigureView(game.art, "亭瑄", pose.tingxuan());
            HBox cast = new HBox(pose.gap(), erhong, tingxuan);
            cast.setAlignment(Pos.BOTTOM_RIGHT);
            cast.setPadding(new Insets(0, 40, 0, 0));
            cast.setMouseTransparent(true);
            for (FigureView f : new FigureView[] {erhong, tingxuan}) {
                f.prefHeightProperty().bind(heightProperty().multiply(0.7));
                // HBox 問寬度時不給高度，剪影會照預設高度算出比較寬的框，兩個人被推向左邊撞到標題
                f.prefWidthProperty().bind(f.prefHeightProperty().multiply(Figures.UNIT_W / Figures.UNIT_H));
                f.setMaxHeight(Region.USE_PREF_SIZE);
            }
            getChildren().add(cast);
        }

        Label tape = new Label("根本沒有這樣的");
        tape.getStyleClass().addAll("name-plate", "title-tape");
        Label note = new Label("資訊科！");
        note.getStyleClass().add("title-note");
        List<Text> code = List.of(
                codeText("throw new ", "title-code-kw"), codeText("Exception", "title-code-type"), codeText("("),
                codeText("\"才……才沒有想寫 Java 呢！\"", "title-code-str"), codeText(");"));
        Region caret = new Region();
        caret.getStyleClass().add("title-caret");
        // 不用 TextFlow：前面有空字串時，它會把嵌在裡面的游標畫成一個「OBJ」方框
        HBox subtitle = new HBox();
        subtitle.getChildren().addAll(code);
        subtitle.getChildren().add(caret);
        subtitle.setAlignment(Pos.BASELINE_LEFT);
        // 膠帶右邊留了一段空白，便利貼只壓在那段上，不會蓋到字
        tape.relocate(0, 40);
        note.relocate(350, 56);
        subtitle.relocate(14, 182);
        Pane logo = new Pane(tape, subtitle, note);
        logo.setPrefSize(640, 220);
        logo.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        menu.getStyleClass().add("title-menu");
        boolean anyCleared = Arrays.stream(Route.values())
                .anyMatch(r -> game.progress.route(r) != null && !game.progress.route(r).cleared().isEmpty());
        if (game.progress.lastRoute() != null) {
            menu.getChildren().add(item("繼續", game::continueGame));
        }
        menu.getChildren().add(item("新遊戲", game::showRouteSelect));
        if (anyCleared) {
            menu.getChildren().add(item("章節", game::showChapterSelect));
        }
        menu.getChildren().addAll(
                item("筆記", () -> game.openOverlay(new HandbookView(game), null)),
                item("設定", game::openSettings),
                item("離開", game::exit));
        if (game.newVersion() != null) {
            addUpdate(game.newVersion());
        }
        // 貼在上方：放在正中間的話，副標會碰到二弘的頭
        VBox left = new VBox(48, logo, menu);
        left.setAlignment(Pos.TOP_LEFT);
        left.setPadding(new Insets(80, 0, 0, 110));
        left.setMaxWidth(Region.USE_PREF_SIZE);
        StackPane.setAlignment(left, Pos.TOP_LEFT);
        getChildren().add(left);

        Motion.rise(tape, Duration.millis(120));
        if (!Motion.isOff()) {
            // pop 只有縮放，等膠帶貼好之前便利貼要先藏起來
            note.setOpacity(0);
            PauseTransition slap = new PauseTransition(Duration.millis(300));
            slap.setOnFinished(e -> {
                note.setOpacity(1);
                Motion.pop(note);
            });
            slap.play();
        }
        typeOut(code, caret, Duration.millis(600));
        for (int i = 0; i < menu.getChildren().size(); i++) {
            Motion.rise(menu.getChildren().get(i), Duration.millis(360 + 60 * i));
        }
    }

    /** 玩完第 {@code chapter} 章時兩個人的樣子；還沒玩完第一章是 null。 */
    static Pose poseAfter(int chapter) {
        return POSES.reversed().stream().filter(p -> chapter >= p.afterChapter()).findFirst().orElse(null);
    }

    /** 兩個視角裡玩完的最後一章；都還沒玩完任何一章就是 0。 */
    private static int furthestCleared(Game game) {
        return game.story.chapters().stream()
                .filter(ch -> Arrays.stream(Route.values()).anyMatch(r -> game.progress.route(r) != null
                        && game.progress.route(r).cleared().contains(ch.id())))
                .mapToInt(Chapter::number)
                .max().orElse(0);
    }

    private static Text codeText(String text, String... kinds) {
        Text t = new Text(text);
        t.getStyleClass().add("title-code-text");
        t.getStyleClass().addAll(kinds);
        return t;
    }

    /** 副標一個字一個字打出來，游標跟在最後面，打完開始閃；動效關閉時整行直接出現，游標不閃。 */
    private static void typeOut(List<Text> parts, Region caret, Duration delay) {
        if (Motion.isOff()) {
            return;
        }
        List<String> full = parts.stream().map(Text::getText).toList();
        parts.forEach(t -> t.setText(""));
        int total = full.stream().mapToInt(String::length).sum();
        Timeline typing = new Timeline();
        for (int n = 1; n <= total; n++) {
            int shown = n;
            typing.getKeyFrames().add(new KeyFrame(PER_CHAR.multiply(n), e -> {
                int left = shown;
                for (int i = 0; i < parts.size(); i++) {
                    String s = full.get(i);
                    parts.get(i).setText(s.substring(0, Math.min(left, s.length())));
                    left = Math.max(0, left - s.length());
                }
            }));
        }
        typing.setDelay(delay);
        Timeline blink = new Timeline(
                new KeyFrame(BLINK, e -> caret.setOpacity(0)),
                new KeyFrame(BLINK.multiply(2), e -> caret.setOpacity(1)));
        blink.setCycleCount(Animation.INDEFINITE);
        typing.setOnFinished(e -> blink.play());
        // 閃爍是無限循環，不會自己停；離開標題畫面時連還沒打完的一起停掉
        caret.sceneProperty().addListener((obs, was, now) -> {
            if (now == null) {
                typing.stop();
                blink.stop();
            }
        });
        typing.play();
    }

    /** 標題畫面開著時才查到新版本。 */
    void offerUpdate(String version) {
        Motion.rise(addUpdate(version), Duration.ZERO);
    }

    private Button addUpdate(String version) {
        Button b = item("更新到 v" + version, game::update);
        menu.getChildren().add(menu.getChildren().size() - 1, b);
        return b;
    }

    private static Button item(String text, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().add("title-item");
        b.setOnAction(e -> action.run());
        Motion.hoverLift(b);
        return b;
    }
}
