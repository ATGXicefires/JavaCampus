package detective.ui;

import detective.fx.Motion;
import detective.progress.Progress;
import detective.progress.Progress.TextSpeed;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.IntConsumer;

/** 設定面板：只有分組名稱和控制項，一調整就套用並存檔。 */
final class SettingsView extends VBox {

    private static final double[] ZOOMS = {0.9, 1, 1.1, 1.2};

    private final Game game;
    private final Progress.Settings s;

    SettingsView(Game game) {
        this.game = game;
        this.s = game.progress.settings();
        getStyleClass().add("overlay-panel");
        setSpacing(14);
        setPrefWidth(760);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        Button close = new Button("✕");
        close.getStyleClass().add("btn-ghost");
        close.setOnAction(e -> game.closeOverlay());
        HBox head = new HBox(close);
        head.setAlignment(Pos.CENTER_RIGHT);

        VBox left = new VBox(22,
                group("顯示",
                        row("全螢幕", onOff(s.fullscreen(), on -> s.setFullscreen(on))),
                        row("介面大小", segmented(List.of("90%", "100%", "110%", "120%"), zoomIndex(),
                                i -> s.setZoom(ZOOMS[i])))),
                group("文字",
                        row("程式字體", slider(12, 26, 1, s.codeFont(), s::setCodeFont, v -> String.valueOf(Math.round(v)))),
                        row("劇情速度", segmented(List.of("慢", "中", "快", "立即"), s.textSpeed().ordinal(),
                                i -> s.setTextSpeed(TextSpeed.values()[i])))),
                group("動效",
                        segmented(List.of("Q 彈", "一般", "關閉"), s.motion().ordinal(),
                                i -> s.setMotion(Motion.Level.values()[i]))));

        Button reset = new Button("重設進度");
        reset.getStyleClass().add("btn-danger");
        reset.setOnAction(e -> game.confirm("要清除所有案件的進度嗎？設定會保留。", "重設進度", game::resetProgress));
        VBox right = new VBox(22,
                group("聲音",
                        row("主音量", percent(s.volume(), s::setVolume)),
                        row("音效", percent(s.sfx(), s::setSfx)),
                        row("雨聲", percent(s.rain(), s::setRain)),
                        row("打字音效", onOff(s.typing(), on -> s.setTyping(on)))),
                group("進度", reset));

        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        left.setPrefWidth(0);
        right.setPrefWidth(0);
        getChildren().addAll(head, new HBox(40, left, right));
    }

    private void changed() {
        game.applySettings();
        game.save();
    }

    private int zoomIndex() {
        int best = 0;
        for (int i = 1; i < ZOOMS.length; i++) {
            if (Math.abs(ZOOMS[i] - s.zoom()) < Math.abs(ZOOMS[best] - s.zoom())) {
                best = i;
            }
        }
        return best;
    }

    private static VBox group(String name, Node... rows) {
        Label title = new Label(name);
        title.getStyleClass().add("settings-group");
        VBox box = new VBox(10, title);
        box.getChildren().addAll(rows);
        return box;
    }

    private static HBox row(String name, Node control) {
        Label label = new Label(name);
        label.setMinWidth(88);
        HBox.setHgrow(control, Priority.ALWAYS);
        HBox row = new HBox(12, label, control);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox onOff(boolean on, Consumer<Boolean> set) {
        return segmented(List.of("開", "關"), on ? 0 : 1, i -> set.accept(i == 0));
    }

    /** 一排互斥的按鈕；再點一次已選的那個不會變成全部沒選。 */
    private HBox segmented(List<String> labels, int selected, IntConsumer onSelect) {
        ToggleGroup group = new ToggleGroup();
        HBox box = new HBox();
        for (String text : labels) {
            ToggleButton b = new ToggleButton(text);
            b.getStyleClass().add("segment");
            b.setToggleGroup(group);
            box.getChildren().add(b);
        }
        group.selectToggle(group.getToggles().get(selected));
        group.selectedToggleProperty().addListener((obs, old, now) -> {
            if (now == null) {
                group.selectToggle(old);
                return;
            }
            onSelect.accept(group.getToggles().indexOf(now));
            changed();
        });
        return box;
    }

    private HBox percent(double value, DoubleConsumer set) {
        return slider(0, 1, 0.05, value, set, v -> String.valueOf(Math.round(v * 100)));
    }

    /** 拖曳時即時套用，放開才存檔，不然一路拖會一直寫檔。 */
    private HBox slider(double min, double max, double step, double value, DoubleConsumer set, DoubleFunction<String> format) {
        Slider slider = new Slider(min, max, value);
        slider.setMajorTickUnit(step);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
        slider.setBlockIncrement(step);
        HBox.setHgrow(slider, Priority.ALWAYS);
        Label shown = new Label(format.apply(value));
        shown.getStyleClass().add("settings-value");
        slider.valueProperty().addListener((obs, old, v) -> {
            set.accept(v.doubleValue());
            shown.setText(format.apply(v.doubleValue()));
            game.applySettings();
            if (!slider.isValueChanging()) {
                game.save();
            }
        });
        slider.valueChangingProperty().addListener((obs, was, changing) -> {
            if (!changing) {
                game.save();
            }
        });
        HBox box = new HBox(10, slider, shown);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }
}
