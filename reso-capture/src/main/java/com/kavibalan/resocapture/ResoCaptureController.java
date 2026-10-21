package com.kavibalan.resocapture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@RestController
@RequestMapping("/v1/reso-capture/sessions")
public class ResoCaptureController {
    private final ResoCaptureService service;
    private final String apiKey;

    public ResoCaptureController(
            ResoCaptureService service,
            @Value("${RESO_CAPTURE_API_KEY:}") String apiKey) {
        this.service = service;
        this.apiKey = apiKey == null ? "" : apiKey;
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestHeader(value = "X-Reso-Capture-Key", required = false) String suppliedKey) {
        if (!authorized(suppliedKey)) return unauthorized();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.createSession());
        } catch (ResoCaptureService.SessionLimitException limit) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", limit.getMessage()));
        }
    }

    @GetMapping("/{sessionId}/spectrum")
    public ResponseEntity<?> spectrum(
            @PathVariable String sessionId,
            @RequestHeader(value = "X-Reso-Capture-Key", required = false) String suppliedKey) {
        if (!authorized(suppliedKey)) return unauthorized();
        ResoCaptureService.SpectrumSnapshot snapshot = service.getSpectrum(sessionId);
        if (snapshot == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Capture session not found or expired"));
        }
        return ResponseEntity.ok(snapshot);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> delete(
            @PathVariable String sessionId,
            @RequestHeader(value = "X-Reso-Capture-Key", required = false) String suppliedKey) {
        if (!authorized(suppliedKey)) return unauthorized();
        return service.deleteSession(sessionId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private boolean authorized(String suppliedKey) {
        if (apiKey.isBlank() || suppliedKey == null) return false;
        return MessageDigest.isEqual(
                apiKey.getBytes(StandardCharsets.UTF_8),
                suppliedKey.getBytes(StandardCharsets.UTF_8));
    }

    private ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid or missing X-Reso-Capture-Key"));
    }
}
