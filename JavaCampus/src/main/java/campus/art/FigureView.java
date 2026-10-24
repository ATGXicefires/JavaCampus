package campus.art;

import campus.fx.Motion;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

import java.util.Optional;

/**
 * 舞台上的一個角色。寬高比固定，外層決定高度。程式繪製的剪影套上霓虹描邊；
 * 有圖檔就改用圖檔、不加描邊。表情符號另外畫在頭附近，換表情時單獨彈出來。
 */
public final class FigureView extends Region {

    private static final double SYMBOL = 0.14;

    private final Art art;
    private final String person;
    private final Canvas body = new Canvas();
    private final Canvas symbol = new Canvas();
    private final ImageView image = new ImageView();
    private String expression;
    private double drawnH;

    public FigureView(Art art, String person, String expression) {
        this.art = art;
        this.person = person;
        this.expression = expression;
        image.setPreserveRatio(true);
        image.setSmooth(true);
        getChildren().addAll(body, image, symbol);
        setMouseTransparent(true);
    }

    public String person() {
        return person;
    }

    public String expression() {
        return expression;
    }

    public void setExpression(String e) {
        if (e.equals(expression)) {
            return;
        }
        expression = e;
        drawnH = 0;
        requestLayout();
        if (Figures.hasSymbol(e)) {
            Motion.pop(symbol);
        }
    }

    @Override
    protected double computePrefWidth(double height) {
        return (height > 0 ? height : 800) * Figures.UNIT_W / Figures.UNIT_H;
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        if (h <= 0 || h == drawnH) {
            return;
        }
        drawnH = h;
        double s = h / Figures.UNIT_H;
        Optional<Image> img = art.figure(person, expression);
        if (img.isPresent()) {
            body.setVisible(false);
            image.setVisible(true);
            image.setImage(img.get());
            image.setFitHeight(h);
            double iw = img.get().getWidth() * h / img.get().getHeight();
            image.relocate((w - iw) / 2, 0);
        } else {
            image.setVisible(false);
            body.setVisible(true);
            body.setWidth(w);
            body.setHeight(h);
            Figures.paint(body.getGraphicsContext2D(), w, h, person, expression);
            body.setEffect(Figures.neon(person, h));
        }
        double size = h * SYMBOL;
        symbol.setWidth(size);
        symbol.setHeight(size);
        symbol.getGraphicsContext2D().clearRect(0, 0, size, size);
        if (Figures.hasSymbol(expression)) {
            Figures.paintSymbol(symbol.getGraphicsContext2D(), size, expression);
            symbol.setEffect(Figures.symbolGlow(expression, size));
        }
        double[] at = Figures.symbolAnchor(person, expression);
        symbol.relocate((w - Figures.UNIT_W * s) / 2 + at[0] * s - size / 2, at[1] * s - size / 2);
    }
}
