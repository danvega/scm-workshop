package dev.danvega.scm.post;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
class CommentService {

    // Mistake 6: Inconsistent transaction boundaries
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(Comment comment) {
        // Each comment gets own transaction
        // Could leave database in inconsistent state if some fail
    }

    public void validateComments(List<Comment> comments) {

    }
}
