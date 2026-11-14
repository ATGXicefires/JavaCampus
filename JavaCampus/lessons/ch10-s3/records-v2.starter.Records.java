import java.util.HashMap;

public class Records {
    private HashMap<String, Integer> best = new HashMap<>();

    public Records() {
    }

    public Records(String[] entries) {
        recordBatch(entries);
    }

    public void record(String unit, int score) {
        // 請檢查有效範圍 (非空課名、0 <= score <= 100)，且僅在比原本高時才更新
    }

    public void recordBatch(String[] entries) {
        // 解析 "課名:分數"，安全略過 null 或格式錯誤
    }

    public int bestOf(String unit) {
        return 0;
    }

    public double averageBest() {
        return 0.0;
    }
}
