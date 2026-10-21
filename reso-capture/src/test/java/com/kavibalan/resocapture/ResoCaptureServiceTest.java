package com.kavibalan.resocapture;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResoCaptureServiceTest {
    @Test
    void capturesMeasuredSpectrumFromPcmAndKeepsBoundedHistory() throws Exception {
        ResoCaptureService service = new ResoCaptureService();
        String sessionId = service.createSession().sessionId();
        int sampleRate = 48_000;
        float[][] pcm = new float[2][8192];
        for (int i = 0; i < pcm[0].length; i++) {
            float sample = (float) (0.6 * Math.sin(2.0 * Math.PI * 440.0 * i / sampleRate));
            pcm[0][i] = sample;
            pcm[1][i] = sample;
        }

        service.offer(sessionId, pcm, 0, pcm[0].length, sampleRate);
        ResoCaptureService.SpectrumSnapshot snapshot = null;
        long deadline = System.currentTimeMillis() + 3_000L;
        while (System.currentTimeMillis() < deadline) {
            snapshot = service.getSpectrum(sessionId);
            if (snapshot != null && !snapshot.frames().isEmpty()) break;
            Thread.sleep(20L);
        }

        assertNotNull(snapshot);
        assertTrue(!snapshot.frames().isEmpty(), "worker should publish measured FFT frames");
        assertTrue(snapshot.frames().size() <= 36, "spectrum history must stay bounded");
        float[] latest = snapshot.frames().get(snapshot.frames().size() - 1);
        assertTrue(latest.length == 38, "spectrum should contain 38 frequency bands");
        assertTrue(max(latest) > 0.5f, "440 Hz input should produce a measurable spectrum");
        service.deleteSession(sessionId);
    }

    @Test
    void silenceProducesZeroSpectrum() throws Exception {
        ResoCaptureService service = new ResoCaptureService();
        String sessionId = service.createSession().sessionId();
        float[][] silence = new float[2][4096];
        service.offer(sessionId, silence, 0, silence[0].length, 48_000);

        ResoCaptureService.SpectrumSnapshot snapshot = null;
        long deadline = System.currentTimeMillis() + 3_000L;
        while (System.currentTimeMillis() < deadline) {
            snapshot = service.getSpectrum(sessionId);
            if (snapshot != null && !snapshot.frames().isEmpty()) break;
            Thread.sleep(20L);
        }

        assertNotNull(snapshot);
        assertTrue(!snapshot.frames().isEmpty(), "silence still produces a measured frame");
        for (float value : snapshot.frames().get(0)) {
            assertTrue(value == 0.0f, "silence should not create synthetic bar activity");
        }
        service.deleteSession(sessionId);
    }

    private static float max(float[] values) {
        float result = 0;
        for (float value : values) result = Math.max(result, value);
        return result;
    }
}
