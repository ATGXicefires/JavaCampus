package campus.ui;

import campus.art.BackdropView;
import campus.art.FigureView;
import campus.art.Figures;
import campus.fx.Motion;
import campus.fx.Music;
import campus.fx.Sfx;
import campus.progress.Progress;
import campus.story.Cast;
import campus.story.Chapter;
import campus.story.Op;
import campus.story.Route;
import campus.story.ScriptParser;
import campus.story.Staging;
import campus.story.StoryRunner;
import campus.story.StoryState;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

/**
 * 劇情畫面。由下往上疊：背景、角色、濾鏡與特效、筆記本對話框和便利貼選項、手機、快捷選單。
 * 播放邏輯在 {@link StoryRunner}，這裡只負責把每一行演出來，玩家看完再交回去。
 */
final class StoryView extends StackPane implements Game.KeyAware, StoryRunner.Stage {

    private static final Map<String, Double> SLOTS = Map.of("左", 0.24, "中", 0.5, "右", 0.76);
    private static final double CHAR_MS = 34;
    private static final String[] NUMERALS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
    /** 筆記本和快捷選單分在兩層，用同樣的寬度和邊距，選單才會剛好貼在筆記本右下角。 */
    private static final double PAGE_W = 1180;
    private static final Insets PAGE_MARGIN = new Insets(0, 48, 34, 48);
    private static final double NOTE = 220;
    /** 便利貼輪流往左右歪一點。 */
    private static final double[] TILTS = {-1.8, 1.4, -1.2, 1.7};
    /** {@link Motion#hoverLift} 浮起的高度。 */
    private static final double LIFT = 6;
    /** 觸控板滑一下會送出一連串小幅度的滾動，要隔一小段時間才再往下一句。 */
    private static final long WHEEL_GAP_MS = 150;

    private final Game game;
    private final Route route;
    private final Chapter chapter;
    private final StoryState state;

    private final Pane backdrops = new Pane();
    private final Region sunset = new Region();
    private final ActorLayer actors = new ActorLayer();
    private final StackPane world = new StackPane();
    private final Region vignette = new Region();
    private final Region flash = new Region();
    private final Region black = new Region();

    private final NotebookPage dialog = new NotebookPage();
    private final HBox choices = new HBox(36);
    private final PhonePanel phone;
    private final HBox callBar = new HBox(14);
    private final Label callTime = new Label();
    private final Pane floaters = new Pane();
    private final ToggleButton autoButton = new ToggleButton("自動");
    private final ToggleButton skipButton = new ToggleButton("快進");
    private final List<BacklogView.Entry> backlog = new ArrayList<>();
    private final Map<String, FigureView> figures = new LinkedHashMap<>();
    private final List<Node> hideable;

    private BackdropView backdrop;
    private StoryRunner runner;
    private Runnable pending;
    private IntConsumer pick;
    private Animation typing;
    private PauseTransition autoTimer;
    private Timeline callClock;
    private boolean ctrlHeld;
    private boolean blackout;
    private boolean lineRead;
    private boolean uiHidden;
    private boolean swallowClick;
    private Timeline uiFade;
    private long lastWheel;

