package campus.ui;

import campus.fx.Motion;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

/** 疊在畫面上的面板。放在被縮放的畫面裡，大小和風格才會跟遊戲一致，不像 Alert 是另開的系統視窗。 */
final class Overlay extends StackPane {

    private final StackPane host;
    private final Runnable onClosed;
    private Node before;

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

    /** 面板蓋上來時把焦點拿走，不然打字還會打進底下的編輯器；面板自己要焦點的（例如文件要接方向鍵）就不搶。 */
    static Overlay show(StackPane host, Node panel, Runnable onClosed) {
        Overlay o = new Overlay(host, panel, onClosed);
        Scene scene = host.getScene();
        o.before = scene == null ? null : scene.getFocusOwner();
        host.getChildren().add(o);
        if (scene != null && !o.contains(scene.getFocusOwner())) {
            o.requestFocus();
        }
        Motion.popIn(o, panel);
        return o;
    }

    void close() {
        if (host.getChildren().remove(this)) {
            // 關掉後回到原本的地方，例如繼續在編輯器裡打字
            if (before != null && before.getScene() != null) {
                before.requestFocus();
            }
            onClosed.run();
        }
    }

    private boolean contains(Node n) {
        for (; n != null; n = n.getParent()) {
            if (n == this) {
                return true;
            }
        }
        return false;
    }
}
