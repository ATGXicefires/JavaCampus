package campus.fx;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.util.function.DoubleUnaryOperator;

/** 音效和音樂共用的合成小工具：44.1 kHz、16-bit、單聲道。 */
final class Synth {

    static final float RATE = 44_100f;
    static final AudioFormat FORMAT = new AudioFormat(RATE, 16, 1, true, false);

    private Synth() {}

    static float[] buffer(double seconds) {
        return new float[(int) (seconds * RATE)];
    }

    /** 把 f(t) 疊加到 buf 上，從 offset 秒開始，超出結尾的部分丟掉。 */
    static void add(float[] buf, double offset, double seconds, DoubleUnaryOperator f) {
        int start = (int) (offset * RATE);
        int len = (int) (seconds * RATE);
        for (int i = 0; i < len && start + i < buf.length; i++) {
            if (start + i >= 0) {
                buf[start + i] += (float) f.applyAsDouble(i / RATE);
            }
        }
    }

    /** 和 {@link #add} 一樣，但超出結尾的部分繞回開頭疊上，循環的音樂接縫處就聽不出斷點。 */
    static void wrap(float[] buf, double offset, double seconds, DoubleUnaryOperator f) {
        int start = (int) (offset * RATE);
        int len = (int) (seconds * RATE);
        for (int i = 0; i < len; i++) {
            int at = Math.floorMod(start + i, buf.length);
            buf[at] += (float) f.applyAsDouble(i / RATE);
        }
    }

    static double decay(double t, double tau) {
        return Math.exp(-t / tau);
    }

    static double sine(double t, double hz) {
        return Math.sin(2 * Math.PI * hz * t);
    }

    static double hz(int midi) {
        return 440 * Math.pow(2, (midi - 69) / 12.0);
    }

    /** 起音、延音、釋音的包絡；{@code hold} 秒之後開始釋音。 */
    static double envelope(double t, double attack, double hold, double release) {
        if (t < attack) {
            return t / attack;
        }
        if (t < hold) {
            return 1;
        }
        return Math.max(0, 1 - (t - hold) / release);
    }

    /** 等比例縮放到峰值 {@code peak}，各段合成的音量才一致。 */
    static float[] normalize(float[] buf, double peak) {
        double max = 0;
        for (float v : buf) {
            max = Math.max(max, Math.abs(v));
        }
        if (max > 0) {
            double k = peak / max;
            for (int i = 0; i < buf.length; i++) {
                buf[i] *= (float) k;
            }
        }
        return buf;
    }

    /** 環境音的循環：多做 {@code fade} 秒，尾巴和開頭交叉淡化，接縫處才不會爆音。 */
    static float[] loopify(float[] raw, double loopSeconds, double fade) {
        float[] out = buffer(loopSeconds);
        int n = out.length;
        int f = (int) (fade * RATE);
        System.arraycopy(raw, 0, out, 0, n);
        for (int i = 0; i < f && n + i < raw.length; i++) {
            double w = (double) i / f;
            out[i] = (float) (raw[n + i] * (1 - w) + raw[i] * w);
        }
        return out;
    }

    static Clip open(float[] samples) throws Exception {
        byte[] pcm = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            int v = Math.round(Math.max(-1, Math.min(1, samples[i])) * 32_000);
            pcm[2 * i] = (byte) v;
            pcm[2 * i + 1] = (byte) (v >> 8);
        }
        Clip clip = AudioSystem.getClip();
        clip.open(FORMAT, pcm, 0, pcm.length);
        return clip;
    }

    static void gain(Clip c, double linear) {
        if (!c.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl g = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
        float db = linear <= 0.0001 ? g.getMinimum() : (float) (20 * Math.log10(linear));
        g.setValue(Math.max(g.getMinimum(), Math.min(g.getMaximum(), db)));
    }

    /** 二階帶通濾波器（RBJ cookbook），做母音共振峰、蟬鳴的頻帶用。 */
    static final class BandPass {
        private final double b0;
        private final double b2;
        private final double a1;
        private final double a2;
        private double x1;
        private double x2;
        private double y1;
        private double y2;

        BandPass(double hz, double q) {
            double w = 2 * Math.PI * hz / RATE;
            double alpha = Math.sin(w) / (2 * q);
            double a0 = 1 + alpha;
            b0 = alpha / a0;
            b2 = -alpha / a0;
            a1 = -2 * Math.cos(w) / a0;
            a2 = (1 - alpha) / a0;
        }

        double next(double x) {
            double y = b0 * x + b2 * x2 - a1 * y1 - a2 * y2;
            x2 = x1;
            x1 = x;
            y2 = y1;
            y1 = y;
            return y;
        }
    }

    /** 一階低通濾波器。 */
    static final class LowPass {
        private final double k;
        private double y;

        LowPass(double hz) {
            k = 1 - Math.exp(-2 * Math.PI * hz / RATE);
        }

        double next(double x) {
            y += k * (x - y);
            return y;
        }
    }
}
