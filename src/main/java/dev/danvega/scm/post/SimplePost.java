package dev.danvega.scm.post;

import java.time.LocalDateTime;

public record SimplePost(Long id,
                         String content,
                         LocalDateTime createdAt) {
}
