package dev.danvega.scm.media;

public record Media(
        Long id,
        String url,
        MediaType type,
        Long size,
        String contentType
) {}