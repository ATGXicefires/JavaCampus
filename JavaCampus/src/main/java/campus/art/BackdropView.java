package campus.art;

import campus.fx.RainLayer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

import java.util.Objects;
import java.util.Optional;

/** 一張背景：有圖檔就等比例填滿畫面，沒有就用 {@link Backgrounds} 依實際大小重畫。 */
public final class BackdropView extends Region {

    private final Canvas canvas = new Canvas();
    private final ImageView image = new ImageView();
    private final Rectangle clip = new Rectangle();
    private final String name;
    private final String variant;
    private final boolean fromImage;
    private RainLayer rain;
    private double drawnW;
    private double drawnH;

    public BackdropView(Art art, String name, String variant) {
        this.name = name;
        this.variant = variant;
        Optional<Image> img = art.background(name, variant);
        fromImage = img.isPresent();
        if (fromImage) {
            image.setImage(img.get());
            image.setSmooth(true);
            getChildren().add(image);
        } else {
            getChildren().add(canvas);
        }
        setClip(clip);
        setMouseTransparent(true);
    }

    public String name() {
        return name;
    }

    public String variant() {
        return variant;
    }

    public boolean sameAs(String otherName, String otherVariant) {
        return name.equals(otherName) && Objects.equals(variant, otherVariant);
    }

    /** 程式繪製的背景有窗戶的話，雨只落在窗外。 */
    public void setRain(boolean on) {
        if (on == (rain != null)) {
            return;
        }
        if (on) {
            rain = new RainLayer();
            getChildren().add(rain);
        } else {
            getChildren().remove(rain);
            rain = null;
        }
        requestLayout();
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        clip.setWidth(w);
        clip.setHeight(h);
        if (fromImage) {
            Image img = image.getImage();
            double s = Math.max(w / img.getWidth(), h / img.getHeight());
            image.setFitWidth(img.getWidth() * s);
            image.setFitHeight(img.getHeight() * s);
            image.relocate((w - img.getWidth() * s) / 2, (h - img.getHeight() * s) / 2);
        } else if (w != drawnW || h != drawnH) {
            canvas.setWidth(w);
            canvas.setHeight(h);
            Backgrounds.paint(canvas.getGraphicsContext2D(), w, h, name, variant);
            drawnW = w;
            drawnH = h;
        }
        if (rain != null) {
            Rectangle2D win = fromImage ? null : Backgrounds.window(name);
            if (win == null) {
                rain.resizeRelocate(0, 0, w, h);
                rain.setClip(null);
            } else {
                rain.resizeRelocate(win.getMinX() * w, win.getMinY() * h, win.getWidth() * w, win.getHeight() * h);
                rain.setClip(new Rectangle(win.getWidth() * w, win.getHeight() * h));
            }
        }
    }
}
