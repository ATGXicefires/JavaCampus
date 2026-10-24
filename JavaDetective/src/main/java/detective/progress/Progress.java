package detective.progress;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** 玩家進度與設定。用 Gson 存成 JSON，所以欄位保持可變的一般集合。 */
public final class Progress {

    public static final class Settings {
        private double volume = 0.7;
        private boolean ambient = true;
        private boolean typing = true;

        public double volume() {
            return volume;
        }

        public boolean ambient() {
            return ambient;
        }

        public boolean typing() {
            return typing;
        }

        public void setVolume(double v) {
            volume = v;
        }

        public void setAmbient(boolean on) {
            ambient = on;
        }

        public void setTyping(boolean on) {
            typing = on;
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

    private CaseProgress of(String caseId) {
        return cases.computeIfAbsent(caseId, k -> new CaseProgress());
    }
}
