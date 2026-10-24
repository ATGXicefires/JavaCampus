package detective.fx;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.Random;

/** 窗外的雨。只在畫面上時才跑動畫，離開畫面就停。 */
public final class RainLayer extends Pane {

    private static final int DROPS = 160;

    private final Canvas canvas = new Canvas();
    private final double[] x = new double[DROPS];
    private final double[] y = new double[DROPS];
    private final double[] speed = new double[DROPS];
    private final double[] length = new double[DROPS];
    private final Random rnd = new Random();
    private long last;

    private final AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            double dt = last == 0 ? 0 : (now - last) / 1e9;
            last = now;
            step(dt);
        }
    };

    public RainLayer() {
        getChildren().add(canvas);
        setMouseTransparent(true);
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        for (int i = 0; i < DROPS; i++) {
            respawn(i, true);
        }
        sceneProperty().addListener((obs, was, now) -> {
            if (now != null) {
                last = 0;
                timer.start();
            } else {
                timer.stop();
            }
        });
    }

    private void respawn(int i, boolean anywhere) {
        double w = Math.max(getWidth(), 1280);
        double h = Math.max(getHeight(), 800);
        x[i] = rnd.nextDouble() * (w + 200) - 100;
        y[i] = anywhere ? rnd.nextDouble() * h : -rnd.nextDouble() * 100;
        speed[i] = 500 + rnd.nextDouble() * 500;
        length[i] = 10 + rnd.nextDouble() * 18;
    }

    private void step(double dt) {
        double h = getHeight();
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setLineWidth(1);
        for (int i = 0; i < DROPS; i++) {
            y[i] += speed[i] * dt;
            x[i] += speed[i] * dt * 0.12;
            if (y[i] > h + 30) {
                respawn(i, false);
            }
            double alpha = 0.10 + (speed[i] - 500) / 500 * 0.18;
            g.setStroke(Color.rgb(170, 190, 215, alpha));
            g.strokeLine(x[i], y[i], x[i] - length[i] * 0.12, y[i] - length[i]);
        }
    }
}
