public class Credit {
    private final String role;
    private final String name;

    public Credit() {
        this("未分類", "匿名");
    }

    public Credit(String role, String name) {
        this.role = (role == null || role.isBlank()) ? "未分類" : role.trim();
        this.name = (name == null || name.isBlank()) ? "匿名" : name.trim();
    }

    public String role() {
        return role;
    }

    public String name() {
        return name;
    }

    public String toString() {
        return role + " " + name;
    }
}
