public class Main {
    public static void main(String[] args) {
        System.out.println(Memory.FIRST_LINE);
    }

    static String easterEgg(String input) {
        if (input.trim().equals(Memory.FIRST_LINE)) {
            return "TODO";
        }
        return "";
    }
}
