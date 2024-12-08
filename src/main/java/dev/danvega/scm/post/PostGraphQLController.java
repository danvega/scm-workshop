package dev.danvega.scm.post;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class PostGraphQLController {

    private final PostRepository postRepository;

    public PostGraphQLController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @QueryMapping
    List<Post> posts() {
        return postRepository.findAll();
    }

    @QueryMapping
    public Post post(@Argument Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    @QueryMapping
    List<Post> postsByAuthor(@Argument Long authorId) {
        return postRepository.findByAuthorId(authorId);
    }

    @MutationMapping
    Post createPost(@Argument CreatePostInput input) {
        Post post = new Post(
                null,
                input.content(),
                null, // createdAt will be set by DB
                List.of(), // attachments
                List.of(), // comments
                List.of(), // reactions
                input.author(),
                List.of(), // tags
                input.draft(),
                input.visibility()
        );
        return postRepository.save(post);
    }

    @MutationMapping
    Post updatePost(@Argument Long id, @Argument UpdatePostInput input) {
        return postRepository.findById(id)
                .map(existingPost -> {
                    Post updatedPost = new Post(
                            existingPost.id(),
                            input.content() != null ? input.content() : existingPost.content(),
                            existingPost.createdAt(),
                            existingPost.attachments(),
                            existingPost.comments(),
                            existingPost.reactions(),
                            existingPost.author(),
                            existingPost.tags(),
                            input.draft() != null ? input.draft() : existingPost.draft(),
                            input.visibility() != null ? input.visibility() : existingPost.visibility()
                    );
                    return postRepository.save(updatedPost);
                })
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    @MutationMapping
    boolean deletePost(@Argument Long id) {
        postRepository.deleteById(id);
        return true;
    }

    @SchemaMapping(typeName = "Post")
    Integer commentCount(Post post) {
        return post.comments().size();
    }

    @SchemaMapping(typeName = "Post")
    Integer reactionCount(Post post) {
        return post.reactions().size();
    }
}



