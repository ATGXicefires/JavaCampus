package detective.progress;

import detective.fx.Motion;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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

    /** 舊存檔沒有的欄位會維持這裡的預設值；列舉值讀不懂時 Gson 給 null，所以列舉一律經過預設。 */
    public static final class Settings {
        private double volume = 0.7;
        private double sfx = 1;
        private double rain = 1;
        private boolean typing = true;
        private boolean fullscreen;
        private double zoom = 1;
        private double codeFont = 17;
        private TextSpeed textSpeed = TextSpeed.NORMAL;
        private Motion.Level motion = Motion.Level.BOUNCY;

        public double volume() {
            return volume;
        }

        public double sfx() {
            return sfx;
        }

        public double rain() {
            return rain;
        }

        public boolean typing() {
            return typing;
        }

        public boolean fullscreen() {
            return fullscreen;
        }

        public double zoom() {
            return zoom;
        }

        public double codeFont() {
            return codeFont;
        }

        public TextSpeed textSpeed() {
            return textSpeed == null ? TextSpeed.NORMAL : textSpeed;
        }

        public Motion.Level motion() {
            return motion == null ? Motion.Level.BOUNCY : motion;
        }

        public void setVolume(double v) {
            volume = v;
        }

        public void setSfx(double v) {
            sfx = v;
        }

        public void setRain(double v) {
            rain = v;
        }

        public void setTyping(boolean on) {
            typing = on;
        }

        public void setFullscreen(boolean on) {
            fullscreen = on;
        }

        public void setZoom(double z) {
            zoom = z;
        }

        public void setCodeFont(double size) {
            codeFont = size;
        }

        public void setTextSpeed(TextSpeed s) {
            textSpeed = s;
        }

        public void setMotion(Motion.Level l) {
            motion = l;
        }
    }

    private static final class CaseProgress {
        private Set<String> solved = new LinkedHashSet<>();
        private Map<String, Integer> hints = new HashMap<>();
        private Map<String, String> code = new HashMap<>();
    }

    private Map<String, CaseProgress> cases = new HashMap<>();
    private Settings settings = new Settings();

    public Settings settings() {
        if (settings == null) {
            settings = new Settings();
        }
        return settings;
    }

    public boolean isSolved(String caseId, String stepId) {
        CaseProgress p = cases.get(caseId);
        return p != null && p.solved.contains(stepId);
    }

    public void markSolved(String caseId, String stepId) {
        of(caseId).solved.add(stepId);
    }

    public int hintsRevealed(String caseId, String stepId) {
        CaseProgress p = cases.get(caseId);
        return p == null ? 0 : p.hints.getOrDefault(stepId, 0);
    }

    public void setHintsRevealed(String caseId, String stepId, int n) {
        of(caseId).hints.put(stepId, n);
    }

    public int totalHints(String caseId) {
        CaseProgress p = cases.get(caseId);
        return p == null ? 0 : p.hints.values().stream().mapToInt(Integer::intValue).sum();
    }

    public String code(String caseId, String stepId) {
        CaseProgress p = cases.get(caseId);
        return p == null ? null : p.code.get(stepId);
    }

    public void setCode(String caseId, String stepId, String source) {
        of(caseId).code.put(stepId, source);
    }

    /** 清掉所有案件進度，設定保留。 */
    public void resetCases() {
        cases.clear();
    }

    private CaseProgress of(String caseId) {
        return cases.computeIfAbsent(caseId, k -> new CaseProgress());
    }
}
