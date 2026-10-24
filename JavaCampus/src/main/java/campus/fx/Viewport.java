package campus.fx;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;

/**
 * 畫面一律用固定的設計尺寸排版，再等比例縮放到視窗大小，
 * 所以 1K 到 2K、16:9 到 16:10、有沒有開 Windows 縮放，看起來都一樣。
 */
public final class Viewport extends Region {

    private static final double DESIGN_W = 1440;
    private static final double DESIGN_H = 900;
    // 介面放大時排版空間會變小，再小下去調查畫面的三欄就擠不下了
    private static final double MIN_W = 1200;
    private static final double MIN_H = 720;

    private final Region content;
    private final Scale scale = new Scale(1, 1, 0, 0);
    private final ReadOnlyDoubleWrapper factor = new ReadOnlyDoubleWrapper(1);
    private double zoom = 1;

    public Viewport(Region content) {
        this.content = content;
        content.getTransforms().add(scale);
        getChildren().add(content);
    }

    /** 玩家設定的介面大小，1 是預設。 */
    public void setZoom(double zoom) {
        this.zoom = zoom;
        requestLayout();
    }

    /** 設計尺寸換算到螢幕的倍率；跳出成獨立視窗的 Tooltip 不在縮放範圍內，要靠它自己調字級。 */
    public ReadOnlyDoubleProperty factorProperty() {
        return factor.getReadOnlyProperty();
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        double s = Math.min(Math.min(w / DESIGN_W, h / DESIGN_H) * zoom, Math.min(w / MIN_W, h / MIN_H));
        content.resizeRelocate(0, 0, w / s, h / s);
        scale.setX(s);
        scale.setY(s);
        factor.set(s);
    }
}
