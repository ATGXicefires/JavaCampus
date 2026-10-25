package campus.fx;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.function.IntConsumer;

/** 共用的動畫效果。強度由玩家設定：Q 彈、一般、關閉。 */
public final class Motion {

    public enum Level { BOUNCY, PLAIN, OFF }

    /** 阻尼彈簧：衝過頭約一成，再晃一下回穩。 */
    static final Interpolator SPRING = new Interpolator() {
        @Override
        protected double curve(double t) {
            // 公式在 t = 1 時只是「非常接近」1，結尾要剛好停在目標值
            return t >= 1 ? 1 : 1 - Math.exp(-6.9 * t) * Math.cos(3 * Math.PI * t);
        }
    };

    /** 先衝過頭一點再收回，不來回晃。 */
    static final Interpolator BACK_OUT = new Interpolator() {
        @Override
        protected double curve(double t) {
            double c1 = 1.70158;
            double u = t - 1;
            return 1 + (c1 + 1) * u * u * u + c1 * u * u;
        }
    };

    private static final String SCALE_KEY = "motion.scale";
    private static final String LIFT_KEY = "motion.lift";

    private static Level level = Level.BOUNCY;

    private Motion() {}

    public static void setLevel(Level l) {
        level = l;
    }

    private static boolean off() {
        return level == Level.OFF;
    }

    private static Interpolator spring() {
        return level == Level.BOUNCY ? SPRING : Interpolator.EASE_OUT;
    }

    private static Interpolator back() {
        return level == Level.BOUNCY ? BACK_OUT : Interpolator.EASE_OUT;
    }

    /** 彈簧要多一點時間才晃得完，一般模式就短一點。 */
    private static Duration springTime(double bouncyMs, double plainMs) {
        return Duration.millis(level == Level.BOUNCY ? bouncyMs : plainMs);
    }

    /** 換畫面：舊的淡出，新的從下方彈上來。 */
    public static void swap(StackPane host, Node next) {
        Node old = host.getChildren().isEmpty() ? null : host.getChildren().getLast();
        if (off()) {
            if (old != null) {
                host.getChildren().remove(old);
            }
            host.getChildren().add(next);
            return;
        }
        next.setOpacity(0);
        next.setTranslateY(28);
        next.setScaleX(0.97);
        next.setScaleY(0.97);
        host.getChildren().add(next);
        Duration t = springTime(560, 260);
        Timeline show = new Timeline(
                new KeyFrame(Duration.millis(220), new KeyValue(next.opacityProperty(), 1)),
                new KeyFrame(t,
                        new KeyValue(next.translateYProperty(), 0, spring()),
                        new KeyValue(next.scaleXProperty(), 1, spring()),
                        new KeyValue(next.scaleYProperty(), 1, spring())));
        if (old == null) {
            show.play();
            return;
        }
        FadeTransition out = new FadeTransition(Duration.millis(140), old);
        out.setToValue(0);
        out.setOnFinished(e -> host.getChildren().remove(old));
        new SequentialTransition(out, show).play();
    }

    /**
     * 打字機效果：一次顯示一個字。
     *
     * @param onChar 每顯示一個非空白字元時呼叫，用來播放打字音
     * @return 呼叫 {@code jumpTo("end")} 可以直接顯示全文
     */
    public static Animation typewriter(Label target, String text, Duration perChar, Runnable onChar, Runnable onDone) {
        return typewriter(target, text, perChar, n -> {
            if (onChar != null && n % 2 == 1) {
                onChar.run();
            }
        }, onDone);
    }

