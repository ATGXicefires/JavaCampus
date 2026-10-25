package campus.story;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 一個視角的劇情進度。用 Gson 存檔，所以欄位保持可變的一般集合。 */
public final class StoryState {

    /** 播放位置：檔名、場景名稱、場景內第幾個指令。用場景名稱而不是場景順序，劇本中途改寫也比較不會錯位。 */
    public record Pos(String file, String scene, int index) {}

    /** 每章開頭的數值，重玩那一章時還原。 */
    public record Snapshot(int ap, int tp, Set<String> flags, Set<String> keys) {}

    static final int START = 10;

    private String chapter;
    private Pos at;
    private List<Pos> returns = new ArrayList<>();
    private int ap = START;
    private int tp = START;
    private Set<String> flags = new HashSet<>();
    private Set<String> keys = new HashSet<>();
    private Staging staging = new Staging();
    private Map<String, Snapshot> chapterStarts = new HashMap<>();
    private Set<String> cleared = new LinkedHashSet<>();
    private boolean atChapterEnd;

    public String chapter() {
        return chapter;
    }

    public Pos at() {
        return at;
    }

    public int ap() {
        return ap;
    }

    public int tp() {
        return tp;
    }

    public int keyCount() {
        return keys().size();
    }

    public boolean hasFlag(String name) {
        return flags().contains(name);
    }

    public Staging staging() {
        if (staging == null) {
            staging = new Staging();
        }
        return staging;
    }

    public Set<String> cleared() {
        if (cleared == null) {
            cleared = new LinkedHashSet<>();
        }
        return cleared;
    }

    /** 播到 {@code @章末} 之後、玩家還沒按繼續之前存的檔，讀回來要直接顯示章末畫面。 */
    public boolean atChapterEnd() {
        return atChapterEnd;
    }

    void setAtChapterEnd(boolean b) {
        atChapterEnd = b;
    }

    /**
     * 從某一章開頭開始玩。從章節選擇重玩時數值還原成那一章當時開頭的樣子；
     * 正常玩到這一章時記下目前的數值，蓋掉以前的紀錄，之後的進度以這次為準。
     */
    public void begin(Chapter ch, Route route, boolean replay) {
        Snapshot start = chapterStarts().get(ch.id());
        if (replay && start != null) {
            ap = start.ap();
            tp = start.tp();
            flags = new HashSet<>(start.flags());
            keys = new HashSet<>(start.keys());
        } else {
            chapterStarts().put(ch.id(), new Snapshot(ap, tp, Set.copyOf(flags()), Set.copyOf(keys())));
        }
        chapter = ch.id();
        ScriptFile pov = ch.pov(route);
        at = new Pos(pov.name(), pov.scenes().getFirst().name(), 0);
        returns().clear();
        staging().clear();
        atChapterEnd = false;
    }

    /** 還在這一章的第一行、什麼都還沒播，劇情畫面要先顯示章節標題卡。 */
    public boolean atStart(Chapter ch, Route route) {
        ScriptFile pov = ch.pov(route);
        return at != null && at.index() == 0 && returns().isEmpty()
                && at.file().equals(pov.name()) && at.scene().equals(pov.scenes().getFirst().name());
    }

    public boolean hasStarted(String chapterId) {
        return chapterStarts().containsKey(chapterId);
    }

    void moveTo(Pos p) {
        at = p;
    }

    List<Pos> returns() {
        if (returns == null) {
            returns = new ArrayList<>();
        }
        return returns;
    }

    void add(int dAp, int dTp) {
        ap = clamp(ap + dAp);
        tp = clamp(tp + dTp);
    }

    void flag(String name) {
        flags().add(name);
    }

    void key(String id) {
        keys().add(id);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(100, v));
    }

    private Set<String> flags() {
        if (flags == null) {
            flags = new HashSet<>();
        }
        return flags;
    }

    private Set<String> keys() {
        if (keys == null) {
            keys = new HashSet<>();
        }
        return keys;
    }

    private Map<String, Snapshot> chapterStarts() {
        if (chapterStarts == null) {
            chapterStarts = new HashMap<>();
        }
        return chapterStarts;
    }

    /** 複製一份互不影響的狀態，給窮舉選項組合的測試，和對話紀錄記下每一句當時的進度。 */
    public StoryState copy() {
        StoryState s = new StoryState();
        s.chapter = chapter;
        s.at = at;
        s.returns = new ArrayList<>(returns());
        s.ap = ap;
        s.tp = tp;
        s.flags = new HashSet<>(flags());
        s.keys = new HashSet<>(keys());
        s.staging = staging().copy();
        s.chapterStarts = new HashMap<>(chapterStarts());
        s.cleared = new LinkedHashSet<>(cleared());
        s.atChapterEnd = atChapterEnd;
        return s;
    }

    /**
     * 回到用 {@link #copy()} 記下的那一刻：播放位置、數值、旗標和舞台都換回去，之後做過的選擇等於沒選。
     * 哪些章開始過、破關過不受影響。同一份紀錄可以拿來回去好幾次。
     */
    public void rewindTo(StoryState mark) {
        at = mark.at;
        returns = new ArrayList<>(mark.returns());
        ap = mark.ap;
        tp = mark.tp;
        flags = new HashSet<>(mark.flags());
        keys = new HashSet<>(mark.keys());
        staging = mark.staging().copy();
        atChapterEnd = false;
    }
}
