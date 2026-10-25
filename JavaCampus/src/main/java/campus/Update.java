package campus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * 到啟動器給的網址看最新版是幾版，比現在的新才回傳；連不上或內容不對都當作沒有新版。
 * 下載和安裝都由啟動器負責，遊戲只決定標題畫面要不要放「更新」。
 */
final class Update {

    /** 遊戲以這個代碼結束時，啟動器會先下載新版再重新開啟遊戲（Launcher.cs 的 UpdateExitCode）。 */
    static final int EXIT_CODE = 75;

    private static final int TIMEOUT_MS = 5000;

    private Update() {
    }

    /**
     * @param url 啟動器傳來的 update.txt 網址；從原始碼執行時沒有
     * @param current 目前的版本號，jpackage 寫在 cfg 裡；從原始碼執行時沒有
     */
    static CompletableFuture<Optional<String>> check(String url, String current) {
        if (url == null || url.isBlank() || current == null) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                URLConnection c = URI.create(url).toURL().openConnection();
                c.setConnectTimeout(TIMEOUT_MS);
                c.setReadTimeout(TIMEOUT_MS);
                try (BufferedReader r = new BufferedReader(
                        new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8))) {
                    return latest(r.lines()).filter(v -> isNewer(v, current));
                }
            } catch (IOException | UncheckedIOException | IllegalArgumentException e) {
                return Optional.empty();
            }
        });
    }

    /** update.txt 裡寫著 {@code version<Tab>0.1.4} 的那一行；其他行是給啟動器看的。 */
    static Optional<String> latest(Stream<String> lines) {
        return lines.filter(l -> l.startsWith("version\t"))
                .map(l -> l.substring("version\t".length()).trim())
                .filter(v -> !v.isEmpty())
                .findFirst();
    }

    /** 逐段比數字；{@link Runtime.Version} 不接受 0 開頭的版本號，像 0.1.4。 */
    static boolean isNewer(String candidate, String current) {
        int[] a = numbers(candidate);
        int[] b = numbers(current);
        if (a == null || b == null) {
            return false;
        }
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int x = i < a.length ? a[i] : 0;
            int y = i < b.length ? b[i] : 0;
            if (x != y) {
                return x > y;
            }
        }
        return false;
    }

    private static int[] numbers(String version) {
        try {
            return Stream.of(version.trim().split("\\.")).mapToInt(Integer::parseInt).toArray();
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
