package detective.fx;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;

/** 全部用程式合成的音效。找不到音訊裝置時整組靜音，遊戲照常進行。 */
public final class Sfx implements AutoCloseable {

    public enum Sound { KEY, RUN, SUCCESS, FAIL, UNLOCK, STAMP, PAPER }

    private static final float RATE = 44_100f;
    private static final AudioFormat FORMAT = new AudioFormat(RATE, 16, 1, true, false);
    private static final double AMBIENT_LEVEL = 0.35;

    private final Map<Sound, Clip[]> clips = new EnumMap<>(Sound.class);
    private final Map<Sound, Integer> next = new EnumMap<>(Sound.class);
    private Clip ambient;
    private double volume = 0.7;
    private boolean typing = true;
    private boolean ambientOn;

    private Sfx() {}

    public static Sfx create() {
        Sfx sfx = new Sfx();
        try {
            Random rnd = new Random(7);
            for (Sound s : Sound.values()) {
                int pool = s == Sound.KEY ? 6 : 2;
                Clip[] arr = new Clip[pool];
                for (int i = 0; i < pool; i++) {
                    arr[i] = open(synth(s, rnd));
                }
                sfx.clips.put(s, arr);
                sfx.next.put(s, 0);
            }
            sfx.ambient = open(rain(rnd));
        } catch (Exception | LinkageError e) {
            System.err.println("音效停用：" + e.getMessage());
            sfx.close();
            sfx.clips.clear();
            sfx.ambient = null;
        }
        return sfx;
    }

    public void play(Sound sound) {
        if (sound == Sound.KEY && !typing) {
            return;
        }
        Clip[] pool = clips.get(sound);
        if (pool == null) {
            return;
        }
        int i = next.get(sound);
        next.put(sound, (i + 1) % pool.length);
        Clip c = pool[i];
        c.stop();
        c.setFramePosition(0);
        gain(c, volume);
        c.start();
    }

    public void setVolume(double v) {
        volume = v;
        if (ambient != null) {
            gain(ambient, volume * AMBIENT_LEVEL);
        }
    }

    public void setTyping(boolean on) {
        typing = on;
    }

    public void setAmbient(boolean on) {
        ambientOn = on;
        if (ambient == null) {
            return;
        }
        if (on) {
            gain(ambient, volume * AMBIENT_LEVEL);
            ambient.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            ambient.stop();
        }
    }

    public boolean ambientOn() {
        return ambientOn;
    }

    @Override
    public void close() {
        clips.values().forEach(arr -> {
            for (Clip c : arr) {
                if (c != null) {
                    c.close();
                }
            }
        });
        if (ambient != null) {
            ambient.close();
        }
    }

