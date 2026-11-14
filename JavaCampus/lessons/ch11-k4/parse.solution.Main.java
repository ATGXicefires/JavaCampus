import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
    }

    public static ArrayList<NoteEntry> parseNotes(String[] lines) {
        ArrayList<NoteEntry> out = new ArrayList<>();
        if (lines == null) {
            return out;
        }
        for (String line : lines) {
            if (line == null) {
                continue;
            }
            String s = line.trim();
            if (s.isEmpty() || s.startsWith("#")) {
                continue;
            }
            String norm = s.replace('：', ':');
            int idx = norm.indexOf(':');
            if (idx < 0 || idx != norm.lastIndexOf(':')) {
                continue;
            }
            String subj = norm.substring(0, idx).trim();
            String num = norm.substring(idx + 1).trim();
            if (subj.isEmpty() || num.isEmpty()) {
                continue;
            }
            try {
                int score = Integer.parseInt(num);
                if (score >= 0 && score <= 100) {
                    out.add(new NoteEntry(subj, score));
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return out;
    }
}
