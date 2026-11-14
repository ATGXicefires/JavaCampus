import java.util.HashMap;

public class Main {
    public static HashMap<String, String> buildTable() {
        return new HashMap<>();
    }

    public static String lookupOrUnknown(HashMap<String, String> table, String code) {
        return "";
    }

    public static void main(String[] args) {
        HashMap<String, String> initials = buildTable();
        System.out.println(lookupOrUnknown(initials, "yc"));
        System.out.println(lookupOrUnknown(initials, "zz"));
    }
}
