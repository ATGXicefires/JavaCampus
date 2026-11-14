import java.util.ArrayList;
import java.util.List;

public class Release {
    private final String version;
    private final String date;
    private final ArrayList<String> items = new ArrayList<>();

    public Release(String version, String date, List<String> items) {
        this.version = version == null ? "" : version.trim();
        this.date = date == null ? "" : date.trim();
        if (items != null) {
            for (String item : items) {
                if (item != null && !item.isBlank()) {
                    this.items.add(item.trim());
                }
            }
        }
    }

    public String version() {
        return version;
    }

    public String date() {
        return date;
    }

    public ArrayList<String> items() {
        return new ArrayList<>(items);
    }

    public String toString() {
        String body = items.isEmpty() ? "無更新項目" : String.join("、", items);
        return version + " (" + date + ")：" + body;
    }
}
