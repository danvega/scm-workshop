package dev.danvega.scm.post;

import dev.danvega.scm.user.User;
import dev.danvega.scm.media.Media;
import dev.danvega.scm.tag.Tag;
import java.time.LocalDateTime;
import java.util.List;

record Post(
        Long id,
        String content,
        LocalDateTime createdAt,
        List<Media> attachments,
        List<Comment> comments,
        List<Reaction> reactions,
        User author,
        List<Tag> tags,
        boolean draft,
        Visibility visibility
) {}
