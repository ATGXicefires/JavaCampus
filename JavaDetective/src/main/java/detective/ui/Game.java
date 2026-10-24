package detective.ui;

import detective.content.CaseDef;
import detective.content.Chapter;
import detective.engine.Sandbox;
import detective.engine.Verifier;
import detective.fx.Motion;
import detective.fx.Sfx;
import detective.progress.Progress;
import detective.progress.SaveStore;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;

import java.util.List;
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

    public Game(List<Chapter> chapters, Progress progress, SaveStore store, Sfx sfx, Sandbox sandbox,
                ExecutorService runner, StackPane root) {
        this.chapters = chapters;
        this.progress = progress;
        this.store = store;
        this.sfx = sfx;
        this.sandbox = sandbox;
        this.verifier = new Verifier(sandbox);
        this.runner = runner;
        this.root = root;
    }

    public void showCases() {
        Motion.swap(root, new CaseSelectView(this));
    }

    void showBriefing(CaseDef c) {
        Motion.swap(root, new BriefingView(this, c));
    }

    void showWorkspace(CaseDef c) {
        Motion.swap(root, new WorkspaceView(this, c));
    }

    void showVerdict(CaseDef c) {
        Motion.swap(root, new VerdictView(this, c));
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
