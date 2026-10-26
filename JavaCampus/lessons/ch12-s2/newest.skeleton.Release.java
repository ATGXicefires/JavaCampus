public class Release {
    String version;
    String month;
    String note;

    Release(String version, String month, String note) {
        this.version = version;
        this.month = month;
        this.note = note;
    }

    @Override
    public String toString() {
        return version + "（" + month + "）" + note;
    }
}
