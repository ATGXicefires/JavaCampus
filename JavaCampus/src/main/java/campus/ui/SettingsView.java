package campus.ui;

import campus.fx.Motion;
import campus.progress.Progress;
import campus.progress.Progress.AutoDelay;
import campus.progress.Progress.TextSpeed;
import campus.story.Route;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;

/** 設定面板：只有分組名稱和控制項，一調整就套用並存檔。 */
final class SettingsView extends VBox {

    private static final double[] ZOOMS = {0.9, 1, 1.1, 1.2};
    private static final int[][] WINDOW_SIZES = {
            {1280, 720}, {1600, 900}, {1920, 1080}, {2560, 1440},
            {1280, 800}, {1440, 900}, {1680, 1050}, {1920, 1200}, {2560, 1600}};
    private static final Pattern CODE_FONT_NAME = Pattern.compile(
            "(?i)mono|code|consol|courier|terminal|fixed|gothic|mingliu|hack|inconsolata|iosevka|sarasa|menlo");
    private static List<String> codeFamilies;

    private final Game game;
    private final Progress.Settings s;

    /** @param inGame 從劇情或程式題打開時，多一個「回到標題」 */
    SettingsView(Game game, boolean inGame) {
        this.game = game;
        this.s = game.progress.settings();
        getStyleClass().add("overlay-panel");
        setSpacing(14);
        setPrefWidth(820);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        Button defaults = new Button("恢復預設");
        defaults.getStyleClass().add("btn-ghost");
        defaults.setOnAction(e -> game.resetSettings());
        Button close = new Button("✕");
        close.getStyleClass().add("btn-ghost");
        close.setOnAction(e -> game.closeOverlay());
        HBox head = new HBox(10, defaults, close);
        head.setAlignment(Pos.CENTER_RIGHT);

        VBox left = new VBox(22,
                group("顯示",
                        row("全螢幕", onOff(s.fullscreen(), on -> s.setFullscreen(on))),
                        row("視窗大小", windowSizes()),
                        row("介面大小", segmented(List.of("90%", "100%", "110%", "120%"), zoomIndex(),
                                i -> s.setZoom(ZOOMS[i])))),
                group("程式",
                        row("字型", dropdown(codeFamilies(), s.codeFamily(), f -> f, f -> f, s::setCodeFamily)),
                        row("字體大小", slider(12, 26, 1, s.codeFont(), s::setCodeFont, v -> String.valueOf(Math.round(v)))),
                        row("配色", segmented(List.of("亮色", "暗色"), s.codeDark() ? 1 : 0, i -> s.setCodeDark(i == 1)))),
                group("劇情",
                        row("文字速度", segmented(List.of("慢", "中", "快", "立即"), s.textSpeed().ordinal(),
                                i -> s.setTextSpeed(TextSpeed.values()[i]))),
                        row("自動播放", segmented(List.of("短", "中", "長"), s.autoDelay().ordinal(),
                                i -> s.setAutoDelay(AutoDelay.values()[i]))),
                        row("快進", segmented(List.of("已讀", "全部"), s.skipAll() ? 1 : 0, i -> s.setSkipAll(i == 1)))),
                group("動效",
                        segmented(List.of("Q 彈", "一般", "關閉"), s.motion().ordinal(),
                                i -> s.setMotion(Motion.Level.values()[i]))));

        HBox resets = new HBox(10);
        for (Route r : Route.values()) {
            Button reset = new Button("重設" + r.label + "視角");
            reset.getStyleClass().add("btn-danger");
            reset.setDisable(game.progress.route(r) == null);
            reset.setOnAction(e -> game.confirm("要清除" + r.label + "視角的劇情和程式題進度嗎？另一個視角和設定會保留。",
                    "重設", () -> game.resetRoute(r)));
            resets.getChildren().add(reset);
        }
        VBox progressGroup = group("進度", resets);
        if (inGame) {
            Button title = new Button("回到標題");
            title.getStyleClass().add("btn-ghost");
            title.setOnAction(e -> game.showTitle());
            progressGroup.getChildren().add(title);
        }
        VBox right = new VBox(22,
                group("聲音",
                        row("主音量", percent(s.volume(), s::setVolume)),
                        row("音樂", percent(s.music(), s::setMusic)),
                        row("音效", percent(s.sfx(), s::setSfx)),
                        row("環境音", percent(s.ambient(), s::setAmbient)),
                        row("角色嗶聲", percent(s.voice(), s::setVoice)),
                        row("打字音效", onOff(s.typing(), on -> s.setTyping(on)))),
                progressGroup);

        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        left.setPrefWidth(0);
        right.setPrefWidth(0);
        getChildren().addAll(head, new HBox(40, left, right));
    }

