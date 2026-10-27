package campus.ui;

import campus.engine.RunResult;
import campus.engine.RunResult.Outcome;
import campus.engine.RunResult.Probe;
import campus.engine.RunResult.Problem;
import campus.engine.RunResult.Severity;
import campus.engine.Verdict;
import campus.engine.Verifier;
import campus.fx.Motion;
import campus.fx.Sfx;
import campus.lesson.Check;
import campus.lesson.Handbook;
import campus.lesson.Lesson;
import campus.lesson.Reactions;
import campus.lesson.SourceFile;
import campus.lesson.Step;
import campus.progress.Progress;
import campus.story.Cast;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/** 程式題：左邊是步驟和題目，中間寫程式和看結果，右邊是筆記和搭檔的反應。三區各自是一張卡片。 */
final class LessonView extends StackPane {

    // 完整程式的檔案清單另外存一份，玩家新增的檔案下次才找得回來；檔名裡不會有「#」，不會跟檔案的鍵撞在一起
    private static final String FILE_LIST = "#files";

    private final Game game;
    private final Lesson lesson;
    private final Progress.LessonRecord record;
    private final boolean practice;
    private final Runnable onContinue;
    private final EditorTabs editors;
    private final VBox stepList = new VBox(4);
    private final VBox detail = new VBox(12);
    private final VBox console = new VBox(10);
    private final ScrollPane consoleScroll = new ScrollPane(console);
    private final Button runButton = new Button("▶  執行");
    private final Button verifyButton = new Button("完成");
    private final Button stopButton = new Button("停止");
    private final Button resetButton = new Button("重設");
    private final Button continueButton = new Button("繼續　→");
    private final Button backButton = new Button("←");
    private final Label reactionWho = new Label();
    private final Label reactionText = new Label();
    private final VBox reaction = new VBox(6, reactionWho, reactionText);
    private final Pane peekLayer = new Pane();
    private final HintDrawer hints;
    private final Random rnd = new Random();
    private final PauseTransition autosave = new PauseTransition(Duration.seconds(1));
    // 大部分執行一下子就結束，等一會兒還沒跑完才露出「停止」，免得按鈕一閃而過
    private final PauseTransition stopReveal = new PauseTransition(Duration.millis(400));
    private Step current;
    private Step justSolved;
    private boolean moreOpen;
    private Animation nudge;
    private boolean busy;

    /**
     * @param record     讀寫哪一份紀錄：劇情和練習分開存
     * @param practice   從練習的終端機開的：頂端多一個「←」，過關不解鎖筆記
     * @param onContinue 按「繼續」離開；練習時按「←」也走這裡
     */
    LessonView(Game game, Lesson lesson, Progress.LessonRecord record, boolean practice, Runnable onContinue) {
        this.game = game;
        this.lesson = lesson;
        this.record = record;
        this.practice = practice;
        this.onContinue = onContinue;
        hints = new HintDrawer(game, record, this::refreshSteps);
        editors = new EditorTabs(game, lesson.vars(), this::onEdited, () -> {
            autosave.stop();
            saveCode();
        });
        Node center = centerPanel();
        HBox.setHgrow(center, Priority.ALWAYS);
        HBox zones = new HBox(12, leftPanel(), center, rightPanel());
        zones.setPadding(new Insets(12));
        BorderPane layout = new BorderPane();
        layout.setTop(topBar());
        layout.setCenter(zones);
        peekLayer.setMouseTransparent(true);
        peekLayer.setPickOnBounds(false);
        StackPane.setAlignment(hints, Pos.CENTER_RIGHT);
        getChildren().addAll(layout, peekLayer, hints);
        getStyleClass().add("lesson");
        editors.addEventFilter(KeyEvent.KEY_PRESSED, this::onKey);
        autosave.setOnFinished(e -> saveCode());
        stopReveal.setOnFinished(e -> stopButton.setVisible(busy));
        open(firstOpenStep());
        if (isDone()) {
            showContinue(false);
        }
    }

    private Node topBar() {
        Label title = new Label(lesson.title());
        title.getStyleClass().add("topbar-title");
        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        Button hintButton = new Button("提示");
        hintButton.getStyleClass().add("btn-ghost");
        hintButton.setOnAction(e -> hints.toggle());
        hintButton.setFocusTraversable(false);
        HBox bar = new HBox(16, title, gap, hintButton, game.settingsButton());
        if (practice) {
            backButton.getStyleClass().add("btn-ghost");
            backButton.setFocusTraversable(false);
            backButton.setOnAction(e -> leave());
            bar.getChildren().addFirst(backButton);
        }
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("topbar");
        return bar;
    }

