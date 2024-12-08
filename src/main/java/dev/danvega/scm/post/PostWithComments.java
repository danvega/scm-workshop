package dev.danvega.scm.post;

import java.util.List;

record PostWithComments(Post post, List<Comment> comments) {}