    /** 常見的 16:9 和 16:10 尺寸，放不進目前螢幕的就不列出來（全螢幕另外有開關）。 */
    private ComboBox<int[]> windowSizes() {
        Screen screen = game.screen();
        double scale = screen.getOutputScaleX();
        Rectangle2D room = screen.getVisualBounds();
        List<int[]> sizes = new ArrayList<>();
        sizes.add(new int[]{0, 0});
        int[] chosen = sizes.getFirst();
        for (int[] size : WINDOW_SIZES) {
            boolean current = size[0] == s.windowWidth() && size[1] == s.windowHeight();
            // 高度多留一點給標題列
            if (current || size[0] / scale <= room.getWidth() && size[1] / scale <= room.getHeight() - 40) {
                sizes.add(size);
            }
            if (current) {
                chosen = size;
            }
        }
        return dropdown(sizes, chosen, SettingsView::sizeName, size -> null, size -> {
            s.setWindow(size[0], size[1]);
            game.applyWindowSize();
        });
    }

    private static String sizeName(int[] size) {
        if (size[0] <= 0) {
            return "最大化";
        }
        return size[0] + " × " + size[1] + "　" + (size[0] * 9 == size[1] * 16 ? "16:9" : "16:10");
    }

    /**
     * 系統裡的等寬字型，附帶的字型排第一個。全部字型都量一遍太慢，先用名字挑出可能是程式字型的，
     * 再比 i 和 W 的寬度確認真的等寬。清單只算一次。
     */
    private static List<String> codeFamilies() {
        if (codeFamilies == null) {
            List<String> found = new ArrayList<>(List.of(Progress.Settings.DEFAULT_CODE_FAMILY));
            for (String family : Font.getFamilies()) {
                if (!found.contains(family) && CODE_FONT_NAME.matcher(family).find() && isMonospace(family)) {
                    found.add(family);
                }
            }
            codeFamilies = List.copyOf(found);
        }
        return codeFamilies;
    }

    private static boolean isMonospace(String family) {
        Font font = Font.font(family, 20);
        // 找不到的字型會退回系統預設字型
        return font.getFamily().equals(family) && Math.abs(width("iiiiiiii", font) - width("WWWWWWWW", font)) < 0.5;
    }

    private static double width(String text, Font font) {
        Text t = new Text(text);
        t.setFont(font);
        return t.getLayoutBounds().getWidth();
    }

    /**
     * 下拉清單跳出來的是獨立的小視窗，不會被畫面縮放，清單裡的字級要自己跟著倍率走。
     *
     * @param family 清單裡用哪個字型顯示這一項；傳回 null 就用介面字型
     */
    private <T> ComboBox<T> dropdown(List<T> items, T selected, Function<T, String> name, Function<T, String> family,
                                     Consumer<T> onSelect) {
        ComboBox<T> box = new ComboBox<>(FXCollections.observableArrayList(items));
        box.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : name.apply(item));
                String f = empty || item == null ? null : family.apply(item);
                styleProperty().bind(Bindings.format(Locale.ROOT, "-fx-font-size: %.1fpx;%s",
                        game.uiScale().multiply(14), f == null ? "" : " -fx-font-family: \"" + f + "\";"));
            }
        });
        box.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : name.apply(item));
            }
        });
        box.setValue(selected);
        box.setMaxWidth(Double.MAX_VALUE);
        box.valueProperty().addListener((obs, old, now) -> {
            if (now != null) {
                onSelect.accept(now);
                changed();
            }
        });
        return box;
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
