package campus.ui;

import javafx.collections.ListChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** 容器裡看得到的 {@link TextFlow} 可以用滑鼠拖曳選取，跨段落也行，選好之後按 Ctrl + C 或右鍵複製。 */
final class TextSelection {

    private static final KeyCombination COPY = new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN);

    /** 第幾段、段裡的第幾個字之前。 */
    private record Spot(int flow, int at) implements Comparable<Spot> {
        @Override
        public int compareTo(Spot o) {
            return flow != o.flow ? Integer.compare(flow, o.flow) : Integer.compare(at, o.at);
        }
    }

    private final Parent container;
    private final ContextMenu menu;
    private final List<Path> marks = new ArrayList<>();
    private List<TextFlow> flows = List.of();
    private Spot anchor;
    private Spot focus;

    static void install(Parent container) {
        new TextSelection(container);
    }

    private TextSelection(Parent container) {
        this.container = container;
        MenuItem copy = new MenuItem("複製");
        copy.setOnAction(e -> copy());
        menu = new ContextMenu(copy);
        container.getStyleClass().add("selectable");
        // 段落之間的空隙也要收到滑鼠事件，不然從空白處按下會被外層的 ScrollPane 拿走焦點
        container.setPickOnBounds(true);
        container.addEventHandler(MouseEvent.MOUSE_PRESSED, this::press);
        container.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::drag);
        container.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (COPY.match(e)) {
                e.consume();
                copy();
            }
        });
        container.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
            e.consume();
            if (!selectedText().isEmpty()) {
                menu.show(container, e.getScreenX(), e.getScreenY());
            }
        });
        // 視窗失去焦點（例如右鍵選單跳出來）時焦點擁有者不變，只有點到別的元件才算離開
        container.focusedProperty().addListener((o, was, is) -> {
            if (!is && container.getScene() != null && container.getScene().getFocusOwner() != container) {
                clear();
            }
        });
        container.getChildrenUnmodifiable().addListener((ListChangeListener<Node>) c -> clear());
    }

    private void press(MouseEvent e) {
        // ScrollPane 按下時會把焦點搶走，Ctrl + C 就收不到了
        e.consume();
        container.requestFocus();
        if (e.getButton() != MouseButton.PRIMARY) {
            return;
        }
        clear();
        flows = visibleFlows(container, new ArrayList<>());
        boolean onText = flows.stream()
                .anyMatch(f -> f.getLayoutBounds().contains(f.sceneToLocal(e.getSceneX(), e.getSceneY())));
        if (onText) {
            anchor = focus = spotAt(e.getSceneX(), e.getSceneY());
        }
    }

    private void drag(MouseEvent e) {
        if (anchor == null) {
            return;
        }
        e.consume();
        focus = spotAt(e.getSceneX(), e.getSceneY());
        paint();
    }

    private static List<TextFlow> visibleFlows(Parent parent, List<TextFlow> into) {
        for (Node n : parent.getChildrenUnmodifiable()) {
            if (!n.isVisible()) {
                continue;
            }
            if (n instanceof TextFlow f) {
                into.add(f);
            } else if (n instanceof Parent p) {
                visibleFlows(p, into);
            }
        }
        return into;
    }

    /** 滑鼠在兩段中間就算下一段的開頭，在最後一段下面就算最後一段的結尾。 */
    private Spot spotAt(double sceneX, double sceneY) {
        for (int i = 0; i < flows.size(); i++) {
            TextFlow f = flows.get(i);
            Point2D p = f.sceneToLocal(sceneX, sceneY);
            if (p.getY() < 0) {
                return new Spot(i, 0);
            }
            if (p.getY() < f.getHeight()) {
                return new Spot(i, f.getHitInfo(p).getInsertionIndex());
            }
        }
        int last = flows.size() - 1;
        return new Spot(last, plain(flows.get(last)).length());
    }

    private void paint() {
        for (Path p : marks) {
            ((Pane) p.getParent()).getChildren().remove(p);
        }
        marks.clear();
        if (anchor == null) {
            return;
        }
        Spot from = min(anchor, focus);
        Spot to = max(anchor, focus);
        for (int i = from.flow; i <= to.flow; i++) {
            TextFlow f = flows.get(i);
            int start = i == from.flow ? from.at : 0;
            int end = i == to.flow ? to.at : plain(f).length();
            if (start == end) {
                continue;
            }
            Path mark = new Path(f.getRangeShape(start, end, true));
            mark.getStyleClass().add("text-selection");
            // 不參與排版，TextFlow 才不會把它當成一個字；放在最底層，字的顏色不受影響
            mark.setManaged(false);
            // 按在反白上重新選取時，反白會先被拿掉；它若成了這次拖曳的目標，後面的拖曳事件就到不了容器
            mark.setMouseTransparent(true);
            f.getChildren().addFirst(mark);
            marks.add(mark);
        }
    }

    private String selectedText() {
        if (anchor == null) {
            return "";
        }
        Spot from = min(anchor, focus);
        Spot to = max(anchor, focus);
        List<String> parts = new ArrayList<>();
        for (int i = from.flow; i <= to.flow; i++) {
            String s = plain(flows.get(i));
            parts.add(s.substring(i == from.flow ? from.at : 0, i == to.flow ? to.at : s.length()));
        }
        return String.join("\n", parts);
    }

    private void copy() {
        String s = selectedText();
        if (!s.isEmpty()) {
            ClipboardContent content = new ClipboardContent();
            content.putString(s);
            Clipboard.getSystemClipboard().setContent(content);
        }
    }

    private void clear() {
        anchor = focus = null;
        paint();
    }

    private static String plain(TextFlow f) {
        return f.getChildren().stream().filter(n -> n instanceof Text).map(n -> ((Text) n).getText())
                .collect(Collectors.joining());
    }

    private static Spot min(Spot a, Spot b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    private static Spot max(Spot a, Spot b) {
        return a.compareTo(b) <= 0 ? b : a;
    }
}
