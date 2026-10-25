package campus.ui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import java.util.Arrays;

/**
 * 從照片下端往上掀、整張翻過上端的紙，像翻一頁便條本。只會往上翻；
 * 要從一角往斜對角撕，就交進轉成那一角朝下的照片，整張再轉回去。
 * 照片切成一條條橫條，每條照紙彎起來之後的位置用透視變形貼上去；
 * {@link #progressProperty()} 從 0（平貼）到 1（整張翻到上端上面）。
 */
final class PeelingNote extends Group {

    /** 條數太少的話，彎的地方看得出一節一節的折角和明暗。 */
    private static final int STRIPS = 48;
    /** 透視變形會把每條的上下緣各糊掉約一像素，每條要往下多疊幾列像素，接縫才不會透出細線。 */
    private static final int SEAM = 3;
    /** 彎折處的半徑，以紙長為單位。 */
    private static final double BEND = 0.17;
    /** 眼睛離紙面的距離，以紙長為單位；越近，翻起來朝向畫面的那頭放大得越多。 */
    private static final double EYE = 3;
    /** 眼睛在上端下方多遠，以紙長為單位。從正對著看，立起來的紙只剩一條線；從下面一點看，才看得到它翻起來。 */
    private static final double EYE_Y = 2;
    /** 紙面越斜越暗，側對眼睛時暗到這個程度。 */
    private static final double SHADE = 0.25;
    /** 背面比正面再暗一點。 */
    private static final double BACK_SHADE = 0.05;

    private final DoubleProperty progress = new SimpleDoubleProperty(this, "progress");
    private final double width;
    private final double paper;
    private final double radius;
    private final double eye;
    private final double[] tops = new double[STRIPS];
    private final double[] bottoms = new double[STRIPS];
    private final Image[] fronts = new Image[STRIPS];
    private final Image[] backs = new Image[STRIPS];
    private final ImageView[] strips = new ImageView[STRIPS];
    private final PerspectiveTransform[] warps = new PerspectiveTransform[STRIPS];
    private final ColorAdjust[] shades = new ColorAdjust[STRIPS];

    /**
     * @param front 正面的照片
     * @param back  同樣大小、沒有字的照片，翻過來時看到的那面
     */
    PeelingNote(Image front, Image back, double width, double height) {
        this.width = width;
        paper = height;
        radius = paper * BEND;
        eye = paper * EYE;
        int cols = (int) front.getWidth();
        int rows = (int) front.getHeight();
        double sy = rows / height;
        int[] frontPixels = pixels(front);
        int[] backPixels = pixels(back);
        for (int i = 0; i < STRIPS; i++) {
            int top = rows * i / STRIPS;
            int own = rows * (i + 1) / STRIPS;
            int bottom = Math.min(rows, own + SEAM);
            tops[i] = top / sy;
            bottoms[i] = bottom / sy;
            fronts[i] = tile(frontPixels, cols, top, own, bottom);
            backs[i] = tile(backPixels, cols, top, own, bottom);
            ImageView v = new ImageView(fronts[i]);
            v.setFitWidth(width);
            v.setFitHeight(bottoms[i] - tops[i]);
            shades[i] = new ColorAdjust();
            warps[i] = new PerspectiveTransform();
            warps[i].setInput(shades[i]);
            v.setEffect(warps[i]);
            strips[i] = v;
            getChildren().add(v);
        }
        progress.addListener((obs, was, now) -> bendTo(now.doubleValue()));
        bendTo(0);
    }

    DoubleProperty progressProperty() {
        return progress;
    }

    private static int[] pixels(Image image) {
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();
        int[] px = new int[w * h];
        image.getPixelReader().getPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), px, 0, w);
        return px;
    }

    /** 第 {@code top} 到 {@code bottom} 列；{@code own} 以下疊到下一條的部分只留不透明的像素，半透明的膠帶疊兩層會變深，透出一道道斜紋。 */
    private static Image tile(int[] px, int cols, int top, int own, int bottom) {
        int[] part = Arrays.copyOfRange(px, top * cols, bottom * cols);
        for (int i = (own - top) * cols; i < part.length; i++) {
            if (part[i] >>> 24 != 0xFF) {
                part[i] = 0;
            }
        }
        WritableImage t = new WritableImage(cols, bottom - top);
        t.getPixelWriter().setPixels(0, 0, cols, bottom - top, PixelFormat.getIntArgbInstance(), part, 0, cols);
        return t;
    }

    private void bendTo(double t) {
        double fold = paper * (1 - t);
        double turn = Math.PI * t;
        for (int i = 0; i < STRIPS; i++) {
            double[] a = project(tops[i], fold, turn);
            double[] b = project(bottoms[i], fold, turn);
            // 投影後下緣跑到上緣上面，就是翻過來看到背面了；透視變形會順便把圖上下鏡射
            boolean flipped = b[1] < a[1];
            strips[i].setImage(flipped ? backs[i] : fronts[i]);
            PerspectiveTransform w = warps[i];
            w.setUlx(a[0]);
            w.setUly(a[1]);
            w.setUrx(width - a[0]);
            w.setUry(a[1]);
            w.setLlx(b[0]);
            w.setLly(b[1]);
            w.setLrx(width - b[0]);
            w.setLry(b[1]);
            double tilt = tilt((tops[i] + bottoms[i]) / 2, fold, turn);
            shades[i].setBrightness(-SHADE * (1 - Math.abs(Math.cos(tilt))) - (flipped ? BACK_SHADE : 0));
        }
    }

    /** 紙上離上端 {@code s} 的點：{@code fold} 以上平貼，接著繞半徑 {@link #radius} 彎過 {@code turn}，剩下的直直伸出去。 */
    private double tilt(double s, double fold, double turn) {
        if (s <= fold) {
            return 0;
        }
        return Math.min((s - fold) / radius, turn);
    }

    /** 那一點投影到畫面上的左緣 x 和 y；右緣和左緣對稱。 */
    private double[] project(double s, double fold, double turn) {
        double arc = radius * turn;
        double y;
        double z;
        if (s <= fold) {
            y = s;
            z = 0;
        } else if (s <= fold + arc) {
            double a = (s - fold) / radius;
            y = fold + radius * Math.sin(a);
            z = radius * (1 - Math.cos(a));
        } else {
            double d = s - fold - arc;
            y = fold + radius * Math.sin(turn) + d * Math.cos(turn);
            z = radius * (1 - Math.cos(turn)) + d * Math.sin(turn);
        }
        double k = eye / (eye - z);
        double level = paper * EYE_Y;
        return new double[] {width / 2 * (1 - k), level + (y - level) * k};
    }
}