    private static Label zoneHead(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("zone-head");
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    private Node leftPanel() {
        ScrollPane detailScroll = new ScrollPane(detail);
        detailScroll.setFitToWidth(true);
        VBox.setVgrow(detailScroll, Priority.ALWAYS);
        detail.setPadding(new Insets(4, 6, 12, 2));
        TextSelection.install(detail);
        VBox body = new VBox(10, stepList, new Separator(), detailScroll);
        body.setPadding(new Insets(12, 14, 14, 14));
        VBox.setVgrow(body, Priority.ALWAYS);
        VBox box = new VBox(zoneHead("題目"), body);
        box.setPrefWidth(320);
        box.setMinWidth(320);
        box.getStyleClass().add("zone");
        return box;
    }

    private Node centerPanel() {
        runButton.getStyleClass().add("btn-primary");
        runButton.setTooltip(game.tooltip("Ctrl + Enter"));
        runButton.setOnAction(e -> run());
        verifyButton.getStyleClass().add("btn-verify");
        verifyButton.setTooltip(game.tooltip("Ctrl + Shift + Enter"));
        verifyButton.setOnAction(e -> verify());
        stopButton.getStyleClass().add("btn-ghost");
        stopButton.setOnAction(e -> game.sandbox.stop());
        stopButton.managedProperty().bind(stopButton.visibleProperty());
        stopButton.setVisible(false);
        resetButton.getStyleClass().add("btn-ghost");
        resetButton.setOnAction(e -> reset());
        continueButton.getStyleClass().add("btn-continue");
        continueButton.setOnAction(e -> leave());
        continueButton.managedProperty().bind(continueButton.visibleProperty());
        continueButton.setVisible(false);
        // 跟一般 IDE 一樣，按了執行還能直接繼續打字，焦點不要被按鈕拿走
        for (Button b : List.of(runButton, verifyButton, stopButton, resetButton)) {
            b.setFocusTraversable(false);
        }
        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        HBox toolbar = new HBox(10, runButton, verifyButton, resetButton, stopButton, gap, continueButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("toolbar-row");

        VBox editorZone = new VBox(editors, toolbar);
        VBox.setVgrow(editors, Priority.ALWAYS);
        editorZone.getStyleClass().addAll("zone", "editor-zone");
        console.getStyleClass().add("console");
        consoleScroll.setFitToWidth(true);
        // 不用 fitToHeight：它會把內容壓成可視範圍那麼高，輸出一長就被截成「...」而且捲不動；改成至少撐滿
        consoleScroll.viewportBoundsProperty().addListener((obs, old, b) -> console.setMinHeight(b.getHeight()));
        consoleScroll.getStyleClass().add("console-scroll");
        VBox.setVgrow(consoleScroll, Priority.ALWAYS);
        VBox consoleZone = new VBox(zoneHead("執行結果"), consoleScroll);
        consoleZone.getStyleClass().addAll("zone", "console-zone");
        SplitPane split = new SplitPane(editorZone, consoleZone);
        split.setOrientation(Orientation.VERTICAL);
        split.setDividerPositions(0.6);
        split.getStyleClass().add("zone-split");
        return split;
    }

    private Node rightPanel() {
        // 這一題相關的筆記條目，寫題目的時候就看得到，過關才會收進「程式基礎教學筆記」
        List<Handbook.Entry> notes = lesson.notes().stream()
                .map(id -> game.lessons.handbook().entry(id).orElseThrow()).toList();
        TabPane tabs = new TabPane(new Tab("筆記", new NoteList(notes, peekLayer)));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getStyleClass().add("zone");
        VBox.setVgrow(tabs, Priority.ALWAYS);

        boolean inner = lesson.partner().endsWith("OS");
        String who = inner ? lesson.partner().substring(0, lesson.partner().length() - 2) : lesson.partner();
        reactionWho.setText(inner ? "" : who);
        // 角色色寫成具名顏色而不是直接設文字色，暗色配色才能在樣式表裡把它調亮
        reactionWho.setStyle("who-color: " + Cast.colorOf(who) + ";");
        reactionWho.getStyleClass().add("reaction-who");
        reactionWho.managedProperty().bind(reactionWho.visibleProperty());
        reactionWho.setVisible(!inner);
        reactionText.getStyleClass().add("reaction-text");
        if (inner) {
            reactionText.getStyleClass().add("os");
        }
        reactionText.setWrapText(true);
        reaction.getStyleClass().add("reaction");
        reaction.managedProperty().bind(reaction.visibleProperty());
        reaction.setVisible(false);
        reaction.setMinHeight(Region.USE_PREF_SIZE);

        VBox box = new VBox(12, tabs, reaction);
        box.setPrefWidth(330);
        box.setMinWidth(330);
        return box;
    }

    private void react(Reactions.Kind kind) {
        String line = game.lessons.reactions().pick(lesson.partner(), kind, rnd);
        if (line == null) {
            return;
        }
        reactionText.setText(lesson.partner().endsWith("OS") ? "（" + line + "）" : line);
        reaction.setVisible(true);
        Motion.pop(reaction);
    }

    private void onKey(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER && e.isShortcutDown()) {
            e.consume();
            if (e.isShiftDown()) {
                verify();
            } else {
                run();
            }
        }
    }

    private void onEdited() {
        stopNudge();
        autosave.playFromStart();
    }

    private void saveCode() {
        if (current != null) {
            saveFiles(current, editors.files());
            game.save();
        }
    }

    /** 片段只有一個檔案，存在步驟 id 底下，跟舊存檔一樣；完整程式每個檔案各存在「步驟 id/檔名」。 */
    private void saveFiles(Step s, List<SourceFile> files) {
        if (s.form() == Step.Form.SNIPPET) {
            record.setCode(lesson.id(), s.id(), files.getFirst().text());
            return;
        }
        for (SourceFile f : files) {
            record.setCode(lesson.id(), s.id() + "/" + f.name(), f.text());
        }
        record.setCode(lesson.id(), s.id() + FILE_LIST,
                files.stream().map(SourceFile::name).collect(Collectors.joining("\n")));
    }

    /** 還沒存過就是 null。 */
    private List<SourceFile> savedFiles(Step s) {
        if (s.form() == Step.Form.SNIPPET) {
            String code = record.code(lesson.id(), s.id());
            return code == null ? null : List.of(new SourceFile(s.form().fileName, code));
        }
        String names = record.code(lesson.id(), s.id() + FILE_LIST);
        if (names == null) {
            return null;
        }
        return Arrays.stream(names.split("\n")).map(name -> {
            String code = record.code(lesson.id(), s.id() + "/" + name);
            return new SourceFile(name, code == null ? "" : code);
        }).toList();
    }

    private void leave() {
        autosave.stop();
        saveCode();
        onContinue.run();
    }

    private boolean isDone() {
        return lesson.steps().stream().allMatch(s -> record.isSolved(lesson.id(), s.id()));
    }

    private Step firstOpenStep() {
        return lesson.steps().stream().filter(s -> !record.isSolved(lesson.id(), s.id())).findFirst()
                .orElse(lesson.steps().getLast());
    }

    private void open(Step s) {
        if (current != null) {
            autosave.stop();
            saveCode();
        }
        current = s;
        moreOpen = false;
        Set<String> given = givenFiles(s);
        editors.open(initialFiles(s), s.form() == Step.Form.PROGRAM, name -> !given.contains(name));
        refreshSteps();
        refreshDetail();
        hints.bind(lesson, s);
        console.getChildren().clear();
        // 畫面剛建立時編輯器還不在 scene 裡，要等加進去之後才拿得到焦點
        Platform.runLater(editors::focusEditor);
    }

    private List<SourceFile> initialFiles(Step s) {
        List<SourceFile> saved = savedFiles(s);
        return saved != null ? saved : startingFiles(s);
    }

    /** 沒有 starter 的步驟沿用上一步過關的程式，讓同一題的程式一路長大。 */
    private List<SourceFile> startingFiles(Step s) {
        if (!s.starter().isEmpty()) {
            return s.starter();
        }
        int i = lesson.steps().indexOf(s);
        if (i > 0) {
            List<SourceFile> previous = savedFiles(lesson.steps().get(i - 1));
            if (previous != null) {
                return previous;
            }
        }
        return List.of(new SourceFile(s.form().fileName, ""));
    }

    /** 題目本來就給的檔案不能刪；沒有 starter 的步驟沿用前面最近一個有 starter 的步驟。 */
    private Set<String> givenFiles(Step s) {
        for (int i = lesson.steps().indexOf(s); i >= 0; i--) {
            List<SourceFile> starter = lesson.steps().get(i).starter();
            if (!starter.isEmpty()) {
                return starter.stream().map(SourceFile::name).collect(Collectors.toSet());
            }
        }
        return Set.of(s.form().fileName);
    }

    private void reset() {
        List<SourceFile> start = startingFiles(current);
        Set<String> kept = start.stream().map(SourceFile::name).collect(Collectors.toSet());
        boolean dropsFiles = editors.files().stream().anyMatch(f -> !kept.contains(f.name()));
        String question = dropsFiles
                ? "要把程式碼改回這一步一開始的樣子嗎？目前寫的內容會被取代，新增的檔案也會刪掉。"
                : "要把程式碼改回這一步一開始的樣子嗎？目前寫的內容會被取代。";
        game.confirm(question, "重設", () -> editors.revert(start));
    }

    private void refreshSteps() {
        stepList.getChildren().clear();
        int firstUnsolved = 0;
        while (firstUnsolved < lesson.steps().size()
                && record.isSolved(lesson.id(), lesson.steps().get(firstUnsolved).id())) {
            firstUnsolved++;
        }
        for (int i = 0; i < lesson.steps().size(); i++) {
            Step s = lesson.steps().get(i);
            boolean solved = record.isSolved(lesson.id(), s.id());
            boolean reachable = solved || i == firstUnsolved;
            Label num = new Label(solved ? "✓" : String.valueOf(i + 1));
            num.getStyleClass().add("step-num");
            Label title = new Label(s.title());
            title.getStyleClass().add("step-title");
            HBox row = new HBox(10, num, title);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("step-row");
            if (s == current) {
                row.getStyleClass().add("current");
            }
            if (solved) {
                row.getStyleClass().add("solved");
            }
            if (s == justSolved) {
                Motion.pop(num);
                justSolved = null;
            }
            if (!reachable) {
                row.getStyleClass().add("locked");
            } else {
                row.getStyleClass().add("pressable");
                row.setOnMouseClicked(e -> {
                    if (s != current && !busy) {
                        open(s);
                    }
                });
            }
            stepList.getChildren().add(row);
        }
    }

    /** 先給這一步要做的事和預期的樣子，背景和完整寫法收在「完整說明」裡；題目給的變數寫在編輯器最上面。 */
    private void refreshDetail() {
        Label title = new Label(current.title());
        title.getStyleClass().add("detail-title");
        title.setWrapText(true);
        TextFlow goal = MiniMarkdown.inline(current.goal());
        goal.getStyleClass().add("step-goal");
        TextFlow shape = new TextFlow();
        shape.getChildren().addAll(MiniMarkdown.texts(MiniMarkdown.visibleSpaces(current.expectedShape(), false), "shape-text"));
        shape.getStyleClass().add("shape-box");
        detail.getChildren().setAll(title, goal, shape);
        if (!current.instructions().isBlank()) {
            detail.getChildren().add(moreDetail(current.instructions()));
        }
        if (record.isSolved(lesson.id(), current.id()) && !current.afterText().isBlank()) {
            VBox after = MiniMarkdown.render(current.afterText());
            after.getStyleClass().add("after-text");
            detail.getChildren().add(after);
        }
    }

    private Node moreDetail(String markdown) {
        Label chevron = new Label(moreOpen ? "▾" : "▸");
        chevron.getStyleClass().add("more-chevron");
        Label label = new Label("完整說明");
        label.getStyleClass().add("more-label");
        HBox toggle = new HBox(8, chevron, label);
        toggle.setAlignment(Pos.CENTER_LEFT);
        toggle.getStyleClass().addAll("more-toggle", "pressable");
        VBox body = MiniMarkdown.render(markdown);
        body.getStyleClass().add("more-body");
        body.managedProperty().bind(body.visibleProperty());
        body.setVisible(moreOpen);
        toggle.setOnMouseClicked(e -> {
            moreOpen = !moreOpen;
            body.setVisible(moreOpen);
            chevron.setText(moreOpen ? "▾" : "▸");
            if (moreOpen) {
                Motion.rise(body, Duration.ZERO);
            }
        });
        return new VBox(8, toggle, body);
    }

    private void run() {
        if (busy) {
            return;
        }
        List<SourceFile> files = editors.files();
        Step step = current;
        List<String> probes = step.check().type() == Check.Mode.VALUES ? step.check().exprs() : List.of();
        setBusy(true);
        game.sfx.play(Sfx.Sound.RUN);
        game.background(() -> step.form() == Step.Form.PROGRAM
                ? game.sandbox.runProgram(files, true, probes)
                : game.sandbox.run(Verifier.preamble(lesson.vars(), Map.of()), files.getFirst().text(), probes), r -> {
            showRun(r);
            if (r.outcome() == Outcome.OK) {
                game.sfx.play(Sfx.Sound.DING);
                precheck(step, files);
            } else {
                failed(r.outcome());
                Motion.shake(consoleScroll, 6);
            }
        }, () -> setBusy(false));
    }

    /** 編譯錯誤是喜劇感的「咚」，執行到一半跌倒是故障聲，各配一句搭檔的反應。 */
    private void failed(Outcome outcome) {
        if (outcome == Outcome.COMPILE_ERROR) {
            game.sfx.play(Sfx.Sound.BONK);
            react(Reactions.Kind.COMPILE);
        } else if (outcome == Outcome.RUNTIME_ERROR || outcome == Outcome.TIMEOUT) {
            game.sfx.play(Sfx.Sound.GLITCH);
            react(Reactions.Kind.RUNTIME);
        }
    }

    /** 執行成功後在背景先驗證一次，對了就讓「完成」閃動提醒。 */
    private void precheck(Step step, List<SourceFile> files) {
        game.background(() -> game.verifier.verify(lesson.key(step), step.form(), lesson.vars(), step.check(), step.solution(), files), v -> {
            if (v.passed() && step == current && files.equals(editors.files())
                    && !record.isSolved(lesson.id(), step.id())) {
                startNudge();
            }
        }, () -> {});
    }

    private void verify() {
        if (busy) {
            return;
        }
        stopNudge();
        List<SourceFile> files = editors.files();
        Step step = current;
        setBusy(true);
        game.sfx.play(Sfx.Sound.RUN);
        game.background(() -> game.verifier.verify(lesson.key(step), step.form(), lesson.vars(), step.check(), step.solution(), files), v -> {
            showRun(v.run());
            if (v.passed()) {
                solved(step, files);
            } else {
                Node card = verdictCard(v);
                console.getChildren().addFirst(card);
                consoleScroll.setVvalue(0);
                if (v.run().outcome() == Outcome.OK) {
                    game.sfx.play(Sfx.Sound.HUH);
                    react(Reactions.Kind.WRONG);
                } else {
                    failed(v.run().outcome());
                }
                Motion.shake(card, 8);
            }
        }, () -> setBusy(false));
    }

    private void solved(Step step, List<SourceFile> files) {
        boolean firstTime = !record.isSolved(lesson.id(), step.id());
        record.markSolved(lesson.id(), step.id());
        saveFiles(step, files);

        VBox card = new VBox(8);
        if (!step.afterText().isBlank()) {
            card.getChildren().add(MiniMarkdown.inline(step.afterText()));
        }
        card.getStyleClass().addAll("card", "success");
        int i = lesson.steps().indexOf(step);
        if (isDone()) {
            // 筆記算劇情的進度
            if (!practice) {
                game.progress.notes().addAll(lesson.notes());
            }
            game.sfx.play(Sfx.Sound.FANFARE);
            react(Reactions.Kind.DONE);
            showContinue(true);
        } else {
            game.sfx.play(Sfx.Sound.DING);
            react(Reactions.Kind.STEP);
            if (i + 1 < lesson.steps().size()) {
                Step next = lesson.steps().get(i + 1);
                Button go = new Button("下一步：" + next.title() + "　→");
                go.getStyleClass().add("btn-primary");
                go.setOnAction(e -> open(next));
                card.getChildren().add(go);
            }
        }
        game.save();
        if (!card.getChildren().isEmpty()) {
            console.getChildren().addFirst(card);
            consoleScroll.setVvalue(0);
        }
        if (firstTime) {
            justSolved = step;
        }
        refreshSteps();
        refreshDetail();
    }

    /** 全部步驟都過了：右上角出現「繼續」，回到劇情或練習的終端機。 */
    private void showContinue(boolean animate) {
        continueButton.setVisible(true);
        if (animate) {
            Motion.pop(continueButton);
            game.sfx.play(Sfx.Sound.UNLOCK);
        }
        Motion.pulse(continueButton);
    }

    private void showRun(RunResult r) {
        List<Node> nodes = new ArrayList<>();
        for (Problem p : r.problems()) {
            nodes.add(problemCard(p, r.outcome()));
        }
        if (r.output().isEmpty()) {
            if (r.outcome() == Outcome.OK && r.probes().isEmpty()) {
                Label empty = new Label("（沒有輸出）");
                empty.getStyleClass().add("console-empty");
                nodes.add(empty);
            }
        } else {
            if (!nodes.isEmpty()) {
                nodes.add(new Separator());
            }
            Label out = new Label(r.output().stripTrailing());
            out.getStyleClass().add("console-out");
            out.setWrapText(true);
            nodes.add(out);
            if (r.truncated()) {
                nodes.add(muted("……輸出太多，只顯示前面一部分。"));
            }
        }
        if (!r.probes().isEmpty()) {
            if (!nodes.isEmpty()) {
                nodes.add(new Separator());
            }
            for (Probe p : r.probes()) {
                Label line = new Label(p.error() == null
                        ? p.expr() + " = " + p.value() + "　（" + p.type() + "）"
                        : p.expr() + "：（還沒有這個變數）");
                line.getStyleClass().add("probe-line");
                nodes.add(line);
            }
        }
        console.getChildren().setAll(nodes);
        consoleScroll.setVvalue(0);
        if (r.outcome() == Outcome.OK) {
            console.getStyleClass().add("ran-ok");
            PauseTransition clear = new PauseTransition(Duration.millis(500));
            clear.setOnFinished(e -> console.getStyleClass().remove("ran-ok"));
            clear.play();
        }
        editors.showProblems(r.problems());
    }

    private Node problemCard(Problem p, Outcome outcome) {
        boolean warning = p.severity() == Severity.WARNING;
        String title = warning ? "提醒" : switch (outcome) {
            case COMPILE_ERROR -> "編譯錯誤";
            case RUNTIME_ERROR -> "執行錯誤";
            case TIMEOUT -> "執行太久";
            case STOPPED -> "已停止";
            case EXITED -> "執行環境重新啟動";
            case OK -> "錯誤";
        };
        String where = (p.file() == null ? "" : p.file() + " ") + "第 " + (editors.lineOf(p.file(), p.start()) + 1) + " 行";
        Label head = new Label(title + "　" + where);
        head.getStyleClass().add("card-title");
        Label raw = new Label(p.raw());
        raw.getStyleClass().add("raw");
        raw.setWrapText(true);
        raw.setVisible(false);
        raw.setManaged(false);
        Hyperlink toggle = new Hyperlink("原文");
        toggle.setOnAction(e -> {
            boolean show = !raw.isVisible();
            raw.setVisible(show);
            raw.setManaged(show);
            toggle.setText(show ? "收起" : "原文");
        });
        VBox card = new VBox(6, head, MiniMarkdown.inline(p.message()), toggle, raw);
        card.getStyleClass().addAll("card", warning ? "warning" : "error");
        card.setOnMouseClicked(e -> editors.jumpTo(p.file(), p.start()));
        return card;
    }

    private Node verdictCard(Verdict v) {
        Label head = new Label("還不對");
        head.getStyleClass().add("card-title");
        VBox card = new VBox(6, head, MiniMarkdown.inline(v.message()));
        card.getStyleClass().addAll("card", "error");
        return card;
    }

    private void setBusy(boolean b) {
        busy = b;
        runButton.setDisable(b);
        verifyButton.setDisable(b);
        resetButton.setDisable(b);
        // 結果晚一步才回來：先離開的話，過關音效會在終端機上響，終端機也補不到打勾那行
        backButton.setDisable(b);
        if (b) {
            stopReveal.playFromStart();
        } else {
            stopReveal.stop();
            stopButton.setVisible(false);
        }
    }

    private void startNudge() {
        if (nudge != null) {
            return;
        }
        verifyButton.getStyleClass().add("ready");
        nudge = Motion.pulse(verifyButton);
    }

    private void stopNudge() {
        if (nudge != null) {
            nudge.stop();
            nudge = null;
        }
        verifyButton.getStyleClass().remove("ready");
    }

    private static Label muted(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("muted");
        l.setWrapText(true);
        return l;
    }
}
