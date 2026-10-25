package campus.story;

import campus.story.ScriptFile.Scene;
import campus.story.StoryState.Pos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;

/**
 * 逐行播放劇本。數值、旗標、舞台狀態和播放位置由這裡改；怎麼顯示、什麼時候往下走由 {@link Stage} 決定。
 * 會等玩家的指令（台詞、選項、程式題⋯⋯）把「繼續」交給畫面，畫面呼叫之後才播下一行。
 */
public final class StoryRunner {

    public interface Stage {

        /** @param read 這一行以前讀過，快進「只跳已讀」時用 */
        void line(Op.Line line, boolean read, Runnable next);

        /** 選完呼叫 {@code pick}，參數是選項的索引。 */
        void menu(Op.Menu menu, IntConsumer pick);

        /** 選完之後，數值已經加上去了。 */
        void chose(Op.Option option);

        /** 舞台、音效、手機、文件、程式題等指令。舞台狀態已經先更新好了，處理完呼叫 {@code next}。 */
        void command(Op.Cmd cmd, Runnable next);

        void chapterEnd(String note);

        /** 自動存檔的時機：場景開始、做完選擇、進出程式題、章末。 */
        void checkpoint();
    }

    private final Chapter chapter;
    private final Route route;
    private final StoryState state;
    private final Set<String> read;
    private final Stage stage;
    private boolean looping;
    private boolean resume;
    /** 跳過劇情時收集路上的 {@code @簡介}；沒在跳過就是 null。 */
    private List<String> summary;

    public StoryRunner(Chapter chapter, Route route, StoryState state, Set<String> read, Stage stage) {
        this.chapter = chapter;
        this.route = route;
        this.state = state;
        this.read = read;
        this.stage = stage;
    }

    public void start() {
        resume();
    }

    /**
     * 跳過劇情：一路往下，停在下一個選項、程式題或章末前面，之後呼叫 {@link #start()} 從那裡接著播。
     * 路上的台詞不交給畫面，也不算讀過；舞台指令照樣記進舞台狀態，畫面再照它重建。
     * 要在畫面正等玩家看完一行台詞時呼叫，那一行也一起跳過。
     *
     * @return 路上經過的 {@code @簡介}
     */
    public List<String> skip() {
        summary = new ArrayList<>();
        try {
            step();
            stage.checkpoint();
            return summary;
        } finally {
            summary = null;
        }
    }

    /** 畫面的「繼續」可能在同一個呼叫裡馬上回來，用迴圈代替遞迴，一長串舞台指令才不會把堆疊撐爆。 */
    private void resume() {
        resume = true;
        if (looping) {
            return;
        }
        looping = true;
        try {
            while (resume) {
                resume = false;
                step();
            }
        } finally {
            looping = false;
        }
    }

    /** 往下執行，直到碰到要等玩家的指令。 */
    private void step() {
        while (true) {
            Pos at = state.at();
            Scene scene = scene(at);
            if (at.index() >= scene.ops().size()) {
                if (!endOfScene(at)) {
                    return;
                }
                continue;
            }
            if (at.index() == 0 && summary == null) {
                stage.checkpoint();
            }
            Op op = scene.ops().get(at.index());
            switch (op) {
                case Op.Line l -> {
                    if (summary != null) {
                        advance();
                    } else {
                        boolean wasRead = !read.add(key(at));
                        stage.line(l, wasRead, proceed());
                        return;
                    }
                }
                case Op.Menu m -> {
                    if (summary == null) {
                        stage.menu(m, i -> choose(at, m, i));
                    }
                    return;
                }
                case Op.Goto g -> state.moveTo(new Pos(at.file(), at.scene(), g.index()));
                case Op.Only o -> state.moveTo(new Pos(at.file(), at.scene(), o.route() == route ? at.index() + 1 : o.end()));
                case Op.Call c -> {
                    state.returns().add(new Pos(at.file(), at.scene(), at.index() + 1));
                    state.moveTo(new Pos(chapter.common().name(), c.scene(), 0));
                }
                case Op.Jump j -> state.moveTo(new Pos(at.file(), j.scene(), 0));
                case Op.If i -> {
                    if (i.conditions().stream().allMatch(c -> c.test(state))) {
                        state.moveTo(new Pos(at.file(), i.scene(), 0));
                    } else {
                        advance();
                    }
                }
                case Op.Cmd c -> {
                    if (command(c)) {
                        return;
                    }
                }
            }
        }
    }

    /** @return true 表示要等畫面處理完 */
    private boolean command(Op.Cmd raw) {
        if (Staging.concernsSelf(raw, route)) {
            state.staging().applySelf(raw);
            advance();
            return false;
        }
        Op.Cmd c = Staging.normalize(raw);
        switch (c.name()) {
            case "旗標" -> {
                state.flag(c.arg(0));
                advance();
                return false;
            }
            case "數值" -> {
                int[] d = ScriptParser.effect(c.args());
                state.add(d[0], d[1]);
                advance();
                return false;
            }
            case "章末" -> {
                if (summary != null) {
                    return true;
                }
                state.cleared().add(chapter.id());
                state.setAtChapterEnd(true);
                stage.checkpoint();
                stage.chapterEnd(c.arg(0));
                return true;
            }
            default -> {
                if (summary != null) {
                    if (c.name().equals("程式")) {
                        return true;
                    }
                    if (c.name().equals("簡介")) {
                        summary.add(c.arg(0));
                    }
                    state.staging().apply(c);
                    advance();
                    return false;
                }
                state.staging().apply(c);
                stage.command(c, proceed());
                return true;
            }
        }
    }

    private void choose(Pos at, Op.Menu m, int i) {
        Op.Option o = m.options().get(i);
        state.add(o.ap(), o.tp());
        if (o.key()) {
            state.key(chapter.id() + "/" + at.file() + "/" + at.scene() + "/" + at.index());
        }
        if (o.target() != null) {
            state.moveTo(new Pos(at.file(), o.target(), 0));
        } else {
            state.moveTo(new Pos(at.file(), at.scene(), o.body() >= 0 ? o.body() : m.end()));
        }
        stage.chose(o);
        stage.checkpoint();
        resume();
    }

    /** @return false 表示劇本播完了，後面沒有東西 */
    private boolean endOfScene(Pos at) {
        List<Pos> returns = state.returns();
        if (at.file().equals(chapter.common().name()) && !returns.isEmpty()) {
            state.moveTo(returns.removeLast());
            return true;
        }
        Scene next = chapter.file(at.file()).after(at.scene());
        if (next == null) {
            return false;
        }
        state.moveTo(new Pos(at.file(), next.name(), 0));
        return true;
    }

    /**
     * 等玩家的指令看完才把位置往前移，這段期間存的檔會停在這一行，讀回來不會跳過它。
     * 畫面不小心呼叫兩次也只算一次。
     */
    private Runnable proceed() {
        boolean[] used = {false};
        return () -> {
            if (!used[0]) {
                used[0] = true;
                advance();
                resume();
            }
        };
    }

    private void advance() {
        Pos at = state.at();
        state.moveTo(new Pos(at.file(), at.scene(), at.index() + 1));
    }

    private Scene scene(Pos at) {
        return chapter.file(at.file()).scene(at.scene())
                .orElseThrow(() -> new IllegalStateException(at.file() + " 沒有場景 [" + at.scene() + "]"));
    }

    /** 共通場景用檔名記，兩個視角讀過同一行都算讀過。 */
    private static String key(Pos at) {
        return at.file() + "#" + at.scene() + "#" + at.index();
    }
}
