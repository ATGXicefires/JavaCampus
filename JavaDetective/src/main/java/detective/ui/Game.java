package detective.ui;

import detective.content.CaseDef;
import detective.content.Chapter;
import detective.engine.Sandbox;
import detective.engine.Verifier;
import detective.fx.Motion;
import detective.fx.Sfx;
import detective.fx.Viewport;
import detective.progress.Progress;
import detective.progress.SaveStore;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/** 各畫面共用的服務與畫面切換。 */
public final class Game {

    final List<Chapter> chapters;
    final Progress progress;
    final Sfx sfx;
    final Sandbox sandbox;
    final Verifier verifier;
    private final SaveStore store;
    private final ExecutorService runner;
    private final StackPane root;
    private final Stage stage;
    private final Viewport viewport;
    private Overlay overlay;
    private String codeFontSheet;

    public Game(List<Chapter> chapters, Progress progress, SaveStore store, Sfx sfx, Sandbox sandbox,
                ExecutorService runner, StackPane root, Stage stage, Viewport viewport) {
        this.chapters = chapters;
        this.progress = progress;
        this.store = store;
        this.sfx = sfx;
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
        sfx.setVolumes(s.volume(), s.sfx(), s.rain());
        sfx.setTyping(s.typing());
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
        openOverlay(new SettingsView(this));
    }

    Button settingsButton() {
        Button b = new Button("設定");
        b.getStyleClass().add("btn-ghost");
        b.setOnAction(e -> openSettings());
        return b;
    }

    void resetProgress() {
        progress.resetCases();
        save();
        showCases();
    }

    /** Tooltip 是獨立的小視窗，不會被 {@link Viewport} 縮放，字級要自己跟著倍率走。 */
    Tooltip tooltip(String text) {
        Tooltip tip = new Tooltip(text);
        tip.styleProperty().bind(Bindings.format(Locale.ROOT, "-fx-font-size: %.1fpx;",
                viewport.factorProperty().multiply(13)));
        return tip;
    }

    public void showCases() {
        swapTo(new CaseSelectView(this));
    }

    void showBriefing(CaseDef c) {
        swapTo(new BriefingView(this, c));
    }

    void showWorkspace(CaseDef c) {
        swapTo(new WorkspaceView(this, c));
    }

    void showVerdict(CaseDef c) {
        swapTo(new VerdictView(this, c));
    }

    private void swapTo(Node view) {
        // swap 會把最上層當成舊畫面淡出，疊在上面的面板要先收掉
        closeOverlay();
        Motion.swap(root, view);
    }

    public void onEscape() {
        if (!closeOverlay()) {
            openSettings();
        }
    }

    void openOverlay(Node panel) {
        closeOverlay();
        overlay = Overlay.show(root, panel, () -> overlay = null);
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
        openOverlay(panel);
        cancel.requestFocus();
    }

    void save() {
        try {
            store.save(progress);
        } catch (RuntimeException e) {
            alert("存檔失敗", e.getMessage());
        }
    }

    boolean isClosed(CaseDef c) {
        return c.steps().stream().allMatch(s -> progress.isSolved(c.id(), s.id()));
    }

    /** 案件依序解鎖：上一案結案後才能打開下一案。 */
    boolean isUnlocked(CaseDef c) {
        List<CaseDef> all = allCases();
        int i = all.indexOf(c);
        return i <= 0 || isClosed(all.get(i - 1));
    }

    Optional<CaseDef> nextCase(CaseDef c) {
        List<CaseDef> all = allCases();
        int i = all.indexOf(c);
        return i >= 0 && i + 1 < all.size() ? Optional.of(all.get(i + 1)) : Optional.empty();
    }

    int caseNumber(CaseDef c) {
        return chapterOf(c).cases().indexOf(c) + 1;
    }

    Chapter chapterOf(CaseDef c) {
        return chapters.stream().filter(ch -> ch.cases().contains(c)).findFirst().orElseThrow();
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

    private List<CaseDef> allCases() {
        return chapters.stream().flatMap(ch -> ch.cases().stream()).toList();
    }

    private static void alert(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR, message);
        a.setHeaderText(title);
        a.showAndWait();
    }
}
