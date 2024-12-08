package dev.danvega.scm.post;

import dev.danvega.scm.user.User;

record CreatePostInput(
        String content,
        User author,
        Boolean draft,
        Visibility visibility
) {}

