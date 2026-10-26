package campus.ui;

import campus.art.BackdropView;
import campus.art.FigureView;
import campus.art.Figures;
import campus.fx.Motion;
import campus.story.Chapter;
import campus.story.Route;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
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

    private static final Duration PER_CHAR = Duration.millis(50);
    /** 刪節號一個一個慢慢打，像說到一半吞吞吐吐。 */
    private static final Duration PER_DOT = Duration.millis(260);
    private static final Duration BLINK = Duration.millis(530);
    /** 驚嘆號隔多久跳一下。 */
    private static final Duration HOP_EVERY = Duration.millis(3200);

    private final Game game;
    private final VBox menu = new VBox(6);
    /** 不會自己停的動畫，離開標題畫面時一起停掉。 */
    private final List<Animation> loops = new ArrayList<>();

    TitleView(Game game) {
        this.game = game;
        getStyleClass().add("title-screen");
        BackdropView bg = new BackdropView(game.art, "電腦教室", "傍晚");
        Region shade = new Region();
        shade.getStyleClass().add("title-shade");
        getChildren().addAll(bg, shade);

        Pose pose = poseAfter(furthestCleared(game));
        HBox cast = pose == null ? null : cast(pose);
        if (cast != null) {
            getChildren().add(cast);
        }

        Label tape = new Label("根本沒有這樣的");
        tape.getStyleClass().addAll("name-plate", "title-tape");
        // 膠帶的角度寫在樣式表裡，程式設的會被蓋掉；出場要轉的話轉外面這一層
        Group tapeHolder = new Group(tape);
        Label note = new Label("資訊科");
        note.getStyleClass().add("title-note");
        // 全形「！」的右半邊是空的，放在便利貼裡右邊會空一截；拿出來放大斜放，壓在便利貼的右緣
        Text bang = new Text("！");
        bang.getStyleClass().add("title-bang");
        // 從上緣算位置：預設從基線算的話，要等樣式表套上字級才知道基線在哪
        bang.setTextOrigin(VPos.TOP);
        bang.setRotate(14);
        List<Text> code = List.of(
                codeText("throw new ", "title-code-kw"), codeText("Exception", "title-code-type"), codeText("("),
                codeText("\"才……才沒有想寫 Java 呢！\"", "title-code-str"), codeText(");"));
        Region caret = new Region();
        caret.getStyleClass().add("title-caret");
        // 不用 TextFlow：前面有空字串時，它會把嵌在裡面的游標畫成一個「OBJ」方框
        HBox subtitle = new HBox();
        subtitle.getStyleClass().add("title-code");
        subtitle.getChildren().addAll(code);
        subtitle.getChildren().add(caret);
        subtitle.setAlignment(Pos.BASELINE_LEFT);
        // 便利貼的左緣剛好在「的」後面，膠帶從它後面穿過去，鋸齒狀的尾端從便利貼上緣探出一小截；
        // 副標往右錯開，不跟膠帶的左端對齊，膠帶往右上斜，錯開後也能往上靠
        tape.relocate(0, 20);
        note.relocate(342, 25);
        bang.relocate(514, -22);
        subtitle.relocate(66, 140);
        Pane logo = new Pane(tapeHolder, subtitle, note, bang);
        logo.setPrefSize(610, 184);
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
                item("不想看劇情，只想當爆肝工程師", game::showPractice),
                item("筆記", () -> game.openOverlay(new HandbookView(game), null)),
                item("設定", game::openSettings),
                item("離開", game::exit));
        if (game.newVersion() != null) {
            addUpdate(game.newVersion());
        }
        // 貼在上方：放在正中間的話，副標會碰到二弘的頭
        VBox left = new VBox(48, logo, menu);
        left.setAlignment(Pos.TOP_LEFT);
        left.setPadding(new Insets(56, 0, 0, 110));
        left.setMaxWidth(Region.USE_PREF_SIZE);
        StackPane.setAlignment(left, Pos.TOP_LEFT);
        getChildren().add(left);

        if (!Motion.isOff()) {
            // 出場：兩個人浮上來 → 膠帶甩上去 → 便利貼拍上去 → 驚嘆號彈出來 → 副標打字，打完游標閃、驚嘆號不時跳一下。
            // 位移用 back 不用彈簧：彈簧在頭六分之一的時間就跑完大半段，東西看起來是一下子跳到定位的
            if (cast != null) {
                glideUp(cast, Duration.millis(150));
            }
            slapOn(tapeHolder, Duration.millis(700));
            slamDown(note, logo, Duration.millis(1300));
            popOut(bang, Duration.millis(1700));
            Motion.rise(subtitle, Duration.millis(1900));
            typeOut(code, caret, Duration.millis(2100));
            Timeline hop = new Timeline(new KeyFrame(HOP_EVERY, e -> Motion.bob(bang)));
            hop.setCycleCount(Animation.INDEFINITE);
            hop.setDelay(Duration.millis(2400));
            loops.add(hop);
            hop.play();
        }
        for (int i = 0; i < menu.getChildren().size(); i++) {
            Motion.rise(menu.getChildren().get(i), Duration.millis(1500 + 100 * i));
        }
        sceneProperty().addListener((obs, was, now) -> {
            if (now == null) {
                loops.forEach(Animation::stop);
            }
        });
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

    /** 右下角的兩個人。 */
    private HBox cast(Pose pose) {
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
        return cast;
    }

    private static Text codeText(String text, String... kinds) {
        Text t = new Text(text);
        t.getStyleClass().add("title-code-text");
        t.getStyleClass().addAll(kinds);
        return t;
    }

    /** 兩個人跟著換場從下面浮上來；剪影要維持純黑，不另外淡入，免得半透明的時候透出後面的教室。 */
    private static void glideUp(Node cast, Duration delay) {
        cast.setTranslateY(120);
        Timeline tl = new Timeline(
                new KeyFrame(Motion.springTime(1000, 700), new KeyValue(cast.translateYProperty(), 0, Motion.back())));
        tl.setDelay(delay);
        tl.play();
    }

    /** 膠帶從左邊甩進來，多轉了一點，再回到原本的角度貼好。 */
    private static void slapOn(Node tape, Duration delay) {
        tape.setOpacity(0);
        tape.setTranslateX(-140);
        tape.setRotate(-8);
        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(200), new KeyValue(tape.opacityProperty(), 1)),
                new KeyFrame(Motion.springTime(900, 600),
                        new KeyValue(tape.translateXProperty(), 0, Motion.back()),
                        new KeyValue(tape.rotateProperty(), 0, Motion.back())));
        tl.setDelay(delay);
        tl.play();
    }

    /** 便利貼從前面拍上來：放大的樣子落下，撞到時縮一點再彈回原尺寸，整個作品名跟著震一下。 */
    private static void slamDown(Node note, Node logo, Duration delay) {
        note.setOpacity(0);
        note.setScaleX(1.8);
        note.setScaleY(1.8);
        Duration hit = Duration.millis(220);
        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(80), new KeyValue(note.opacityProperty(), 1)),
                new KeyFrame(hit, e -> Motion.shake(logo, 6),
                        new KeyValue(note.scaleXProperty(), 0.92, Interpolator.EASE_IN),
                        new KeyValue(note.scaleYProperty(), 0.92, Interpolator.EASE_IN)),
                new KeyFrame(hit.add(Motion.springTime(600, 260)),
                        new KeyValue(note.scaleXProperty(), 1, Motion.spring()),
                        new KeyValue(note.scaleYProperty(), 1, Motion.spring())));
        tl.setDelay(delay);
        tl.play();
    }

    /** 驚嘆號轉著彈出來，停在斜放的角度。 */
    private static void popOut(Node bang, Duration delay) {
        double angle = bang.getRotate();
        bang.setScaleX(0);
        bang.setScaleY(0);
        bang.setRotate(angle - 45);
        Timeline tl = new Timeline(new KeyFrame(Motion.springTime(760, 360),
                new KeyValue(bang.scaleXProperty(), 1, Motion.back()),
                new KeyValue(bang.scaleYProperty(), 1, Motion.back()),
                new KeyValue(bang.rotateProperty(), angle, Motion.back())));
        tl.setDelay(delay);
        tl.play();
    }

    /** 副標一個字一個字打出來，游標跟在最後面，打完開始閃。 */
    private void typeOut(List<Text> parts, Region caret, Duration delay) {
        List<String> full = parts.stream().map(Text::getText).toList();
        String all = String.join("", full);
        parts.forEach(t -> t.setText(""));
        Timeline typing = new Timeline();
        Duration at = Duration.ZERO;
        for (int n = 1; n <= all.length(); n++) {
            at = at.add(all.charAt(n - 1) == '…' ? PER_DOT : PER_CHAR);
            int shown = n;
            typing.getKeyFrames().add(new KeyFrame(at, e -> {
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
        // 還沒打完就離開的話，打字也要停，不然打完會接著開始閃
        loops.add(typing);
        loops.add(blink);
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
