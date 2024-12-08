package dev.danvega.scm.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.danvega.scm.user.Role;
import dev.danvega.scm.user.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public PostRepository postRepository() {
            return mock(PostRepository.class);
        }
    }

    @Test
    void findAll_ShouldReturnPosts() throws Exception {
        Post post1 = new Post(1L, "Content 1", LocalDateTime.now(), List.of(), List.of(), List.of(), new User(1L, "Author 1", "author1@example.com", "hashedPassword1", null, List.of(), List.of(), Role.USER), List.of(), false, Visibility.PUBLIC);
        Post post2 = new Post(2L, "Content 2", LocalDateTime.now(), List.of(), List.of(), List.of(), new User(2L, "Author 2", "author2@example.com", "hashedPassword2", null, List.of(), List.of(), Role.USER), List.of(), false, Visibility.PUBLIC);

        given(repository.findAll()).willReturn(Arrays.asList(post1, post2));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].content").value("Content 1"))
                .andExpect(jsonPath("$[1].content").value("Content 2"));
    }

    @Test
    void findById_ShouldReturnPost_WhenPostExists() throws Exception {
        Post post = new Post(1L, "Content 1", LocalDateTime.now(), List.of(), List.of(), List.of(), new User(1L, "Author 1", "author1@example.com", "hashedPassword1", null, List.of(), List.of(), Role.USER), List.of(), false, Visibility.PUBLIC);

        given(repository.findById(1L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").value("Content 1"));
    }

    @Test
    public void findById_ShouldThrowNotFoundException_WhenPostDoesNotExist() {
        given(repository.findById(99L)).willThrow(new PostNotFoundException(99L));

        Assertions.assertThrows(PostNotFoundException.class, () -> {
            repository.findById(99L);
        });
    }

    @Test
    void create_ShouldCreatePost() throws Exception {
        Post post = new Post(null, "New Content", LocalDateTime.now(), List.of(), List.of(), List.of(), new User(1L, "Author 1", "author1@example.com", "hashedPassword1", null, List.of(), List.of(), Role.USER), List.of(), false, Visibility.PUBLIC);
        Post savedPost = new Post(1L, "New Content", LocalDateTime.now(), List.of(), List.of(), List.of(), new User(1L, "Author 1", "author1@example.com", "hashedPassword1", null, List.of(), List.of(), Role.USER), List.of(), false, Visibility.PUBLIC);

        given(repository.save(post)).willReturn(savedPost);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("New Content"));
    }

    @Test
    void update_ShouldUpdatePost_WhenPostExists() throws Exception {
        Post post = new Post(1L, "Updated Content", LocalDateTime.now(), List.of(), List.of(), List.of(), new User(1L, "Author 1", "author1@example.com", "hashedPassword1", null, List.of(), List.of(), Role.USER), List.of(), false, Visibility.PUBLIC);

        given(repository.findById(1L)).willReturn(Optional.of(post));
        given(repository.save(post)).willReturn(post);

        mockMvc.perform(put("/api/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated Content"));
    }

    @Test
    void delete_ShouldDeletePost_WhenPostExists() throws Exception {
        doNothing().when(repository).deleteById(1L);

        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isNoContent());
    }

}