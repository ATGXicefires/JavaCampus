package campus;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void versionsAreComparedNumberByNumber() {
        assertTrue(Update.isNewer("0.1.10", "0.1.9"));
        assertTrue(Update.isNewer("0.2", "0.1.9"));
        assertFalse(Update.isNewer("0.1.4", "0.1.4"));
        assertFalse(Update.isNewer("0.1.4.0", "0.1.4"));
        assertFalse(Update.isNewer("0.1.3", "0.1.4"));
    }

    @Test
    void anUnreadableVersionIsNeverNewer() {
        assertFalse(Update.isNewer("0.2.0-beta", "0.1.4"));
        assertFalse(Update.isNewer("", "0.1.4"));
        assertFalse(Update.isNewer("0.2", "dev"));
    }

    @Test
    void onlyTheVersionLineMatters() {
        assertEquals(Optional.of("0.1.4"),
                Update.latest(Stream.of("format\t1", "version\t0.1.4", "part\tapp\t\tabc\t1\tdef\thttp://x")));
        assertEquals(Optional.empty(), Update.latest(Stream.of("format\t1", "part\tapp\t\tabc")));
    }

    @Test
    void aNewerReleaseIsFoundBehindTheLatestRedirect() throws Exception {
        // GitHub 的 releases/latest/download/ 會先轉址到那一版的檔案；啟動器寫的檔案是 CRLF 換行
        server = serve("format\t1\r\nversion\t0.1.4\r\npart\tapp\t\tabc\t1\tdef\thttp://x\r\n");
        assertEquals(Optional.of("0.1.4"), check(url("/latest/update.txt"), "0.1.3"));
    }

    @Test
    void theSameVersionOffersNothing() throws Exception {
        server = serve("format\t1\r\nversion\t0.1.4\r\n");
        assertEquals(Optional.empty(), check(url("/latest/update.txt"), "0.1.4"));
    }

    @Test
    void aMissingFileOrServerMeansNoUpdate() throws Exception {
        server = serve("format\t1\r\nversion\t0.1.4\r\n");
        assertEquals(Optional.empty(), check(url("/nothing-here.txt"), "0.1.3"));

        String gone = url("/latest/update.txt");
        server.stop(0);
        server = null;
        assertEquals(Optional.empty(), check(gone, "0.1.3"));
    }

    @Test
    void runningFromSourceNeverGoesOnline() throws Exception {
        assertEquals(Optional.empty(), check(null, "0.1.3"));
        assertEquals(Optional.empty(), check("http://127.0.0.1:1/update.txt", null));
    }

    private static Optional<String> check(String url, String current) throws Exception {
        return Update.check(url, current).get(10, TimeUnit.SECONDS);
    }

    private static HttpServer serve(String manifest) throws IOException {
        HttpServer s = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        s.createContext("/latest/update.txt", ex -> {
            ex.getResponseHeaders().add("Location", "/download/v0.1.4/update.txt");
            ex.sendResponseHeaders(302, -1);
            ex.close();
        });
        s.createContext("/download/v0.1.4/update.txt", ex -> {
            byte[] body = manifest.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, body.length);
            try (OutputStream out = ex.getResponseBody()) {
                out.write(body);
            }
        });
        s.start();
        return s;
    }

    private String url(String path) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + path;
    }
}
