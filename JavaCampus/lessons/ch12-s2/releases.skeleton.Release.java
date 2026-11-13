import java.util.ArrayList;
import java.util.List;

public class Release {
    private final String version;
    private final String date;
    private final ArrayList<String> items = new ArrayList<>();

    public Release(String version, String date, List<String> items) {
        ____
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
        ____
    }
}
