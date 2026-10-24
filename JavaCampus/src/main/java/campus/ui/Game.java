package campus.ui;

import campus.art.Art;
import campus.engine.Sandbox;
import campus.engine.Verifier;
import campus.fx.Motion;
import campus.fx.Music;
import campus.fx.Sfx;
import campus.fx.Viewport;
import campus.lesson.Lesson;
import campus.lesson.LessonLibrary;
import campus.progress.Progress;
import campus.progress.SaveStore;
import campus.story.Chapter;
import campus.story.Op;
import campus.story.Route;
import campus.story.StoryLibrary;
import campus.story.StoryState;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.css.PseudoClass;
import javafx.event.EventTarget;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** 各畫面共用的服務與畫面切換。 */
public final class Game {

    /** 程式題的配色掛在最外層，各畫面的樣式用 {@code .app-root:code-dark} 切換，不用各自監聽設定。 */
    private static final PseudoClass CODE_DARK = PseudoClass.getPseudoClass("code-dark");
    private static final String CODE_SELECTORS = Stream.of(".code-area", ".code-font", ".md-code", ".md-pre",
            ".lineno", ".console-out", ".shape-box", ".hint-code", ".probe-line", ".raw", ".var-name", ".var-type",
            ".var-value").map(c -> ".app-root " + c).collect(Collectors.joining(", "));

    /** 想自己處理按鍵的畫面（劇情、選視角）實作這個介面；Esc 和 F11 由 {@link Game} 統一處理。 */
    interface KeyAware {
        void onKey(KeyEvent e);
    }

    final StoryLibrary story;
    final LessonLibrary lessons;
    final Art art;
    final Progress progress;
    final Sfx sfx;
    final Music music;
    final Sandbox sandbox;
    final Verifier verifier;
    private final SaveStore store;
    private final ExecutorService runner;
    private final StackPane root;
    private final Stage stage;
    private final Viewport viewport;
    private Node current;
    private Overlay overlay;
    private String codeFontSheet;

    public Game(StoryLibrary story, LessonLibrary lessons, Art art, Progress progress, SaveStore store, Sfx sfx,
                Music music, Sandbox sandbox, ExecutorService runner, StackPane root, Stage stage, Viewport viewport) {
        this.story = story;
        this.lessons = lessons;
        this.art = art;
        this.progress = progress;
        this.store = store;
        this.sfx = sfx;
        this.music = music;
        this.sandbox = sandbox;
        this.verifier = new Verifier(sandbox);
        this.runner = runner;
        this.root = root;
        this.stage = stage;
        this.viewport = viewport;
        // 系統也可能自己把視窗切出全螢幕，以視窗實際狀態為準
        stage.fullScreenProperty().addListener((obs, was, now) -> {
            if (progress.settings().fullscreen() != now) {
                progress.settings().setFullscreen(now);
                save();
            }
        });
    }

    /** 預設尺寸指的是畫面本身的實際像素，不含標題列和邊框；視窗用的是邏輯像素，要除以螢幕縮放比例。 */
    public void applyWindowSize() {
        Progress.Settings s = progress.settings();
        if (stage.isFullScreen()) {
            return;
        }
        if (s.windowWidth() <= 0) {
            stage.setMaximized(true);
            return;
        }
        stage.setMaximized(false);
        double scale = screen().getOutputScaleX();
        double frameW = stage.getWidth() - stage.getScene().getWidth();
        double frameH = stage.getHeight() - stage.getScene().getHeight();
        stage.setWidth(s.windowWidth() / scale + frameW);
        stage.setHeight(s.windowHeight() / scale + frameH);
        stage.centerOnScreen();
    }

    /** 視窗所在的螢幕；多螢幕時預設尺寸要以那一個螢幕的大小和縮放比例為準。 */
    Screen screen() {
        List<Screen> on = Screen.getScreensForRectangle(stage.getX(), stage.getY(), Math.max(stage.getWidth(), 1),
                Math.max(stage.getHeight(), 1));
        return on.isEmpty() ? Screen.getPrimary() : on.getFirst();
    }

    /** 設計尺寸換算到螢幕的倍率，給跳出成獨立視窗、不在縮放範圍內的清單調字級用。 */
    ReadOnlyDoubleProperty uiScale() {
        return viewport.factorProperty();
    }

