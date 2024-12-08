package dev.danvega.scm.post;

record UpdatePostInput(
        String content,
        Boolean draft,
        Visibility visibility
) {}
