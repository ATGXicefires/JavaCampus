package campus.ui;

import campus.fx.Motion;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/** 疊在畫面上的面板。放在被縮放的畫面裡，大小和風格才會跟遊戲一致，不像 Alert 是另開的系統視窗。 */
final class Overlay extends StackPane {

    private final StackPane host;
    private final Runnable onClosed;

    private Overlay(StackPane host, Node panel, Runnable onClosed) {
        this.host = host;
        this.onClosed = onClosed;
        getStyleClass().add("overlay");
        StackPane.setAlignment(panel, Pos.CENTER);
        getChildren().add(panel);
        // 點面板本身不算點到背景
        panel.setOnMouseClicked(e -> e.consume());
        setOnMouseClicked(e -> close());
    }

    static Overlay show(StackPane host, Node panel, Runnable onClosed) {
        Overlay o = new Overlay(host, panel, onClosed);
        host.getChildren().add(o);
        Motion.popIn(o, panel);
        return o;
    }

    void close() {
        if (host.getChildren().remove(this)) {
            onClosed.run();
        }
    }
}
