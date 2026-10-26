package campus.progress;

import campus.fx.Motion;
import campus.story.Route;
import campus.story.StoryState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/** 玩家進度與設定。用 Gson 存成 JSON，所以欄位保持可變的一般集合。 */
public final class Progress {

    /** 劇情打字機效果的速度，數值是每個字的時間倍率；立即就是不打字、直接顯示全文。 */
    public enum TextSpeed {
        SLOW(1.6), NORMAL(1), FAST(0.5), INSTANT(0);

        public final double factor;

        TextSpeed(double factor) {
            this.factor = factor;
        }
    }

    /** 自動播放時，一句話顯示完之後停多久才換下一句。 */
    public enum AutoDelay {
        SHORT(0.9), MEDIUM(1.8), LONG(3.0);

        public final double seconds;

        AutoDelay(double seconds) {
            this.seconds = seconds;
        }
    }

    /** 舊存檔沒有的欄位會維持這裡的預設值；列舉值讀不懂時 Gson 給 null，所以列舉一律經過預設。 */
    public static final class Settings {
        /** 遊戲附帶的程式字型，玩家的電腦上一定有。 */
        public static final String DEFAULT_CODE_FAMILY = "Maple Mono NF CN";

        private double volume = 0.7;
        private double music = 0.6;
        private double sfx = 1;
        private double ambient = 0.8;
        private double voice = 0.6;
        private boolean typing = true;
        private boolean fullscreen;
        private boolean borderless;
        private double zoom = 1;
        private double codeFont = 17;
        private String codeFamily = DEFAULT_CODE_FAMILY;
        private boolean codeDark;
        private int windowWidth;
        private int windowHeight;
        private TextSpeed textSpeed = TextSpeed.NORMAL;
        private AutoDelay autoDelay = AutoDelay.MEDIUM;
        private boolean skipAll;
        private Motion.Level motion = Motion.Level.BOUNCY;

        public double volume() {
            return volume;
        }

        public double music() {
            return music;
        }

        public double sfx() {
            return sfx;
        }

        public double ambient() {
            return ambient;
        }

        public double voice() {
            return voice;
        }

        public boolean typing() {
            return typing;
        }

        public boolean fullscreen() {
            return fullscreen;
        }

        /** 視窗不要系統的標題列和邊框；全螢幕時看不出差別，按 F11 離開全螢幕後回到這個樣子。 */
        public boolean borderless() {
            return borderless;
        }

        public double zoom() {
            return zoom;
        }

        public double codeFont() {
            return codeFont;
        }

        public String codeFamily() {
            return codeFamily == null || codeFamily.isBlank() ? DEFAULT_CODE_FAMILY : codeFamily;
        }

        /** 程式題畫面整體用暗色；預設是亮色。 */
        public boolean codeDark() {
            return codeDark;
        }

        /** 視窗畫面的實際像素寬度；0 代表最大化。 */
        public int windowWidth() {
            return windowWidth;
        }

        public int windowHeight() {
            return windowHeight;
        }

        public TextSpeed textSpeed() {
            return textSpeed == null ? TextSpeed.NORMAL : textSpeed;
        }

        public AutoDelay autoDelay() {
            return autoDelay == null ? AutoDelay.MEDIUM : autoDelay;
        }

        /** 快進要不要連沒讀過的台詞也跳過。 */
        public boolean skipAll() {
            return skipAll;
        }

        public Motion.Level motion() {
            return motion == null ? Motion.Level.BOUNCY : motion;
        }

        public void setVolume(double v) {
            volume = v;
        }

        public void setMusic(double v) {
            music = v;
        }

        public void setSfx(double v) {
            sfx = v;
        }

        public void setAmbient(double v) {
            ambient = v;
        }

        public void setVoice(double v) {
            voice = v;
        }

        public void setTyping(boolean on) {
            typing = on;
        }

        public void setFullscreen(boolean on) {
            fullscreen = on;
        }

        public void setBorderless(boolean on) {
            borderless = on;
        }

        public void setZoom(double z) {
            zoom = z;
        }

        public void setCodeFont(double size) {
            codeFont = size;
        }

        public void setCodeFamily(String family) {
            codeFamily = family;
        }

        public void setCodeDark(boolean on) {
            codeDark = on;
        }

        /** 傳 0、0 代表最大化。 */
        public void setWindow(int width, int height) {
            windowWidth = width;
            windowHeight = height;
        }

        public void setTextSpeed(TextSpeed s) {
            textSpeed = s;
        }

        public void setAutoDelay(AutoDelay d) {
            autoDelay = d;
        }

        public void setSkipAll(boolean on) {
            skipAll = on;
        }

        public void setMotion(Motion.Level l) {
            motion = l;
        }
    }

