import java.util.ArrayList;

public class Reconciler {
    public static ArrayList<String> cleanUnique(ArrayList<String> items) {
        ArrayList<String> result = new ArrayList<>();
        if (items == null) {
            return result;
        }
        for (String item : items) {
            if (item != null && !item.isBlank()) {
                String clean = item.trim();
                if (!result.contains(clean)) {
                    result.add(clean);
                }
            }
        }
        return result;
    }

    public static ArrayList<String> missingFeatures(ArrayList<String> needed, ArrayList<String> implemented) {
        ArrayList<String> cleanNeed = cleanUnique(needed);
        ArrayList<String> cleanHave = cleanUnique(implemented);
        ArrayList<String> missing = new ArrayList<>();
        for (String item : cleanNeed) {
            if (!cleanHave.contains(item)) {
                missing.add(item);
            }
        }
        return missing;
    }

    public static ArrayList<String> unusedFeatures(ArrayList<String> needed, ArrayList<String> implemented) {
        ArrayList<String> cleanNeed = cleanUnique(needed);
        ArrayList<String> cleanHave = cleanUnique(implemented);
        ArrayList<String> unused = new ArrayList<>();
        for (String item : cleanHave) {
            if (!cleanNeed.contains(item)) {
                unused.add(item);
            }
        }
        return unused;
    }

    public static ArrayList<String> duplicateItems(ArrayList<String> items) {
        ArrayList<String> seen = new ArrayList<>();
        ArrayList<String> dups = new ArrayList<>();
        if (items == null) {
            return dups;
        }
        for (String item : items) {
            if (item != null && !item.isBlank()) {
                String clean = item.trim();
                if (seen.contains(clean)) {
                    if (!dups.contains(clean)) {
                        dups.add(clean);
                    }
                } else {
                    seen.add(clean);
                }
            }
        }
        return dups;
    }
}
