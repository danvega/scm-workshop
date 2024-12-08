package dev.danvega.scm.user;

import dev.danvega.scm.media.Media;

import java.time.LocalDateTime;

public record Profile(
        Long id,
        String displayName,
        String bio,
        Media avatar,
        LocalDateTime joinedAt
) {}
