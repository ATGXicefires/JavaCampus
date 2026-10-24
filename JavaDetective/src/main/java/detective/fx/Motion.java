package detective.fx;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/** 共用的動畫效果。 */
public final class Motion {

    private Motion() {}

    /** 換畫面：舊的淡出，新的淡入並往上滑。 */
    public static void swap(StackPane host, Node next) {
        Node old = host.getChildren().isEmpty() ? null : host.getChildren().getLast();
        next.setOpacity(0);
        next.setTranslateY(18);
        host.getChildren().add(next);
        FadeTransition in = new FadeTransition(Duration.millis(260), next);
        in.setToValue(1);
        TranslateTransition up = new TranslateTransition(Duration.millis(260), next);
        up.setToY(0);
        up.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition show = new ParallelTransition(in, up);
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
        target.setText("");
        Timeline tl = new Timeline();
        for (int i = 1; i <= text.length(); i++) {
            int n = i;
            tl.getKeyFrames().add(new KeyFrame(perChar.multiply(i), e -> {
                target.setText(text.substring(0, n));
                if (onChar != null && !Character.isWhitespace(text.charAt(n - 1)) && n % 2 == 1) {
                    onChar.run();
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

    /** 印章從畫面外重重落下，落定時呼叫 {@code onImpact} 並震動 {@code shakeTarget}。 */
    public static void stamp(Node stamp, Node shakeTarget, Runnable onImpact) {
        stamp.setOpacity(0);
        stamp.setScaleX(3);
        stamp.setScaleY(3);
        stamp.setRotate(-30);
        Timeline drop = new Timeline(new KeyFrame(Duration.millis(230),
                new KeyValue(stamp.opacityProperty(), 1, Interpolator.EASE_IN),
                new KeyValue(stamp.scaleXProperty(), 1, Interpolator.EASE_IN),
                new KeyValue(stamp.scaleYProperty(), 1, Interpolator.EASE_IN),
                new KeyValue(stamp.rotateProperty(), -12, Interpolator.EASE_IN)));
        drop.setOnFinished(e -> {
            if (onImpact != null) {
                onImpact.run();
            }
            shake(shakeTarget, 9);
        });
        drop.play();
    }

    public static void shake(Node node, double amplitude) {
        Timeline tl = new Timeline();
        double[] offsets = {1, -0.8, 0.6, -0.4, 0.2, 0};
        for (int i = 0; i < offsets.length; i++) {
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(45 * (i + 1)),
                    new KeyValue(node.translateXProperty(), offsets[i] * amplitude)));
        }
        tl.play();
    }

    /** 新證物翻面進場，並短暫發光。 */
    public static void flipIn(Node node) {
        node.setScaleX(0.02);
        DropShadow glow = new DropShadow(28, Color.web("#e0a84a"));
        glow.setSpread(0.25);
        node.setEffect(glow);
        ScaleTransition flip = new ScaleTransition(Duration.millis(380), node);
        flip.setToX(1);
        flip.setInterpolator(Interpolator.EASE_OUT);
        Timeline fade = new Timeline(new KeyFrame(Duration.millis(1400), new KeyValue(glow.radiusProperty(), 0)));
        fade.setOnFinished(e -> node.setEffect(null));
        flip.play();
        fade.play();
    }

    /** 反覆放大縮小的提醒效果，停止時呼叫回傳值的 {@code stop()}。 */
    public static Animation pulse(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(520), node);
        st.setFromX(1);
        st.setFromY(1);
        st.setToX(1.07);
        st.setToY(1.07);
        st.setAutoReverse(true);
        st.setCycleCount(Animation.INDEFINITE);
        st.statusProperty().addListener((obs, was, now) -> {
            if (now == Animation.Status.STOPPED) {
                node.setScaleX(1);
                node.setScaleY(1);
            }
        });
        st.play();
        return st;
    }

    /** 畫面中央滑過一條橫幅，停一下後淡出。 */
    public static void banner(StackPane host, String text, String styleClass) {
        Label strip = new Label(text);
        strip.getStyleClass().addAll("banner", styleClass);
        strip.setMouseTransparent(true);
        strip.setTranslateX(-host.getWidth());
        host.getChildren().add(strip);
        TranslateTransition in = new TranslateTransition(Duration.millis(260), strip);
        in.setToX(0);
        in.setInterpolator(Interpolator.EASE_OUT);
        PauseTransition hold = new PauseTransition(Duration.millis(700));
        FadeTransition out = new FadeTransition(Duration.millis(300), strip);
        out.setToValue(0);
        SequentialTransition seq = new SequentialTransition(in, hold, out);
        seq.setOnFinished(e -> host.getChildren().remove(strip));
        seq.play();
    }

    /** 疊加面板出現：背景淡入，面板從小彈到原尺寸。 */
    public static void popIn(Node backdrop, Node panel) {
        backdrop.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(160), backdrop);
        fade.setToValue(1);
        panel.setScaleX(0.9);
        panel.setScaleY(0.9);
        ScaleTransition grow = new ScaleTransition(Duration.millis(220), panel);
        grow.setToX(1);
        grow.setToY(1);
        grow.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(fade, grow).play();
    }

    /** 延遲 {@code delay} 後淡入並從下方滑上來，最後回到呼叫前的透明度。 */
    public static void rise(Node node, Duration delay) {
        double target = node.getOpacity();
        node.setOpacity(0);
        node.setTranslateY(14);
        FadeTransition f = new FadeTransition(Duration.millis(320), node);
        f.setToValue(target);
        TranslateTransition t = new TranslateTransition(Duration.millis(320), node);
        t.setToY(0);
        ParallelTransition p = new ParallelTransition(f, t);
        p.setDelay(delay);
        p.play();
    }
}
