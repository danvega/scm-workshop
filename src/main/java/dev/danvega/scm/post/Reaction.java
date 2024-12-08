package dev.danvega.scm.post;

import dev.danvega.scm.user.User;

import java.time.LocalDateTime;

record Reaction(
        Long id,
        ReactionType type,
        User user,
        LocalDateTime createdAt
) {}
