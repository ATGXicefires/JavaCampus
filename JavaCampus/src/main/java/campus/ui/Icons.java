package campus.ui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;

/** 選完之後飄出來的小圖示：愛心是 AP，打開的小鎖頭是 TP。 */
final class Icons {

    private static final Color AP = Color.web("#ef6f8a");
    private static final Color TP = Color.web("#e3b341");

    private Icons() {}

    static Node heart(double size) {
        SVGPath p = new SVGPath();
        p.setContent("M12 21 C5 16 1 12 1 7.5 C1 4 3.8 1.5 7 1.5 C9.2 1.5 11 2.8 12 4.5 C13 2.8 14.8 1.5 17 1.5 "
                + "C20.2 1.5 23 4 23 7.5 C23 12 19 16 12 21 Z");
        p.setFill(AP);
        p.setStroke(Color.web("#0c0b10"));
        p.setStrokeWidth(1.4);
        double k = size / 24;
        p.setScaleX(k);
        p.setScaleY(k);
        Group g = new Group(p);
        g.setEffect(new DropShadow(size * 0.4, AP));
        return g;
    }

    static Node lock(double size) {
        double k = size / 24;
        Rectangle body = new Rectangle(3 * k, 10 * k, 18 * k, 13 * k);
        body.setFill(TP);
        body.setStroke(Color.web("#0c0b10"));
        body.setStrokeWidth(1.4 * k);
        Arc shackle = new Arc(9 * k, 9 * k, 5.5 * k, 6.5 * k, 0, 180);
        shackle.setType(ArcType.OPEN);
        shackle.setFill(null);
        shackle.setStroke(TP);
        shackle.setStrokeWidth(3 * k);
        shackle.setStrokeLineCap(StrokeLineCap.ROUND);
        Rectangle hole = new Rectangle(11 * k, 14.5 * k, 2 * k, 4.5 * k);
        hole.setFill(Color.web("#0c0b10"));
        Group g = new Group(shackle, body, hole);
        g.setEffect(new DropShadow(size * 0.4, TP));
        return g;
    }
}
