package campus.fx;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MusicTest {

    @Test
    void everyTrackRendersAnAudibleLoopOfTheRightLength() {
        for (Music.Track t : Music.Track.values()) {
            float[] samples = Music.render(t);
            double seconds = samples.length / Synth.RATE;
            assertTrue(seconds >= 29 && seconds <= 61, t + " 長度 " + seconds);
            double peak = 0;
            double energy = 0;
            for (float v : samples) {
                assertTrue(Float.isFinite(v), t + " 有不是數字的取樣");
                peak = Math.max(peak, Math.abs(v));
                energy += v * v;
            }
            assertTrue(peak > 0.8 && peak <= 0.86, t + " 峰值 " + peak);
            assertTrue(Math.sqrt(energy / samples.length) > 0.02, t + " 幾乎沒有聲音");
        }
    }

    @Test
    void noteNames() {
        assertEquals(60, Music.Score.midi("C4"));
        assertEquals(70, Music.Score.midi("Bb4"));
        assertEquals(78, Music.Score.midi("F#5"));
        assertEquals(84, Music.Score.midi("C6"));
    }
}
