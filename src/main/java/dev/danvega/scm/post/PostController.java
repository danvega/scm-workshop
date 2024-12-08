package dev.danvega.scm.post;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository repository;

    public PostController(PostRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    List<Post> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    Post findById(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    @GetMapping("/author/{authorId}")
    List<Post> findByAuthorId(@PathVariable Long authorId) {
        return repository.findByAuthorId(authorId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Post create(@RequestBody Post post) {
        return repository.save(post);
    }

    @PutMapping("/{id}")
    Post update(@PathVariable Long id, @RequestBody Post post) {
        return repository.findById(id)
                .map(existingPost -> repository.save(post))
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

}