    /** 同上，{@code onChar} 拿到的是第幾個非空白字元（從 1 起算），讓呼叫的人自己決定隔幾個字出聲。 */
    public static Animation typewriter(Label target, String text, Duration perChar, IntConsumer onChar, Runnable onDone) {
        target.setText("");
        Timeline tl = new Timeline();
        int[] visible = {0};
        for (int i = 1; i <= text.length(); i++) {
            int n = i;
            tl.getKeyFrames().add(new KeyFrame(perChar.multiply(i), e -> {
                target.setText(text.substring(0, n));
                if (!Character.isWhitespace(text.charAt(n - 1))) {
                    onChar.accept(++visible[0]);
                }
            }));
        }
        tl.setOnFinished(e -> {
            target.setText(text);
            if (onDone != null) {
                onDone.run();
            }
        });
        tl.play();
        return tl;
    }

    public static void shake(Node node, double amplitude) {
        if (off()) {
            return;
        }
        Timeline tl = new Timeline();
        double[] offsets = {1, -0.8, 0.6, -0.4, 0.2, 0};
        for (int i = 0; i < offsets.length; i++) {
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(45 * (i + 1)),
                    new KeyValue(node.translateXProperty(), offsets[i] * amplitude)));
        }
        tl.play();
    }

    /** 每隔一下彈一次的提醒效果，停止時呼叫回傳值的 {@code stop()}。 */
    public static Animation pulse(Node node) {
        Timeline tl = new Timeline();
        if (!off()) {
            tl.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO, new KeyValue(node.scaleXProperty(), 1), new KeyValue(node.scaleYProperty(), 1)),
                    new KeyFrame(Duration.millis(140),
                            new KeyValue(node.scaleXProperty(), 1.12, Interpolator.EASE_OUT),
                            new KeyValue(node.scaleYProperty(), 1.12, Interpolator.EASE_OUT)),
                    new KeyFrame(Duration.millis(700),
                            new KeyValue(node.scaleXProperty(), 1, spring()),
                            new KeyValue(node.scaleYProperty(), 1, spring())),
                    new KeyFrame(Duration.millis(1200)));
            tl.setCycleCount(Animation.INDEFINITE);
        }
        tl.statusProperty().addListener((obs, was, now) -> {
            if (now == Animation.Status.STOPPED) {
                node.setScaleX(1);
                node.setScaleY(1);
            }
        });
        tl.play();
        return tl;
    }

    /** 角色登場：從下方彈上來。 */
    public static void enter(Node node) {
        if (off()) {
            node.setOpacity(1);
            return;
        }
        node.setOpacity(0);
        node.setTranslateY(level == Level.BOUNCY ? 60 : 20);
        new Timeline(
                new KeyFrame(Duration.millis(220), new KeyValue(node.opacityProperty(), 1)),
                new KeyFrame(springTime(620, 280), new KeyValue(node.translateYProperty(), 0, spring()))).play();
    }

    /** 角色退場：往下沉並淡出，結束時呼叫 {@code onDone}。 */
    public static void exit(Node node, Runnable onDone) {
        if (off()) {
            onDone.run();
            return;
        }
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(240),
                new KeyValue(node.opacityProperty(), 0, Interpolator.EASE_IN),
                new KeyValue(node.translateYProperty(), 30, Interpolator.EASE_IN)));
        tl.setOnFinished(e -> onDone.run());
        tl.play();
    }

    /** 換位置：節點已經排到新位置，先往回偏移 {@code fromX}，再彈回 0，看起來就是滑過去的。 */
    public static void slideFrom(Node node, double fromX) {
        if (off()) {
            node.setTranslateX(0);
            return;
        }
        node.setTranslateX(fromX);
        new Timeline(new KeyFrame(springTime(560, 260), new KeyValue(node.translateXProperty(), 0, spring()))).play();
    }

    /** 說話時輕輕跳一下。 */
    public static void bob(Node node) {
        if (off()) {
            return;
        }
        double h = level == Level.BOUNCY ? -8 : -4;
        new Timeline(
                new KeyFrame(Duration.millis(90), new KeyValue(node.translateYProperty(), h, Interpolator.EASE_OUT)),
                new KeyFrame(springTime(420, 200), new KeyValue(node.translateYProperty(), 0, spring()))).play();
    }

    /** 畫面白光一閃。一般模式比較暗，關閉時不閃。 */
    public static void flash(Node white) {
        if (off()) {
            return;
        }
        white.setOpacity(level == Level.BOUNCY ? 0.85 : 0.45);
        new Timeline(new KeyFrame(Duration.millis(level == Level.BOUNCY ? 420 : 260),
                new KeyValue(white.opacityProperty(), 0, Interpolator.EASE_OUT))).play();
    }

    /** 心跳：畫面邊緣的暈影脈動兩下（一般模式一下）。 */
    public static void heartbeat(Node vignette) {
        if (off()) {
            return;
        }
        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(vignette.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(120), new KeyValue(vignette.opacityProperty(), 0.9, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(320), new KeyValue(vignette.opacityProperty(), 0.25, Interpolator.EASE_IN)));
        if (level == Level.BOUNCY) {
            tl.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(420), new KeyValue(vignette.opacityProperty(), 0.75, Interpolator.EASE_OUT)),
                    new KeyFrame(Duration.millis(1100), new KeyValue(vignette.opacityProperty(), 0, Interpolator.EASE_IN)));
        } else {
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(700), new KeyValue(vignette.opacityProperty(), 0)));
        }
        tl.play();
    }

    /** 小圖示彈出後往上飄走，結束時呼叫 {@code onDone}（用來移除節點）。 */
    public static void floatAway(Node node, Runnable onDone) {
        Duration life = Duration.millis(off() ? 500 : 1300);
        Timeline tl = new Timeline();
        if (!off()) {
            node.setScaleX(0.2);
            node.setScaleY(0.2);
            tl.getKeyFrames().addAll(
                    new KeyFrame(springTime(420, 200),
                            new KeyValue(node.scaleXProperty(), 1, spring()),
                            new KeyValue(node.scaleYProperty(), 1, spring())),
                    new KeyFrame(life, new KeyValue(node.translateYProperty(), -90, Interpolator.EASE_IN)));
        }
        tl.getKeyFrames().addAll(
                new KeyFrame(life.multiply(0.6), new KeyValue(node.opacityProperty(), 1)),
                new KeyFrame(life, new KeyValue(node.opacityProperty(), 0)));
        tl.setOnFinished(e -> onDone.run());
        tl.play();
    }

    /** 淡入或淡出到 {@code to}；動效關閉時直接設定。 */
    public static void fade(Node node, double to, double ms, Runnable onDone) {
        if (off() || ms <= 0) {
            node.setOpacity(to);
            if (onDone != null) {
                onDone.run();
            }
            return;
        }
        FadeTransition f = new FadeTransition(Duration.millis(ms), node);
        f.setToValue(to);
        if (onDone != null) {
            f.setOnFinished(e -> onDone.run());
        }
        f.play();
    }

    /** 進度條之類的寬度從目前長到 {@code width}。 */
    public static void grow(Region bar, double width, Duration delay) {
        if (off()) {
            bar.setPrefWidth(width);
            return;
        }
        Timeline tl = new Timeline(new KeyFrame(springTime(1200, 700), new KeyValue(bar.prefWidthProperty(), width, spring())));
        tl.setDelay(delay);
        tl.play();
    }

    public static boolean isOff() {
        return off();
    }

    /** 延遲 {@code delay} 後淡入並從下方彈上來，最後回到呼叫前的透明度。 */
    public static void rise(Node node, Duration delay) {
        if (off()) {
            return;
        }
        double target = node.getOpacity();
        node.setOpacity(0);
        node.setTranslateY(18);
        node.setScaleX(0.94);
        node.setScaleY(0.94);
        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(260), new KeyValue(node.opacityProperty(), target)),
                new KeyFrame(springTime(560, 320),
                        new KeyValue(node.translateYProperty(), 0, spring()),
                        new KeyValue(node.scaleXProperty(), 1, spring()),
                        new KeyValue(node.scaleYProperty(), 1, spring())));
        tl.setDelay(delay);
        tl.play();
    }

    /** 小東西從小彈到原尺寸，例如剛打勾的步驟。 */
    public static void pop(Node node) {
        if (off()) {
            return;
        }
        node.setScaleX(0.3);
        node.setScaleY(0.3);
        scaleTo(node, 1, springTime(560, 220), spring());
    }

    /** 疊加面板出現：背景淡入，面板從小彈到原尺寸。 */
    public static void popIn(Node backdrop, Node panel) {
        if (off()) {
            return;
        }
        backdrop.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(160), backdrop);
        fade.setToValue(1);
        fade.play();
        panel.setScaleX(0.9);
        panel.setScaleY(0.9);
        scaleTo(panel, 1, springTime(520, 220), spring());
    }

    /** 抽屜水平滑動：打開時彈一下，收起時加速離開。 */
    public static void slideX(Node node, double toX, boolean opening, Runnable onFinished) {
        if (off()) {
            node.setTranslateX(toX);
            onFinished.run();
            return;
        }
        Duration t = opening ? springTime(520, 260) : Duration.millis(220);
        Interpolator curve = opening ? spring() : Interpolator.EASE_IN;
        Timeline tl = new Timeline(new KeyFrame(t, new KeyValue(node.translateXProperty(), toX, curve)));
        tl.setOnFinished(e -> onFinished.run());
        tl.play();
    }

    /** 按下按鈕或標了 {@code pressable} 的節點時先壓扁，放開時彈回。 */
    public static void installPress(Scene scene) {
        Node[] pressed = {null};
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() != MouseButton.PRIMARY || off()) {
                return;
            }
            Node n = pressable(e.getPickResult().getIntersectedNode());
            if (n != null) {
                pressed[0] = n;
                scaleTo(n, 0.93, Duration.millis(80), Interpolator.EASE_OUT);
            }
        });
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            Node n = pressed[0];
            if (n != null) {
                pressed[0] = null;
                scaleTo(n, 1, springTime(480, 160), spring());
            }
        });
        // 按著拖曳是在選卡片裡的字，不是按卡片；先彈回來，免得選字的時候整張卡片縮著
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            Node n = pressed[0];
            if (n != null && !e.isStillSincePress()) {
                pressed[0] = null;
                scaleTo(n, 1, springTime(480, 160), spring());
            }
        });
    }

    private static Node pressable(Node n) {
        while (n != null) {
            if (n instanceof ButtonBase b) {
                return b.isDisabled() ? null : b;
            }
            if (n.getStyleClass().contains("pressable")) {
                return n;
            }
            n = n.getParent();
        }
        return null;
    }

    /** 滑鼠移上去時浮起來，移開時落回原位。 */
    public static void hoverLift(Node node) {
        node.setOnMouseEntered(e -> lift(node, -6));
        node.setOnMouseExited(e -> lift(node, 0));
    }

    private static void lift(Node node, double y) {
        if (off()) {
            return;
        }
        if (node.getProperties().get(LIFT_KEY) instanceof Animation running) {
            running.stop();
        }
        Timeline tl = new Timeline(new KeyFrame(springTime(480, 180), new KeyValue(node.translateYProperty(), y, spring())));
        node.getProperties().put(LIFT_KEY, tl);
        tl.play();
    }

    /** 同一個節點的縮放動畫一次只跑一個，新的會接手舊的。 */
    private static void scaleTo(Node node, double s, Duration t, Interpolator curve) {
        if (node.getProperties().get(SCALE_KEY) instanceof Animation running) {
            running.stop();
        }
        Timeline tl = new Timeline(new KeyFrame(t,
                new KeyValue(node.scaleXProperty(), s, curve),
                new KeyValue(node.scaleYProperty(), s, curve)));
        node.getProperties().put(SCALE_KEY, tl);
        tl.play();
    }
}
