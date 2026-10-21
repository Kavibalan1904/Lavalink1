package com.kavibalan.resocapture;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ResoCaptureService {
    private static final int MAX_SESSIONS = 4;
    private static final int QUEUE_CHUNKS = 12;
    private static final int CHUNK_SAMPLES = 1024;
    private static final int FFT_SIZE = 2048;
    private static final int MAX_SPECTRUM_FRAMES = 36;
    private static final long SESSION_TTL_MILLIS = 120_000L;
    private static final int BAND_COUNT = 38;

    private final Map<String, CaptureSession> sessions = new ConcurrentHashMap<>();
    private final ThreadPoolExecutor workers = new ThreadPoolExecutor(
            1, 2, 30L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(64),
            new DaemonThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy());

    public SessionCreated createSession() {
        purgeExpired();
        if (sessions.size() >= MAX_SESSIONS) {
            throw new SessionLimitException("Too many active Reso capture sessions");
        }
        String id = UUID.randomUUID().toString();
        CaptureSession session = new CaptureSession(id, System.currentTimeMillis());
        sessions.put(id, session);
        return new SessionCreated(id, 48_000, BAND_COUNT, FFT_SIZE);
    }

    public boolean deleteSession(String sessionId) {
        CaptureSession removed = sessions.remove(sessionId);
        if (removed == null) return false;
        removed.closed.set(true);
        removed.queue.clear();
        removed.spectra.clear();
        return true;
    }

    public void offer(String sessionId, float[][] input, int offset, int length, int sampleRate) {
        CaptureSession session = sessions.get(sessionId);
        if (session == null || session.closed.get() || input == null || input.length == 0 ||
                length <= 0 || offset < 0 || sampleRate <= 0) return;

        session.sampleRate = sampleRate;
        int channelCount = input.length;
        for (int start = 0; start < length; start += CHUNK_SAMPLES) {
            int count = Math.min(CHUNK_SAMPLES, length - start);
            float[] mono = new float[count];
            for (int i = 0; i < count; i++) {
                double sum = 0;
                int validChannels = 0;
                for (int channel = 0; channel < channelCount; channel++) {
                    float[] samples = input[channel];
                    int index = offset + start + i;
                    if (samples != null && index < samples.length) {
                        sum += samples[index];
                        validChannels++;
                    }
                }
                mono[i] = validChannels == 0 ? 0f : (float) (sum / validChannels);
            }
            if (!session.queue.offer(mono)) session.droppedChunks.incrementAndGet();
        }
        scheduleDrain(session);
    }

    public SpectrumSnapshot getSpectrum(String sessionId) {
        purgeExpired();
        CaptureSession session = sessions.get(sessionId);
        if (session == null || session.closed.get()) return null;
        List<float[]> frames = new ArrayList<>(session.spectra);
        return new SpectrumSnapshot(session.id, session.sampleRate, BAND_COUNT, frames,
                session.lastSampleAt, session.droppedChunks.get());
    }

    private void scheduleDrain(CaptureSession session) {
        if (!session.processing.compareAndSet(false, true)) return;
        try {
            workers.execute(() -> drain(session));
        } catch (RejectedExecutionException rejected) {
            session.processing.set(false);
        }
    }

    private void drain(CaptureSession session) {
        try {
            while (!session.closed.get()) {
                float[] chunk = session.queue.poll();
                if (chunk == null) break;
                for (float sample : chunk) {
                    session.frameBuffer[session.sampleCount++] = sample;
                    if (session.sampleCount == FFT_SIZE) {
                        float[] spectrum = analyze(session.frameBuffer, session.sampleRate);
                        session.spectra.addLast(spectrum);
                        while (session.spectra.size() > MAX_SPECTRUM_FRAMES) {
                            session.spectra.pollFirst();
                        }
                        session.sampleCount = 0;
                        session.lastSampleAt = System.currentTimeMillis();
                    }
                }
            }
        } finally {
            session.processing.set(false);
            if (!session.closed.get() && !session.queue.isEmpty()) scheduleDrain(session);
        }
    }

    private void purgeExpired() {
        long cutoff = System.currentTimeMillis() - SESSION_TTL_MILLIS;
        sessions.forEach((id, session) -> {
            if (session.createdAt < cutoff && sessions.remove(id, session)) {
                session.closed.set(true);
                session.queue.clear();
                session.spectra.clear();
            }
        });
    }

    private static float[] analyze(float[] pcm, int sampleRate) {
        double[] real = new double[FFT_SIZE];
        double[] imag = new double[FFT_SIZE];
        // Apply the Hann window in time order before the FFT bit-reversal permutation.
        for (int i = 0; i < FFT_SIZE; i++) {
            real[i] = pcm[i] * 0.5 * (1.0 - Math.cos(2.0 * Math.PI * i / (FFT_SIZE - 1)));
        }

        int j = 0;
        for (int i = 1; i < FFT_SIZE; i++) {
            int bit = FFT_SIZE >> 1;
            while (bit > 0 && (j & bit) != 0) {
                j ^= bit;
                bit >>= 1;
            }
            j ^= bit;
            if (i < j) {
                double swap = real[i];
                real[i] = real[j];
                real[j] = swap;
            }
        }

        for (int size = 2; size <= FFT_SIZE; size <<= 1) {
            int half = size >> 1;
            double angle = -2.0 * Math.PI / size;
            double stepReal = Math.cos(angle);
            double stepImag = Math.sin(angle);
            for (int start = 0; start < FFT_SIZE; start += size) {
                double wr = 1.0;
                double wi = 0.0;
                for (int offset = 0; offset < half; offset++) {
                    int even = start + offset;
                    int odd = even + half;
                    double oddReal = real[odd] * wr - imag[odd] * wi;
                    double oddImag = real[odd] * wi + imag[odd] * wr;
                    double evenReal = real[even];
                    double evenImag = imag[even];
                    real[even] = evenReal + oddReal;
                    imag[even] = evenImag + oddImag;
                    real[odd] = evenReal - oddReal;
                    imag[odd] = evenImag - oddImag;
                    double nextWr = wr * stepReal - wi * stepImag;
                    wi = wr * stepImag + wi * stepReal;
                    wr = nextWr;
                }
            }
        }

        double[] magnitudes = new double[FFT_SIZE / 2];
        for (int i = 1; i < magnitudes.length; i++) magnitudes[i] = Math.hypot(real[i], imag[i]) / FFT_SIZE;

        float[] bands = new float[BAND_COUNT];
        double maxHz = Math.min(16_000.0, sampleRate / 2.0);
        double minLog = Math.log(35.0);
        double maxLog = Math.log(maxHz);
        double peak = 0;
        for (int band = 0; band < BAND_COUNT; band++) {
            double lowHz = Math.exp(minLog + (maxLog - minLog) * band / BAND_COUNT);
            double highHz = Math.exp(minLog + (maxLog - minLog) * (band + 1) / BAND_COUNT);
            int lowBin = Math.max(1, (int) Math.floor(lowHz * FFT_SIZE / sampleRate));
            int highBin = Math.min(magnitudes.length - 1,
                    Math.max(lowBin + 1, (int) Math.ceil(highHz * FFT_SIZE / sampleRate)));
            double total = 0;
            int count = 0;
            for (int bin = lowBin; bin <= highBin; bin++) {
                total += magnitudes[bin];
                count++;
            }
            bands[band] = count == 0 ? 0f : (float) (total / count);
            peak = Math.max(peak, bands[band]);
        }

        if (peak < 1.0e-7) return bands;
        for (int i = 0; i < bands.length; i++) {
            bands[i] = (float) Math.max(0.0, Math.min(1.0, Math.sqrt(bands[i] / peak)));
        }
        return bands;
    }

    public record SessionCreated(String sessionId, int suggestedSampleRate, int bands, int fftSize) {}
    public record SpectrumSnapshot(String sessionId, int sampleRate, int bands, List<float[]> frames,
                                   long lastSampleAt, long droppedChunks) {}

    public static final class SessionLimitException extends RuntimeException {
        public SessionLimitException(String message) { super(message); }
    }

    private static final class CaptureSession {
        final String id;
        final long createdAt;
        final ArrayBlockingQueue<float[]> queue = new ArrayBlockingQueue<>(QUEUE_CHUNKS);
        final ConcurrentLinkedDeque<float[]> spectra = new ConcurrentLinkedDeque<>();
        final AtomicBoolean processing = new AtomicBoolean(false);
        final AtomicBoolean closed = new AtomicBoolean(false);
        final AtomicLong droppedChunks = new AtomicLong();
        final float[] frameBuffer = new float[FFT_SIZE];
        volatile int sampleRate = 48_000;
        volatile long lastSampleAt;
        int sampleCount;

        CaptureSession(String id, long createdAt) {
            this.id = id;
            this.createdAt = createdAt;
        }
    }

    private static final class DaemonThreadFactory implements ThreadFactory {
        private int sequence;
        @Override
        public synchronized Thread newThread(Runnable task) {
            Thread thread = new Thread(task, "reso-pcm-spectrum-" + (++sequence));
            thread.setDaemon(true);
            return thread;
        }
    }
}