    public void toggleFullScreen() {
        stage.setFullScreen(!stage.isFullScreen());
    }

    /** 把設定一次套用到視窗、聲音、動效、程式字型和配色。視窗大小只在選的時候套用，見 {@link #applyWindowSize()}。 */
    public void applySettings() {
        Progress.Settings s = progress.settings();
        viewport.setZoom(s.zoom());
        if (stage.isFullScreen() != s.fullscreen()) {
            stage.setFullScreen(s.fullscreen());
        }
        sfx.setVolumes(s.volume(), s.sfx(), s.ambient(), s.voice());
        sfx.setTyping(s.typing());
        music.setVolume(s.volume() * s.music());
        Motion.setLevel(s.motion());
        setCodeFont(s.codeFont(), s.codeFamily());
        root.pseudoClassStateChanged(CODE_DARK, s.codeDark());
    }

    /**
     * 字型寫成一份小樣式表疊在主題後面，已經開著的編輯器也會馬上跟著變。
     * 選擇器都帶 {@code .app-root}，跟主題裡最具體的程式字樣式一樣具體，靠排在後面蓋過去。
     */
    private void setCodeFont(double size, String family) {
        String css = String.format(Locale.ROOT,
                ".app-root .code-area { -fx-font-size: %.0fpx; } .app-root .lineno, .app-root .console-out { -fx-font-size: %.0fpx; } "
                        + "%s { -fx-font-family: \"%s\"; }",
                size, size - 2, CODE_SELECTORS, family.replace("\"", ""));
        String url = "data:text/css;base64," + Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
        List<String> sheets = stage.getScene().getStylesheets();
        sheets.remove(codeFontSheet);
        sheets.add(url);
        codeFontSheet = url;
    }

    void openSettings() {
        boolean inGame = current instanceof StoryView || current instanceof LessonView;
        openOverlay(new SettingsView(this, inGame), null);
    }

    void resetSettings() {
        confirm("要把所有設定恢復成預設值嗎？劇情和程式題的進度不受影響。", "恢復預設", () -> {
            progress.resetSettings();
            applySettings();
            applyWindowSize();
            save();
            openSettings();
        }, this::openSettings);
    }

    Button settingsButton() {
        Button b = new Button("設定");
        b.getStyleClass().add("btn-ghost");
        b.setOnAction(e -> openSettings());
        return b;
    }

    /** Tooltip 是獨立的小視窗，不會被 {@link Viewport} 縮放，字級要自己跟著倍率走。 */
    Tooltip tooltip(String text) {
        Tooltip tip = new Tooltip(text);
        tip.styleProperty().bind(Bindings.format(Locale.ROOT, "-fx-font-size: %.1fpx;",
                viewport.factorProperty().multiply(13)));
        return tip;
    }

    public void showTitle() {
        sfx.setAmbience(List.of());
        music.play(Music.Track.TITLE);
        swapTo(new TitleView(this));
    }

    void showRouteSelect() {
        swapTo(new RouteSelectView(this));
    }

    void showChapterSelect() {
        swapTo(new ChapterSelectView(this));
    }

    /** 這個視角的程式題 id，重設或重新開始那個視角時一起清掉。 */
    List<String> lessonIds(Route r) {
        return lessons.lessons().values().stream().filter(l -> l.route().equals(r.label)).map(Lesson::id).toList();
    }

    void newGame(Route r) {
        StoryState s = progress.startRoute(r, lessonIds(r));
        s.begin(story.chapters().getFirst(), r, false);
        progress.setLastRoute(r);
        save();
        showStory(r);
    }

    void continueGame() {
        Route r = progress.lastRoute();
        if (r != null) {
            showStory(r);
        }
    }

    void replayChapter(Route r, Chapter ch) {
        StoryState s = progress.route(r);
        s.begin(ch, r, true);
        progress.setLastRoute(r);
        save();
        showStory(r);
    }

    void showStory(Route r) {
        StoryState s = progress.route(r);
        Chapter ch = story.chapter(s.chapter());
        if (s.atChapterEnd()) {
            showChapterEnd(r);
            return;
        }
        swapTo(new StoryView(this, r, ch, s));
    }

