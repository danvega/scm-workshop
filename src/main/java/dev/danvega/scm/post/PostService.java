package dev.danvega.scm.post;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
class PostService {

    private final PostRepository postRepository;
    private final CommentService commentService;

    public PostService(PostRepository postRepository, CommentService commentService) {
        this.postRepository = postRepository;
        this.commentService = commentService;
    }

    // Mistake 1: @Transactional on private method won't work
    @Transactional
    protected void updatePostStats(Long postId) { // Won't create transaction
        // update post statistics
    }

    // Mistake 2: Calling @Transactional method from within same class
    public void createPostWithComments(Post post, List<Comment> comments) {
        savePost(post); // Transaction won't be created - internal method call
        comments.forEach(commentService::save);
    }

    @Transactional
    protected void savePost(Post post) {
        postRepository.save(post);
    }

    // Mistake 3: Not understanding propagation
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updatePost(Post post) {
        // Creates new transaction even when one exists
        // Could lead to partial updates if outer transaction fails
    }

    // Mistake 4: Exception handling breaking transaction
    @Transactional
    void processPost(Post post) {
        try {
            postRepository.save(post);
            commentService.validateComments(post.comments());
        } catch (Exception e) {
            e.printStackTrace(); // Swallowing exception prevents rollback
        }
    }

    // Mistake 5: Missing @Transactional on read operations
    List<Post> searchPosts(String keyword) { // No transaction for consistent reads
        return postRepository.search(keyword);
    }

    // Correct usage
    @Service
    class CorrectPostService {

        private final PostRepository postRepository;
        private final CommentService commentService;

        public CorrectPostService(PostRepository postRepository, CommentService commentService) {
            this.postRepository = postRepository;
            this.commentService = commentService;
        }

        @Transactional
        Post createPostWithComments(Post post, List<Comment> comments) {
            Post savedPost = postRepository.save(post);
            comments.forEach(comment -> commentService.save(comment));
            return savedPost;
        }

        @Transactional(readOnly = true)
        List<Post> searchPosts(String keyword) {
            return postRepository.search(keyword);
        }

        @Transactional(propagation = Propagation.REQUIRED)
        void updatePost(Post post) {
            postRepository.save(post);
        }
    }
}
