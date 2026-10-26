public class Main {
    public static void main(String[] args) {
        // 4.2 標準輸出
        // System.out.println(...) 中，System 為 java.lang 套件內之 final class，
        // out 為其 public static final PrintStream 欄位；println 會呼叫 PrintStream 的 println 方法，
        // 將引數轉為字串後寫入標準輸出串流，並附加平台相依之行分隔符號。
        System.out.println("把引號裡的字，原封不動印出來");
    }
}
