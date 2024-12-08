package dev.danvega.scm.tag;

public record Tag(
        Long id,
        String name,
        Integer usageCount
) {}
