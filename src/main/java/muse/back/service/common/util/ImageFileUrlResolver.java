package muse.back.service.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class ImageFileUrlResolver {
    private final String imageBaseUrl;

    public ImageFileUrlResolver(@Value("${integration.image.base-url:http://localhost:8081}") String imageBaseUrl) {
        this.imageBaseUrl = normalizeBaseUrl(imageBaseUrl);
    }

    public String resolveImageUrl(String storedValue) {
        String normalized = normalizeStoredValue(storedValue);
        if (normalized == null) {
            return null;
        }
        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            return normalized;
        }
        if (normalized.startsWith("/images/")) {
            return imageBaseUrl + normalized;
        }
        if (normalized.startsWith("images/")) {
            return imageBaseUrl + "/" + normalized;
        }
        return imageBaseUrl + "/images/" + normalized;
    }

    private String normalizeBaseUrl(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String normalizeStoredValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            try {
                return URI.create(trimmed).toString();
            } catch (IllegalArgumentException ex) {
                return trimmed;
            }
        }
        return trimmed;
    }
}
