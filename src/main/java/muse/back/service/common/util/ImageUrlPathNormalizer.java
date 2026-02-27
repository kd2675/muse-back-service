package muse.back.service.common.util;

import java.net.URI;

public final class ImageUrlPathNormalizer {

    private static final String IMAGE_PREFIX = "/images";

    private ImageUrlPathNormalizer() {
    }

    public static String toStoragePath(String rawImageUrl) {
        if (rawImageUrl == null) {
            return null;
        }

        String trimmed = rawImageUrl.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }

        String path = extractPath(trimmed);
        if (path == null || path.isBlank()) {
            return IMAGE_PREFIX;
        }
        if (IMAGE_PREFIX.equals(path) || path.startsWith(IMAGE_PREFIX + "/")) {
            return path;
        }
        if (path.startsWith("/")) {
            return IMAGE_PREFIX + path;
        }
        return IMAGE_PREFIX + "/" + path;
    }

    private static String extractPath(String value) {
        if (value.startsWith("http://") || value.startsWith("https://")) {
            try {
                return URI.create(value).getPath();
            } catch (IllegalArgumentException ex) {
                return value;
            }
        }
        if (value.startsWith("//")) {
            try {
                return URI.create("http:" + value).getPath();
            } catch (IllegalArgumentException ex) {
                return value;
            }
        }
        return value;
    }
}
