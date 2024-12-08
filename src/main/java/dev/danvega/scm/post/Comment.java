package dev.danvega.scm.post;

import dev.danvega.scm.user.User;

import java.time.LocalDateTime;
import java.util.List;

record Comment(
        Long id,
        String content,
        LocalDateTime createdAt,
        User author,
        List<Reaction> reactions
) {}
