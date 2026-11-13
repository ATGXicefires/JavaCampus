import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        String[] raw = {"Java:95", " 數學 ： 82 ", "# 補考前別看", "Java:88", "英文:缺考", "物理:105", "數學:75", "Java:92"};
        ArrayList<NoteEntry> list = parseNotes(raw);
        System.out.println("有效筆記：" + list.size() + " 筆");
        System.out.println(subjectSummary(list, "Java"));
        System.out.println(subjectSummary(list, "數學"));
        System.out.println("A 等第：" + countByGrade(list, "A") + " 筆");
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

    public static int countByGrade(ArrayList<NoteEntry> entries, String grade) {
        if (entries == null || grade == null) {
            return 0;
        }
        String target = grade.trim();
        int count = 0;
        for (NoteEntry e : entries) {
            if (e != null && e.grade().equalsIgnoreCase(target)) {
                count++;
            }
        }
        return count;
    }

    public static String subjectSummary(ArrayList<NoteEntry> entries, String subject) {
        String target = subject == null ? "" : subject.trim();
        if (entries == null || target.isEmpty()) {
            return target + ": 0 筆";
        }
        int count = 0;
        int sum = 0;
        int max = -1;
        for (NoteEntry e : entries) {
            if (e == null || !e.getSubject().equals(target)) {
                continue;
            }
            count++;
            sum += e.getScore();
            if (e.getScore() > max) {
                max = e.getScore();
            }
        }
        if (count == 0) {
            return target + ": 0 筆";
        }
        long avg = Math.round((double) sum / count);
        return target + ": " + count + " 筆, 平均 " + avg + ", 最高 " + max;
    }
}
