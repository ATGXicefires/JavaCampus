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
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/** 各畫面共用的服務與畫面切換。 */
public final class Game {

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

    public void toggleFullScreen() {
        stage.setFullScreen(!stage.isFullScreen());
    }

    /** 把設定一次套用到視窗、聲音、動效和編輯器字級。 */
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
        setCodeFont(s.codeFont());
    }

    /** 字級寫成一份小樣式表疊在主題後面，已經開著的編輯器也會馬上跟著變。 */
    private void setCodeFont(double size) {
        String css = String.format(Locale.ROOT,
                ".app-root .code-area { -fx-font-size: %.0fpx; } .lineno, .console-out { -fx-font-size: %.0fpx; }",
                size, size - 2);
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

    public void onEscape() {
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
        Label text = new Label(question);
        text.setWrapText(true);
        Button cancel = new Button("取消");
        cancel.getStyleClass().add("btn-ghost");
        cancel.setOnAction(e -> closeOverlay());
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

    void save() {
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