    /** 從劇情進入程式題；按「繼續」時回到同一個劇情畫面，再接著播下一行。 */
    void showLesson(String lessonId, StoryView back, Runnable next) {
        swapTo(new LessonView(this, lessons.lesson(lessonId), () -> {
            swapTo(back);
            back.resumeAfterLesson();
            next.run();
        }));
    }

    void showChapterEnd(Route r) {
        StoryState s = progress.route(r);
        Chapter ch = story.chapter(s.chapter());
        sfx.setAmbience(List.of());
        swapTo(new ChapterEndView(this, ch, chapterNote(s, ch), previousProgress(ch), () -> {
            Chapter next = story.next(ch);
            if (next == null) {
                showTitle();
                return;
            }
            s.begin(next, r, false);
            save();
            showStory(r);
        }));
    }

    /** 章末畫面的筆記本文字就寫在存檔位置那一行的 {@code @章末} 指令裡。 */
    private static String chapterNote(StoryState s, Chapter ch) {
        StoryState.Pos at = s.at();
        return ch.file(at.file()).scene(at.scene())
                .map(scene -> at.index() < scene.ops().size() ? scene.ops().get(at.index()) : null)
                .filter(op -> op instanceof Op.Cmd c && c.name().equals("章末"))
                .map(op -> ((Op.Cmd) op).arg(0))
                .orElse("");
    }

    private int previousProgress(Chapter ch) {
        int i = story.chapters().indexOf(ch);
        return i > 0 ? story.chapters().get(i - 1).progress() : 0;
    }

    void resetRoute(Route r) {
        progress.resetRoute(r, lessonIds(r));
        save();
        showTitle();
    }

    private void swapTo(Node view) {
        // swap 會把最上層當成舊畫面淡出，疊在上面的面板要先收掉
        closeOverlay();
        current = view;
        Motion.swap(root, view);
    }

    public void onKey(KeyEvent e) {
        if (overlay == null && current instanceof KeyAware k) {
            k.onKey(e);
        }
    }

    /** @param target 按下 Esc 時的焦點；編輯器的自動完成開著的話，這一下只收起清單 */
    public void onEscape(EventTarget target) {
        if (CodeCompletion.dismiss(target)) {
            return;
        }
        if (!closeOverlay()) {
            openSettings();
        }
    }

    /** @param onClosed 面板關掉之後要做的事，例如文件看完才讓劇情往下走；沒有就傳 null */
    void openOverlay(Node panel, Runnable onClosed) {
        closeOverlay();
        overlay = Overlay.show(root, panel, () -> {
            overlay = null;
            if (onClosed != null) {
                onClosed.run();
            }
        });
    }

    boolean closeOverlay() {
        if (overlay == null) {
            return false;
        }
        overlay.close();
        return true;
    }

    void confirm(String question, String action, Runnable onConfirm) {
        confirm(question, action, onConfirm, this::closeOverlay);
    }

    /** @param onCancel 按「取消」要做的事，例如回到原本開著的面板 */
    void confirm(String question, String action, Runnable onConfirm, Runnable onCancel) {
        Label text = new Label(question);
        text.setWrapText(true);
        Button cancel = new Button("取消");
        cancel.getStyleClass().add("btn-ghost");
        cancel.setOnAction(e -> onCancel.run());
        Button ok = new Button(action);
        ok.getStyleClass().add("btn-primary");
        ok.setOnAction(e -> {
            closeOverlay();
            onConfirm.run();
        });
        HBox buttons = new HBox(10, cancel, ok);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        VBox panel = new VBox(22, text, buttons);
        panel.getStyleClass().add("overlay-panel");
        panel.setPrefWidth(440);
        panel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        openOverlay(panel, null);
        cancel.requestFocus();
    }

    public void save() {
        try {
            store.save(progress);
        } catch (RuntimeException e) {
            alert("存檔失敗", e.getMessage());
        }
    }

    /** 在沙盒專用的背景執行緒上做事，完成後回到 FX 執行緒。 */
    <T> void background(Callable<T> work, Consumer<T> done, Runnable always) {
        runner.submit(() -> {
            try {
                T result = work.call();
                Platform.runLater(() -> {
                    always.run();
                    done.accept(result);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    always.run();
                    alert("執行環境發生問題", String.valueOf(e.getMessage()));
                });
            }
        });
    }

    void exit() {
        Platform.exit();
    }

    private static void alert(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR, message);
        a.setHeaderText(title);
        a.showAndWait();
    }
}