    private static void gain(Clip c, double linear) {
        if (!c.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl g = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
        float db = linear <= 0.0001 ? g.getMinimum() : (float) (20 * Math.log10(linear));
        g.setValue(Math.max(g.getMinimum(), Math.min(g.getMaximum(), db)));
    }

    private static Clip open(float[] samples) throws Exception {
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

    private static float[] synth(Sound s, Random rnd) {
        return switch (s) {
            case KEY -> key(rnd);
            case RUN -> run(rnd);
            case SUCCESS -> chime();
            case FAIL -> buzz();
            case UNLOCK -> sweep();
            case STAMP -> stamp(rnd);
            case PAPER -> paper(rnd);
        };
    }

    private static float[] buffer(double seconds) {
        return new float[(int) (seconds * RATE)];
    }

    /** 把 f(t) 疊加到 buf 上，從 offset 秒開始。 */
    private static void add(float[] buf, double offset, double seconds, DoubleUnaryOperator f) {
        int start = (int) (offset * RATE);
        int len = (int) (seconds * RATE);
        for (int i = 0; i < len && start + i < buf.length; i++) {
            buf[start + i] += (float) f.applyAsDouble(i / RATE);
        }
    }

    private static double decay(double t, double tau) {
        return Math.exp(-t / tau);
    }

    private static double sine(double t, double hz) {
        return Math.sin(2 * Math.PI * hz * t);
    }

    private static float[] key(Random rnd) {
        float[] b = buffer(0.06);
        double pitch = 0.9 + rnd.nextDouble() * 0.2;
        add(b, 0, 0.06, t -> (rnd.nextDouble() * 2 - 1) * 0.5 * decay(t, 0.004));
        add(b, 0, 0.06, t -> sine(t, 1800 * pitch) * 0.25 * decay(t, 0.008));
        add(b, 0, 0.06, t -> sine(t, 170 * pitch) * 0.35 * decay(t, 0.015));
        return b;
    }

    private static float[] run(Random rnd) {
        float[] b = buffer(0.2);
        for (double at : new double[] {0, 0.075}) {
            add(b, at, 0.08, t -> (rnd.nextDouble() * 2 - 1) * 0.45 * decay(t, 0.003));
            add(b, at, 0.08, t -> sine(t, 900) * 0.3 * decay(t, 0.012));
            add(b, at, 0.08, t -> sine(t, 140) * 0.3 * decay(t, 0.02));
        }
        return b;
    }

    private static float[] chime() {
        float[] b = buffer(0.9);
        double[][] notes = {{0, 523.25}, {0.11, 783.99}};
        for (double[] n : notes) {
            add(b, n[0], 0.75, t -> {
                double env = Math.min(1, t / 0.005) * decay(t, 0.25);
                return (sine(t, n[1]) + 0.3 * sine(t, n[1] * 2) + 0.1 * sine(t, n[1] * 3)) * 0.28 * env;
            });
        }
        return b;
    }

    private static float[] buzz() {
        float[] b = buffer(0.32);
        double[][] notes = {{0, 110}, {0.12, 98}};
        for (double[] n : notes) {
            add(b, n[0], 0.2, t -> {
                double square = 0;
                for (int h = 1; h <= 7; h += 2) {
                    square += sine(t, n[1] * h) / h;
                }
                return square * 0.22 * Math.min(1, t / 0.004) * decay(t, 0.09);
            });
        }
        return b;
    }

    private static float[] sweep() {
        float[] b = buffer(0.45);
        double duration = 0.32;
        double[] phase = {0};
        add(b, 0, duration, t -> {
            double hz = 400 + (1200 - 400) * (t / duration);
            phase[0] += 2 * Math.PI * hz / RATE;
            return Math.sin(phase[0]) * 0.22 * Math.sin(Math.PI * t / duration);
        });
        add(b, 0.28, 0.17, t -> sine(t, 2400) * 0.12 * decay(t, 0.04));
        return b;
    }

    private static float[] stamp(Random rnd) {
        float[] b = buffer(0.45);
        double[] phase = {0};
        add(b, 0, 0.4, t -> {
            double hz = 90 - 40 * Math.min(1, t / 0.15);
            phase[0] += 2 * Math.PI * hz / RATE;
            return Math.sin(phase[0]) * 0.75 * decay(t, 0.08);
        });
        double[] low = {0};
        add(b, 0, 0.12, t -> {
            low[0] += 0.15 * ((rnd.nextDouble() * 2 - 1) - low[0]);
            return low[0] * 1.6 * decay(t, 0.02);
        });
        return b;
    }

    private static float[] paper(Random rnd) {
        float[] b = buffer(0.3);
        double[] lp = {0};
        double[] prev = {0};
        add(b, 0, 0.28, t -> {
            double white = rnd.nextDouble() * 2 - 1;
            lp[0] += 0.3 * (white - lp[0]);
            double band = lp[0] - prev[0] * 0.6;
            prev[0] = lp[0];
            return band * 0.5 * Math.sin(Math.PI * t / 0.28);
        });
        return b;
    }

    /** 8 秒的雨聲循環：低通雜訊加隨機水滴，頭尾交叉淡化才不會在接縫處爆音。 */
    private static float[] rain(Random rnd) {
        double loop = 8.0;
        double fade = 0.5;
        float[] raw = buffer(loop + fade);
        double[] lp = {0};
        add(raw, 0, loop + fade, t -> {
            lp[0] += 0.08 * ((rnd.nextDouble() * 2 - 1) - lp[0]);
            return lp[0] * 0.9;
        });
        for (int i = 0; i < 90; i++) {
            double at = rnd.nextDouble() * (loop + fade - 0.05);
            double hz = 2000 + rnd.nextDouble() * 2500;
            double amp = 0.02 + rnd.nextDouble() * 0.05;
            add(raw, at, 0.05, t -> sine(t, hz) * amp * decay(t, 0.006));
        }
        float[] out = buffer(loop);
        int n = out.length;
        int f = (int) (fade * RATE);
        System.arraycopy(raw, 0, out, 0, n);
        for (int i = 0; i < f; i++) {
            double w = (double) i / f;
            out[i] = (float) (raw[n + i] * (1 - w) + raw[i] * w);
        }
        return out;
    }
}