    private static final class LessonProgress {
        private Set<String> solved = new LinkedHashSet<>();
        private Map<String, Integer> hints = new HashMap<>();
        private Map<String, String> code = new HashMap<>();
    }

    /** 一份程式題紀錄：完成的步驟、看過幾層提示、寫過的程式碼。劇情和練習各有一份，分開存。 */
    public static final class LessonRecord {
        // 每次都回頭問 Progress 拿表：重設練習會換掉整張表，先拿到的紀錄也要跟著換
        private final Supplier<Map<String, LessonProgress>> map;

        private LessonRecord(Supplier<Map<String, LessonProgress>> map) {
            this.map = map;
        }

        public boolean isEmpty() {
            return map.get().isEmpty();
        }

        public boolean isSolved(String lessonId, String stepId) {
            LessonProgress p = map.get().get(lessonId);
            return p != null && p.solved.contains(stepId);
        }

        public void markSolved(String lessonId, String stepId) {
            of(lessonId).solved.add(stepId);
        }

        public int hintsRevealed(String lessonId, String stepId) {
            LessonProgress p = map.get().get(lessonId);
            return p == null ? 0 : p.hints.getOrDefault(stepId, 0);
        }

        public void setHintsRevealed(String lessonId, String stepId, int n) {
            of(lessonId).hints.put(stepId, n);
        }

        public String code(String lessonId, String stepId) {
            LessonProgress p = map.get().get(lessonId);
            return p == null ? null : p.code.get(stepId);
        }

        public void setCode(String lessonId, String stepId, String source) {
            of(lessonId).code.put(stepId, source);
        }

        private LessonProgress of(String lessonId) {
            return map.get().computeIfAbsent(lessonId, k -> new LessonProgress());
        }
    }

    private Route lastRoute;
    private Map<Route, StoryState> routes = new HashMap<>();
    private Set<String> read = new HashSet<>();
    private Set<String> notes = new LinkedHashSet<>();
    private Map<String, LessonProgress> lessons = new HashMap<>();
    /** 練習模式的紀錄，跟劇情的 {@link #lessons} 分開；舊存檔沒有這一欄。 */
    private Map<String, LessonProgress> practice = new HashMap<>();
    private Settings settings = new Settings();

    public Settings settings() {
        if (settings == null) {
            settings = new Settings();
        }
        return settings;
    }

    /** 設定全部回到預設值；劇情和程式題進度不動。 */
    public void resetSettings() {
        settings = new Settings();
    }

    /** 最後玩的視角，標題畫面的「繼續」從這裡接。 */
    public Route lastRoute() {
        return lastRoute != null && route(lastRoute) != null ? lastRoute : null;
    }

    public void setLastRoute(Route r) {
        lastRoute = r;
    }

    /** 這個視角還沒開始玩就是 null。 */
    public StoryState route(Route r) {
        return routes().get(r);
    }

    /** 開新的一輪：那個視角的劇情和它的程式題一起清掉，另一個視角、已讀台詞和筆記保留。 */
    public StoryState startRoute(Route r, Iterable<String> lessonIds) {
        StoryState s = new StoryState();
        routes().put(r, s);
        for (String id : lessonIds) {
            lessons().remove(id);
        }
        return s;
    }

    public void resetRoute(Route r, Iterable<String> lessonIds) {
        routes().remove(r);
        for (String id : lessonIds) {
            lessons().remove(id);
        }
        if (lastRoute == r) {
            lastRoute = null;
        }
    }

    /** 兩個視角共用：共通場景在一邊讀過，另一邊也能快進。 */
    public Set<String> read() {
        if (read == null) {
            read = new HashSet<>();
        }
        return read;
    }

    /** 已解鎖的筆記條目，兩個視角共用。 */
    public Set<String> notes() {
        if (notes == null) {
            notes = new LinkedHashSet<>();
        }
        return notes;
    }

    /** 劇情裡的程式題紀錄，依視角清除，見 {@link #startRoute} 和 {@link #resetRoute}。 */
    public LessonRecord storyLessons() {
        return new LessonRecord(this::lessons);
    }

    /** 練習模式的程式題紀錄；新遊戲和重設視角都不清它。 */
    public LessonRecord practiceLessons() {
        return new LessonRecord(this::practice);
    }

    /** 只清練習的紀錄；劇情、筆記和設定不動。 */
    public void resetPractice() {
        practice = new HashMap<>();
    }

    private Map<Route, StoryState> routes() {
        if (routes == null) {
            routes = new HashMap<>();
        }
        return routes;
    }

    private Map<String, LessonProgress> lessons() {
        if (lessons == null) {
            lessons = new HashMap<>();
        }
        return lessons;
    }

    private Map<String, LessonProgress> practice() {
        if (practice == null) {
            practice = new HashMap<>();
        }
        return practice;
    }
}