    StoryView(Game game, Route route, Chapter chapter, StoryState state) {
        this.game = game;
        this.route = route;
        this.chapter = chapter;
        this.state = state;
        getStyleClass().add("story");
        phone = new PhonePanel(game, route);

        sunset.getStyleClass().add("sunset-filter");
        sunset.setMouseTransparent(true);
        sunset.setOpacity(0);
        world.getChildren().addAll(backdrops, sunset, actors);

        vignette.getStyleClass().add("vignette");
        flash.getStyleClass().add("flash");
        black.getStyleClass().add("blackout");
        for (Region r : List.of(vignette, flash, black)) {
            r.setOpacity(0);
            r.setMouseTransparent(true);
        }

        Node page = dialogLayer();
        Node notes = choicesLayer();
        Node quick = quickMenu();
        hideable = List.of(page, notes, quick);
        getChildren().addAll(world, vignette, flash, page, notes, phone, callLayer(), floaters, quick, black);
        floaters.setMouseTransparent(true);
        phone.setVisible(false);
        choices.setVisible(false);
        dialog.setVisible(false);

        // 介面收起時，按下去那一下只負責叫回介面；放開後的點擊也要吃掉，不然會順便往下一句
        addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            swallowClick = uiHidden;
            if (uiHidden) {
                e.consume();
                hideUi(false);
            }
        });
        addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (swallowClick) {
                e.consume();
            }
        });
        addEventFilter(ScrollEvent.SCROLL, e -> {
            if (uiHidden) {
                e.consume();
                hideUi(false);
            }
        });
        setOnMouseClicked(this::onClick);
        setOnScroll(this::onScroll);
        rebuildStage();
        sceneProperty().addListener((obs, was, now) -> {
            if (now == null) {
                stopTimers();
            }
        });
        if (state.atStart(chapter, route)) {
            showTitleCard(this::start);
        } else {
            // 建構時還沒換上畫面；讀檔停在程式題或文件時，馬上開始會被外層的換畫面蓋掉
            Platform.runLater(this::start);
        }
    }

    private void start() {
        if (runner == null) {
            runner = new StoryRunner(chapter, route, state, game.progress.read(), this);
            runner.start();
        }
    }

    private Node dialogLayer() {
        StackPane layer = new StackPane(dialog);
        StackPane.setAlignment(dialog, Pos.BOTTOM_CENTER);
        layer.setPadding(PAGE_MARGIN);
        layer.setPickOnBounds(false);
        dialog.setMaxWidth(PAGE_W);
        return layer;
    }

    private Node choicesLayer() {
        choices.setAlignment(Pos.CENTER);
        choices.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        choices.setPickOnBounds(false);
        StackPane layer = new StackPane(choices);
        // 擺在筆記本上方那塊空間的正中間，不蓋住玩家正在回答的那句台詞
        layer.setPadding(new Insets(0, 0, 250, 0));
        layer.setPickOnBounds(false);
        return layer;
    }

    private Node callLayer() {
        Label who = new Label();
        who.getStyleClass().add("call-name");
        callTime.getStyleClass().add("call-time");
        callBar.getChildren().addAll(who, callTime);
        callBar.getStyleClass().add("call-bar");
        callBar.setAlignment(Pos.CENTER);
        callBar.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        callBar.setVisible(false);
        StackPane layer = new StackPane(callBar);
        StackPane.setAlignment(callBar, Pos.TOP_CENTER);
        layer.setPadding(new Insets(28, 0, 0, 0));
        layer.setPickOnBounds(false);
        return layer;
    }

    /** 快捷選單有自己一層，手機聊天把筆記本收起來時也還按得到。 */
    private Node quickMenu() {
        autoButton.setOnAction(e -> {
            if (autoButton.isSelected()) {
                skipButton.setSelected(false);
                if (pending != null && typing == null) {
                    scheduleAuto();
                }
            } else {
                cancelAuto();
            }
        });
        skipButton.setOnAction(e -> {
            if (skipButton.isSelected()) {
                autoButton.setSelected(false);
                skipNow();
            }
        });
        autoButton.selectedProperty().addListener((obs, was, on) -> refreshMark());
        skipButton.selectedProperty().addListener((obs, was, on) -> refreshMark());
        HBox menu = new HBox(autoButton, skipButton, quick("紀錄", this::openBacklog), quick("隱藏", () -> hideUi(true)),
                quick("筆記", this::openHandbook), quick("設定", game::openSettings));
        for (Node n : menu.getChildren()) {
            n.getStyleClass().add("quick");
            n.setFocusTraversable(false);
        }
        menu.getStyleClass().addAll("quick-menu", "no-advance");
        menu.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        // 跨在筆記本下緣，一半壓在紙上、一半露在外面
        menu.translateYProperty().bind(menu.heightProperty().divide(2));
        StackPane frame = new StackPane(menu);
        frame.setMaxWidth(PAGE_W);
        frame.setPickOnBounds(false);
        StackPane.setAlignment(menu, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(menu, new Insets(0, 26, 0, 0));
        StackPane layer = new StackPane(frame);
        layer.setPadding(PAGE_MARGIN);
        layer.setPickOnBounds(false);
        return layer;
    }

    private static Button quick(String label, Runnable action) {
        Button b = new Button(label);
        b.setOnAction(e -> action.run());
        return b;
    }

    /** 收起筆記本、選單和選項，讓玩家看整個畫面；之後隨便按一下就叫回來，不會順便往下走。 */
    private void hideUi(boolean hide) {
        if (hide == uiHidden) {
            return;
        }
        uiHidden = hide;
        if (hide) {
            skipButton.setSelected(false);
            cancelAuto();
            if (typing != null) {
                typing.jumpTo("end");
            }
        } else if (pending != null && typing == null) {
            lineShown();
        }
        if (uiFade != null) {
            uiFade.stop();
        }
        if (Motion.isOff()) {
            for (Node n : hideable) {
                n.setOpacity(1);
                n.setVisible(!hide);
            }
            return;
        }
        List<KeyValue> values = new ArrayList<>();
        for (Node n : hideable) {
            n.setVisible(true);
            values.add(new KeyValue(n.opacityProperty(), hide ? 0 : 1, Interpolator.EASE_OUT));
        }
        uiFade = new Timeline(new KeyFrame(Duration.millis(hide ? 150 : 220), values.toArray(KeyValue[]::new)));
        uiFade.setOnFinished(e -> hideable.forEach(n -> n.setVisible(!hide)));
        uiFade.play();
    }

    /** 讀檔或從程式題回來時，照存檔裡的舞台狀態直接擺好，不播進場動畫。 */
    private void rebuildStage() {
        Staging st = state.staging();
        if (st.background() != null) {
            setBackground(st.background(), st.variant(), false);
        }
        for (Map.Entry<String, Staging.Actor> e : st.actors().entrySet()) {
            FigureView f = new FigureView(game.art, e.getKey(), e.getValue().expression());
            figures.put(e.getKey(), f);
            actors.place(f, e.getValue().position());
        }
        applyFilters(false);
        playMusic(st.music());
        applyAmbience();
        if (st.phone() != null) {
            phone.open(st.phone());
            showPhone(true);
        }
        if (st.call() != null) {
            startCall(st.call());
        }
    }

    void resumeAfterLesson() {
        requestFocus();
    }

    @Override
    public void line(Op.Line l, boolean read, Runnable next) {
        cancelAuto();
        hideUi(false);
        lineRead = read;
        pending = next;
        backlog.add(new BacklogView.Entry(l.kind(), l.who(), l.text()));
        if (blackout) {
            blackout = false;
            Motion.fade(black, 0, 350, null);
        }
        String personId = l.who() == null ? null : Cast.find(l.who()).map(Cast.Person::id).orElse(null);
        highlight(l.kind() == Op.Kind.SAY ? personId : null);
        if (state.staging().phone() != null && l.kind() == Op.Kind.SAY) {
            dialog.setVisible(false);
            boolean mine = route.self.equals(personId);
            phone.message(l.who(), l.text(), mine, skipping() || instant(), this::lineShown);
            return;
        }
        showDialog(l, personId);
        refreshMark();
    }

    private void showDialog(Op.Line l, String personId) {
        dialog.setVisible(true);
        dialog.getStyleClass().removeAll("os", "narration", "direction");
        boolean speaking = l.kind() == Op.Kind.SAY;
        dialog.speaker(speaking ? l.who() : null);
        dialog.action.setVisible(l.action() != null);
        dialog.action.setText(l.action() == null ? "" : "（" + l.action() + "）");
        String shown = l.text();
        switch (l.kind()) {
            case OS -> {
                dialog.getStyleClass().add("os");
                shown = "（" + l.text() + "）";
            }
            case NARRATION -> dialog.getStyleClass().add("narration");
            case DIRECTION -> dialog.getStyleClass().add("direction");
            default -> {
            }
        }
        if (speaking && shown.isEmpty() && l.action() != null) {
            dialog.action.setVisible(false);
            shown = "（" + l.action() + "）";
        }
        if (skipping() || instant()) {
            dialog.text.setText(shown);
            lineShown();
            return;
        }
        Sfx.Voice voice = speaking ? Sfx.Voice.of(personId) : null;
        double factor = game.progress.settings().textSpeed().factor;
        typing = Motion.typewriter(dialog.text, shown, Duration.millis(CHAR_MS * factor), n -> {
            if (voice != null && n % voice.every == 1) {
                game.sfx.blip(voice);
            }
        }, () -> {
            typing = null;
            lineShown();
        });
    }

    private boolean instant() {
        return game.progress.settings().textSpeed() == Progress.TextSpeed.INSTANT;
    }

    /** 一行顯示完了：快進時馬上往下，自動播放時等一下再往下；介面收起時先停著，叫回來再接著走。 */
    private void lineShown() {
        refreshMark();
        if (uiHidden) {
            return;
        }
        if (skipping() && (lineRead || game.progress.settings().skipAll())) {
            PauseTransition p = new PauseTransition(Duration.millis(45));
            p.setOnFinished(e -> {
                if (skipping() && !uiHidden) {
                    advance();
                }
            });
            p.play();
        } else if (skipping()) {
            skipButton.setSelected(false);
        } else if (autoButton.isSelected()) {
            scheduleAuto();
        }
    }

    private void scheduleAuto() {
        cancelAuto();
        double seconds = game.progress.settings().autoDelay().seconds + dialog.text.getText().length() * 0.03;
        autoTimer = new PauseTransition(Duration.seconds(seconds));
        autoTimer.setOnFinished(e -> {
            if (autoButton.isSelected()) {
                advance();
            }
        });
        autoTimer.play();
    }

    private void cancelAuto() {
        if (autoTimer != null) {
            autoTimer.stop();
            autoTimer = null;
        }
    }

    private boolean skipping() {
        return skipButton.isSelected() || ctrlHeld;
    }

    /** 整句出現、等玩家往下時，筆記本右下角才出現記號；自動和快進各換一種。 */
    private void refreshMark() {
        if (pending == null || typing != null) {
            dialog.showMark(null);
        } else {
            dialog.showMark(skipping() ? NotebookPage.Mark.SKIP
                    : autoButton.isSelected() ? NotebookPage.Mark.AUTO : NotebookPage.Mark.NEXT);
        }
    }

    private void skipNow() {
        if (typing != null) {
            typing.jumpTo("end");
        } else if (pending != null) {
            lineShown();
        }
    }

    /** 點畫面、空白鍵、Enter：字還在跑就先整句顯示，已經顯示完就換下一句。 */
    private void advance() {
        if (typing != null) {
            typing.jumpTo("end");
            return;
        }
        if (pending != null) {
            cancelAuto();
            Runnable p = pending;
            pending = null;
            p.run();
            refreshMark();
        }
    }

    private void onClick(MouseEvent e) {
        if (fromControl(e)) {
            return;
        }
        if (e.getButton() == MouseButton.PRIMARY) {
            advance();
        } else if (e.getButton() == MouseButton.SECONDARY) {
            hideUi(true);
        }
    }

    /** 滾輪往上看紀錄，往下前進一句。 */
    private void onScroll(ScrollEvent e) {
        if (e.getDeltaY() > 0) {
            openBacklog();
        } else if (e.getDeltaY() < 0 && pick == null && !e.isInertia()) {
            long now = System.currentTimeMillis();
            if (now - lastWheel >= WHEEL_GAP_MS) {
                lastWheel = now;
                advance();
            }
        }
    }

    private static boolean fromControl(MouseEvent e) {
        for (Node n = e.getTarget() instanceof Node t ? t : null; n != null; n = n.getParent()) {
            if (n instanceof ButtonBase || n.getStyleClass().contains("no-advance")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onKey(KeyEvent e) {
        KeyCode code = e.getCode();
        if (e.getEventType() == KeyEvent.KEY_RELEASED) {
            if (code == KeyCode.CONTROL) {
                ctrlHeld = false;
                refreshMark();
            }
            return;
        }
        if (uiHidden) {
            // 按住 Ctrl 時系統會一直重送按下事件，那不算玩家又按了一個鍵
            if (code == KeyCode.CONTROL && ctrlHeld) {
                return;
            }
            hideUi(false);
            e.consume();
            return;
        }
        switch (code) {
            case SPACE, ENTER -> {
                if (pick == null) {
                    advance();
                }
            }
            case CONTROL -> {
                if (!ctrlHeld) {
                    ctrlHeld = true;
                    skipNow();
                }
            }
            case A -> autoButton.fire();
            case L -> openBacklog();
            case H -> hideUi(true);
            case N -> openHandbook();
            case DIGIT1, DIGIT2, DIGIT3, DIGIT4, NUMPAD1, NUMPAD2, NUMPAD3, NUMPAD4 -> {
                int i = code.getName().charAt(code.getName().length() - 1) - '1';
                if (pick != null && i < choices.getChildren().size()) {
                    ((Button) choices.getChildren().get(i)).fire();
                }
            }
            default -> {
                return;
            }
        }
        e.consume();
    }

    @Override
    public void menu(Op.Menu menu, IntConsumer onPick) {
        cancelAuto();
        hideUi(false);
        skipButton.setSelected(false);
        if (blackout) {
            blackout = false;
            Motion.fade(black, 0, 350, null);
        }
        choices.getChildren().clear();
        pick = onPick;
        for (int i = 0; i < menu.options().size(); i++) {
            int index = i;
            Button b = new Button(menu.options().get(i).text());
            b.getStyleClass().addAll("choice", "note-" + i % TILTS.length);
            b.setPrefSize(NOTE, NOTE);
            b.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            b.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            b.setWrapText(true);
            double tilt = TILTS[i % TILTS.length];
            // 角度跟著 hoverLift 的高度走：浮起來時剛好轉正，進場從下方彈上來時歪得更多，像被拍上去
            b.rotateProperty().bind(b.translateYProperty().multiply(tilt / LIFT).add(tilt));
            b.setOnAction(e -> choose(index));
            Motion.hoverLift(b);
            choices.getChildren().add(b);
            Motion.rise(b, Duration.millis(70 * i));
        }
        choices.setVisible(true);
        game.sfx.play(Sfx.Sound.CHOICE_IN);
    }

    private void choose(int index) {
        if (pick == null) {
            return;
        }
        IntConsumer p = pick;
        pick = null;
        game.sfx.play(Sfx.Sound.CHOICE_OK);
        choices.setVisible(false);
        choices.getChildren().clear();
        p.accept(index);
    }

    @Override
    public void chose(Op.Option option) {
        backlog.add(BacklogView.Entry.choice(option.text()));
        double x = getWidth() - 150;
        double y = getHeight() - 300;
        if (option.ap() > 0) {
            game.sfx.play(Sfx.Sound.AP);
            floatIcon(Icons.heart(size(option.ap())), x, y);
        }
        if (option.tp() > 0) {
            PauseTransition later = new PauseTransition(Duration.millis(option.ap() > 0 ? 220 : 0));
            later.setOnFinished(e -> {
                game.sfx.play(Sfx.Sound.TP);
                floatIcon(Icons.lock(size(option.tp())), x + 64, y + 10);
            });
            later.play();
        }
    }

    /** 加得越多圖示越大，分三種大小；數字本身不顯示。 */
    private static double size(int amount) {
        return amount <= 5 ? 34 : amount <= 10 ? 46 : 60;
    }

    private void floatIcon(Node icon, double x, double y) {
        icon.relocate(x, y);
        floaters.getChildren().add(icon);
        Motion.floatAway(icon, () -> floaters.getChildren().remove(icon));
    }

    @Override
    public void command(Op.Cmd c, Runnable next) {
        switch (c.name()) {
            case "背景" -> {
                setBackground(c.arg(0), c.arg(1), !blackout);
                next.run();
            }
            case "登場" -> {
                enter(c.arg(0), c.arg(1), c.arg(2) != null ? c.arg(2) : "一般");
                next.run();
            }
            case "表情" -> {
                FigureView f = figures.get(c.arg(0));
                if (f != null) {
                    f.setExpression(c.arg(1));
                }
                next.run();
            }
            case "退場" -> {
                exit(c.arg(0));
                next.run();
            }
            case "音樂" -> {
                playMusic(state.staging().music());
                next.run();
            }
            case "環境" -> {
                applyAmbience();
                next.run();
            }
            case "音效" -> {
                Sfx.Sound.byScriptName(c.arg(0)).ifPresent(game.sfx::play);
                next.run();
            }
            case "特效" -> {
                effect(c.arg(0));
                next.run();
            }
            case "暗場" -> fadeToBlack(next);
            case "等待" -> {
                double seconds = skipping() ? 0.05 : Double.parseDouble(c.arg(0));
                PauseTransition p = new PauseTransition(Duration.seconds(seconds));
                p.setOnFinished(e -> next.run());
                p.play();
            }
            case "手機" -> {
                if (state.staging().phone() != null) {
                    phone.open(state.staging().phone());
                    dialog.setVisible(false);
                    showPhone(true);
                } else {
                    showPhone(false);
                }
                next.run();
            }
            case "附件" -> {
                String docId = c.arg(1);
                phone.attachment(c.arg(0), skipping(), () -> openDocument(docId, null));
                next.run();
            }
            case "電話" -> {
                if (state.staging().call() != null) {
                    game.sfx.play(Sfx.Sound.CONNECT);
                    startCall(state.staging().call());
                } else {
                    stopCall();
                }
                next.run();
            }
            case "文件" -> {
                skipButton.setSelected(false);
                cancelAuto();
                openDocument(c.arg(0), next);
            }
            case "程式" -> {
                skipButton.setSelected(false);
                autoButton.setSelected(false);
                cancelAuto();
                game.showLesson(c.arg(0), this, next);
            }
            default -> next.run();
        }
    }

    @Override
    public void chapterEnd(String note) {
        stopTimers();
        game.showChapterEnd(route);
    }

    @Override
    public void checkpoint() {
        game.save();
    }

    private void setBackground(String name, String variant, boolean animate) {
        if (backdrop != null && backdrop.sameAs(name, variant)) {
            return;
        }
        BackdropView next = new BackdropView(game.art, name, variant);
        next.prefWidthProperty().bind(backdrops.widthProperty());
        next.prefHeightProperty().bind(backdrops.heightProperty());
        next.setRain(state.staging().ambient().contains("雨"));
        BackdropView old = backdrop;
        backdrop = next;
        backdrops.getChildren().add(next);
        if (old == null) {
            return;
        }
        if (animate) {
            next.setOpacity(0);
            Motion.fade(next, 1, 420, () -> backdrops.getChildren().remove(old));
        } else {
            backdrops.getChildren().remove(old);
        }
    }

    private void enter(String person, String position, String expression) {
        FigureView f = figures.get(person);
        if (f != null) {
            double from = actors.x(f);
            actors.place(f, position);
            actors.layout();
            Motion.slideFrom(f, from - actors.x(f));
            f.setExpression(expression);
            return;
        }
        f = new FigureView(game.art, person, expression);
        figures.put(person, f);
        actors.place(f, position);
        Motion.enter(f);
    }

    private void exit(String person) {
        List<String> who = person.equals("全部") ? new ArrayList<>(figures.keySet()) : List.of(person);
        for (String p : who) {
            FigureView f = figures.remove(p);
            if (f != null) {
                Motion.exit(f, () -> actors.getChildren().remove(f));
            }
        }
    }

    /** 說話的人亮一點、跳一下，其他人退後一點。 */
    private void highlight(String speaker) {
        for (FigureView f : figures.values()) {
            boolean talking = f.person().equals(speaker);
            // 剪影調透明度會透出背後的螢幕和桌子，所以改成壓暗：黑色不變，只有描邊和眼鏡變暗
            f.setEffect(speaker == null || talking ? null : new ColorAdjust(0, -0.2, -0.45, 0));
            if (talking) {
                Motion.bob(f);
            }
        }
    }

    private void playMusic(String name) {
        if (name == null) {
            game.music.stop();
        } else {
            Music.Track.byScriptName(name).ifPresent(game.music::play);
        }
    }

    private void applyAmbience() {
        List<Sfx.Ambience> on = new ArrayList<>();
        for (String a : state.staging().ambient()) {
            Sfx.Ambience.byScriptName(a).ifPresent(on::add);
        }
        game.sfx.setAmbience(on);
        if (backdrop != null) {
            backdrop.setRain(state.staging().ambient().contains("雨"));
        }
    }

    private void effect(String name) {
        switch (name) {
            case "震動" -> {
                game.sfx.play(Sfx.Sound.SLAP);
                Motion.shake(world, 16);
            }
            case "閃白" -> Motion.flash(flash);
            case "心跳" -> {
                game.sfx.play(Sfx.Sound.HEARTBEAT);
                Motion.heartbeat(vignette);
            }
            default -> applyFilters(true);
        }
    }

    private void applyFilters(boolean animate) {
        boolean warm = state.staging().filters().contains("夕陽");
        Motion.fade(sunset, warm ? 1 : 0, animate ? 900 : 0, null);
        world.setEffect(state.staging().filters().contains("回憶") ? new ColorAdjust(0, -0.75, -0.05, 0) : null);
    }

    private void fadeToBlack(Runnable next) {
        blackout = true;
        if (skipping()) {
            black.setOpacity(1);
            next.run();
            return;
        }
        Motion.fade(black, 1, 380, next);
    }

    private void showPhone(boolean on) {
        if (on == phone.isVisible()) {
            return;
        }
        phone.setVisible(on);
        if (on) {
            Motion.enter(phone);
        }
    }

    private void startCall(String who) {
        ((Label) callBar.getChildren().getFirst()).setText(who);
        callBar.setVisible(true);
        int[] seconds = {0};
        callTime.setText("00:00");
        if (callClock != null) {
            callClock.stop();
        }
        callClock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            seconds[0]++;
            callTime.setText(String.format("%02d:%02d", seconds[0] / 60, seconds[0] % 60));
        }));
        callClock.setCycleCount(Animation.INDEFINITE);
        callClock.play();
        Motion.pop(callBar);
    }

    private void stopCall() {
        if (callClock != null) {
            callClock.stop();
            callClock = null;
        }
        callBar.setVisible(false);
    }

    private void stopTimers() {
        cancelAuto();
        if (callClock != null) {
            callClock.stop();
        }
    }

    private void openDocument(String docId, Runnable onClosed) {
        game.sfx.play(Sfx.Sound.PAPER);
        game.openOverlay(new DocumentView(game, game.story.docs().get(docId)), onClosed);
    }

    private void openBacklog() {
        game.openOverlay(new BacklogView(backlog), null);
    }

    private void openHandbook() {
        game.sfx.play(Sfx.Sound.PAPER);
        game.openOverlay(new HandbookView(game), null);
    }

    /** 章節標題卡：先打出編譯錯誤樣式的一行，閃一下故障，再變成正式的章名。點一下可以跳過。 */
    private void showTitleCard(Runnable then) {
        StackPane card = new StackPane();
        card.getStyleClass().addAll("title-card", "no-advance");
        Label error = new Label();
        error.getStyleClass().add("title-card-error");
        Label number = new Label("第" + numeral(chapter.number()) + "章");
        number.getStyleClass().add("title-card-number");
        Label name = new Label(chapter.title());
        name.getStyleClass().add("title-card-name");
        VBox real = new VBox(10, number, name);
        real.setAlignment(Pos.CENTER);
        real.setOpacity(0);
        card.getChildren().addAll(error, real);
        getChildren().add(card);
        boolean[] done = {false};
        Runnable finish = () -> {
            if (done[0]) {
                return;
            }
            done[0] = true;
            Motion.fade(card, 0, 400, () -> getChildren().remove(card));
            then.run();
        };
        // 用按下而不是點擊：按「重玩」那一下放開時產生的點擊事件，會落到剛蓋上來的標題卡上，一出現就被跳過
        card.setOnMousePressed(e -> finish.run());
        String line = "error: 第" + numeral(chapter.number()) + "章「" + chapter.title() + "」";
        if (Motion.isOff()) {
            real.setOpacity(1);
            PauseTransition hold = new PauseTransition(Duration.seconds(1.4));
            hold.setOnFinished(e -> finish.run());
            hold.play();
            return;
        }
        game.sfx.play(Sfx.Sound.TITLE);
        Motion.typewriter(error, line, Duration.millis(38), (Runnable) null, () -> {
            PauseTransition glitch = new PauseTransition(Duration.millis(450));
            glitch.setOnFinished(e -> {
                Motion.shake(error, 10);
                Motion.fade(error, 0, 260, null);
                // rise 會淡入到呼叫當下的透明度，所以先設成 1 再交給它
                real.setOpacity(1);
                Motion.rise(real, Duration.ZERO);
                PauseTransition hold = new PauseTransition(Duration.seconds(1.6));
                hold.setOnFinished(x -> finish.run());
                hold.play();
            });
            glitch.play();
        });
    }

    private static String numeral(int n) {
        return n >= 0 && n < NUMERALS.length ? NUMERALS[n] : String.valueOf(n);
    }

    /** 角色站位：左、中、右三個位置，高度跟著畫面走，腳底對齊畫面下緣。 */
    private static final class ActorLayer extends Pane {

        private final Map<Node, String> positions = new HashMap<>();

        ActorLayer() {
            setPickOnBounds(false);
            setMouseTransparent(true);
        }

        void place(FigureView f, String position) {
            positions.put(f, ScriptParser.POSITIONS.contains(position) ? position : "中");
            if (!getChildren().contains(f)) {
                getChildren().add(f);
            }
            requestLayout();
        }

        double x(Node n) {
            return n.getLayoutX();
        }

        @Override
        protected void layoutChildren() {
            double h = getHeight() * 0.94;
            double w = h * Figures.UNIT_W / Figures.UNIT_H;
            for (Node n : getChildren()) {
                double slot = SLOTS.getOrDefault(positions.getOrDefault(n, "中"), 0.5);
                n.resizeRelocate(getWidth() * slot - w / 2, getHeight() - h + 8, w, h);
            }
        }
    }
}
