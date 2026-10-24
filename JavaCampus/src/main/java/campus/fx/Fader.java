package campus.fx;

import javax.sound.sampled.Clip;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 在背景執行緒慢慢調整循環音軌的音量，換環境音時交叉淡化；降到 0 的音軌會停掉，不在背景空轉。
 * 音軌可以指定要等哪些音軌完全停掉才開始，背景音樂靠這個保證一次只聽得到一首。
 */
final class Fader implements AutoCloseable {

    static final long TICK_MS = 20;

    private static final class Fade {
        double current;
        double target;
        double perSecond;
        boolean started;
        Collection<Clip> after = List.of();
    }

    private final Map<Clip, Fade> fades = new HashMap<>();
    private final Thread thread;
    private volatile boolean running = true;

    Fader() {
        this(true);
    }

    /** {@code background} 為 false 時不開執行緒，由測試自己呼叫 {@link #tick()} 推進時間。 */
    Fader(boolean background) {
        thread = new Thread(this::loop, "audio-fader");
        thread.setDaemon(true);
        if (background) {
            thread.start();
        }
    }

    /** 沒在播的音軌會從 0 開始循環播放，再淡入到 {@code target}。 */
    synchronized void fadeTo(Clip c, double target, double seconds) {
        fadeTo(c, target, seconds, List.of());
    }

    /** 同上，但還沒開始播的音軌要等 {@code after} 裡的音軌都停掉才開始；已經在播的直接調音量。 */
    synchronized void fadeTo(Clip c, double target, double seconds, Collection<Clip> after) {
        Fade f = fades.get(c);
        if (f == null) {
            if (target <= 0) {
                return;
            }
            f = new Fade();
            fades.put(c, f);
        }
        double perSecond = seconds <= 0 ? Double.MAX_VALUE : Math.max(Math.abs(target - f.current), 0.01) / seconds;
        // 同一個目標再要求一次時不放慢進行中的淡出，連續換曲時前一首的尾巴才不會越拖越長
        f.perSecond = target == f.target ? Math.max(f.perSecond, perSecond) : perSecond;
        f.target = target;
        if (!f.started) {
            f.after = List.copyOf(after);
            startIfClear(c, f);
        }
    }

    synchronized boolean isPlaying(Clip c) {
        Fade f = fades.get(c);
        return f != null && f.target > 0;
    }

    /** 已經開始播、還沒淡出到停掉；和 {@link #isPlaying} 不同，正在淡出的也算。 */
    synchronized boolean isAudible(Clip c) {
        Fade f = fades.get(c);
        return f != null && f.started;
    }

    private void startIfClear(Clip c, Fade f) {
        if (f.target <= 0) {
            return;
        }
        for (Clip other : f.after) {
            if (other != c && isAudible(other)) {
                return;
            }
        }
        f.started = true;
        Synth.gain(c, 0);
        c.loop(Clip.LOOP_CONTINUOUSLY);
    }

    synchronized void tick() {
        Iterator<Map.Entry<Clip, Fade>> it = fades.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Clip, Fade> e = it.next();
            Clip c = e.getKey();
            Fade f = e.getValue();
            if (!f.started) {
                startIfClear(c, f);
                if (!f.started) {
                    if (f.target <= 0) {
                        it.remove();
                    }
                    continue;
                }
            }
            double step = f.perSecond * TICK_MS / 1000.0;
            if (Math.abs(f.target - f.current) <= step) {
                f.current = f.target;
            } else {
                f.current += Math.signum(f.target - f.current) * step;
            }
            Synth.gain(c, f.current);
            if (f.current <= 0 && f.target <= 0) {
                c.stop();
                it.remove();
            }
        }
    }

    private void loop() {
        while (running) {
            tick();
            try {
                Thread.sleep(TICK_MS);
            } catch (InterruptedException ex) {
                return;
            }
        }
    }

    @Override
    public void close() {
        running = false;
        thread.interrupt();
    }
}
