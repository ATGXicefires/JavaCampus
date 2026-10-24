package campus.fx;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FaderTest {

    private final Fader fader = new Fader(false);
    private final FakeClip a = new FakeClip();
    private final FakeClip b = new FakeClip();

    private void run(double seconds) {
        for (long i = Math.round(seconds * 1000 / Fader.TICK_MS); i > 0; i--) {
            fader.tick();
            assertFalse(a.audible() && b.audible(), "兩軌同時有聲音");
        }
    }

    @Test
    void fadingToZeroStopsTheClip() {
        fader.fadeTo(a.clip, 0.5, 0.2);
        assertTrue(a.running);
        run(0.3);
        assertTrue(a.audible());
        fader.fadeTo(a.clip, 0, 0.2);
        assertTrue(fader.isAudible(a.clip), "淡出途中還聽得到");
        assertFalse(fader.isPlaying(a.clip));
        run(0.3);
        assertFalse(a.running);
        assertFalse(fader.isAudible(a.clip));
    }

    @Test
    void waitsUntilTheOtherClipHasStoppedBeforeStarting() {
        fader.fadeTo(a.clip, 0.5, 0.1);
        run(0.2);
        fader.fadeTo(a.clip, 0, 0.7);
        fader.fadeTo(b.clip, 0.5, 0.5, List.of(a.clip));
        assertFalse(b.running);
        assertTrue(fader.isPlaying(b.clip));
        assertFalse(fader.isAudible(b.clip));
        run(0.6);
        assertTrue(a.running);
        assertFalse(b.running);
        run(0.2);
        assertFalse(a.running);
        assertTrue(b.running);
        run(0.6);
        assertEquals(20 * Math.log10(0.5), b.gain.getValue(), 0.01);
    }

    @Test
    void aWaitingClipThatIsNoLongerWantedNeverStarts() {
        fader.fadeTo(a.clip, 0.5, 0.1);
        run(0.2);
        fader.fadeTo(a.clip, 0, 0.5);
        fader.fadeTo(b.clip, 0.5, 0.5, List.of(a.clip));
        run(0.2);
        fader.fadeTo(b.clip, 0, 0.5);
        run(1);
        assertEquals(0, b.starts);
        assertFalse(fader.isPlaying(b.clip));
        assertFalse(a.running);
    }

    @Test
    void askingForTheSameFadeAgainNeverSlowsItDown() {
        fader.fadeTo(a.clip, 0.5, 0.1);
        run(0.2);
        fader.fadeTo(a.clip, 0, 0.3);
        run(0.2);
        fader.fadeTo(a.clip, 0, 1.5);
        run(0.15);
        assertFalse(a.running);
    }

    @Test
    void aClipAlreadyPlayingIgnoresWhatItShouldWaitFor() {
        fader.fadeTo(a.clip, 0.5, 0.1);
        run(0.2);
        fader.fadeTo(a.clip, 0, 0.7);
        fader.fadeTo(b.clip, 0.5, 0.5, List.of(a.clip));
        run(0.2);
        fader.fadeTo(b.clip, 0, 0.5);
        fader.fadeTo(a.clip, 0.5, 0.3, List.of(b.clip));
        run(0.4);
        assertTrue(a.audible());
        assertEquals(20 * Math.log10(0.5), a.gain.getValue(), 0.01);
        assertEquals(1, a.starts, "淡出到一半又要回來，接著原本的位置播，不重新開始");
    }
}
