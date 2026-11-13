import java.util.HashMap;

public class Main {
    public static HashMap<String, String> buildTable() {
        HashMap<String, String> initials = new HashMap<>();
        ____
        return initials;
    }

    public static String lookupOrUnknown(HashMap<String, String> table, String code) {
        ____
    }

    public static void main(String[] args) {
        HashMap<String, String> initials = buildTable();
        System.out.println(lookupOrUnknown(initials, "yc"));
        System.out.println(lookupOrUnknown(initials, "zz"));
    }
}
