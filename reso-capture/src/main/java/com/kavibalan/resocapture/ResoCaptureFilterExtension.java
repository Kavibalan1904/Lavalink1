package com.kavibalan.resocapture;

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import dev.arbjerg.lavalink.api.AudioFilterExtension;
import kotlinx.serialization.json.JsonElement;
import kotlinx.serialization.json.JsonObject;
import kotlinx.serialization.json.JsonPrimitive;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ResoCaptureFilterExtension implements AudioFilterExtension {
    public static final String FILTER_NAME = "reso-capture";
    private static final Logger LOG = Logger.getLogger(ResoCaptureFilterExtension.class.getName());
    private final ResoCaptureService captureService;

    public ResoCaptureFilterExtension(ResoCaptureService captureService) {
        this.captureService = captureService;
    }

    @Override
    public String getName() {
        return FILTER_NAME;
    }

    @Override
    public boolean isEnabled(JsonElement data) {
        if (!(data instanceof JsonObject)) return false;
        JsonElement enabled = ((JsonObject) data).get("enabled");
        return enabled instanceof JsonPrimitive && ((JsonPrimitive) enabled).getBoolean();
    }

    @Override
    public FloatPcmAudioFilter build(JsonElement data, AudioDataFormat format, FloatPcmAudioFilter output) {
        if (output == null || !(data instanceof JsonObject)) return output;
        JsonObject object = (JsonObject) data;
        JsonElement idElement = object.get("sessionId");
        if (!(idElement instanceof JsonPrimitive)) return output;
        String sessionId = ((JsonPrimitive) idElement).getContent();
        if (sessionId == null || sessionId.isBlank()) return output;
        int sampleRate = format == null ? 48_000 : format.sampleRate;

        return new FloatPcmAudioFilter() {
            @Override
            public void process(float[][] input, int offset, int length) throws InterruptedException {
                try {
                    captureService.offer(sessionId, input, offset, length, sampleRate);
                } catch (RuntimeException error) {
                    LOG.log(Level.FINE, "PCM capture skipped; playback path remains unchanged", error);
                }
                output.process(input, offset, length);
            }

            @Override
            public void seekPerformed(long requestedTime, long providedTime) {
                output.seekPerformed(requestedTime, providedTime);
            }

            @Override
            public void flush() throws InterruptedException {
                output.flush();
            }

            @Override
            public void close() {
                output.close();
            }
        };
    }
}
