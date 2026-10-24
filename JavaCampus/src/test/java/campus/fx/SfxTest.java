package campus.fx;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SfxTest {

    @Test
    void ambienceStepsAsideWhileMusicPlays() {
        assertEquals(0.3, Sfx.ambienceTarget(true, false, 0.3));
        assertEquals(0, Sfx.ambienceTarget(true, true, 0.3));
        assertEquals(0, Sfx.ambienceTarget(false, false, 0.3));
        assertEquals(0, Sfx.ambienceTarget(false, true, 0.3));
    }

    @Test
    void tearIsAShortCrackleThatEndsSilently() {
        float[] s = Sfx.tear(new Random(7));
        double peak = 0;
        double energy = 0;
        for (float v : s) {
            assertTrue(Float.isFinite(v));
            peak = Math.max(peak, Math.abs(v));
            energy += v * v;
        }
        assertTrue(peak <= 0.41, "太大聲：" + peak);
        assertTrue(Math.sqrt(energy / s.length) > 0.03, "幾乎沒有聲音");
        // 結尾要收到幾乎無聲，播完才不會喀一聲
        assertTrue(Math.abs(s[s.length - 1]) < 0.01);
    }

    @Test
    void everyAmbienceLoopIsSeamless() {
        Random rnd = new Random(7);
        for (Sfx.Ambience a : Sfx.Ambience.values()) {
            float[] s = Sfx.loop(a, rnd);
            double energy = 0;
            double steps = 0;
            for (int i = 0; i < s.length; i++) {
                assertTrue(Float.isFinite(s[i]), a + " 有不是數字的取樣");
                assertTrue(Math.abs(s[i]) <= 0.6, a + " 太大聲");
                energy += s[i] * s[i];
                if (i > 0) {
                    steps += (s[i] - s[i - 1]) * (s[i] - s[i - 1]);
                }
            }
            assertTrue(Math.sqrt(energy / s.length) > 0.02, a + " 幾乎沒有聲音");
            // 從最後一個取樣跳回第一個的落差，要和平常相鄰取樣的差距同一個量級，循環時才不會喀一聲
            double typicalStep = Math.sqrt(steps / (s.length - 1));
            double seam = Math.abs(s[0] - s[s.length - 1]);
            assertTrue(seam < 4 * typicalStep, a + " 接縫跳了 " + seam + "，平常約 " + typicalStep);
        }
    }
}
