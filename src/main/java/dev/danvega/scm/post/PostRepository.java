package dev.danvega.scm.post;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.danvega.scm.media.Media;
import dev.danvega.scm.tag.Tag;
import dev.danvega.scm.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
class PostRepository {

    private static final Logger log = LoggerFactory.getLogger(PostRepository.class);
    private final JdbcClient jdbcClient;
    private ObjectMapper objectMapper;
    private static final String POST_WITH_RELATIONS_SQL = """
            SELECT p.*,
                   jsonb_agg(DISTINCT jsonb_build_object(
                       'id', m.id,
                       'url', m.url,
                       'mediaType', m.media_type,
                       'size', m.size,
                       'contentType', m.content_type
                   )) FILTER (WHERE m.id IS NOT NULL) as attachments,
                   jsonb_agg(DISTINCT jsonb_build_object(
                       'id', c.id,
                       'content', c.content,
                       'createdAt', c.created_at
                   )) FILTER (WHERE c.id IS NOT NULL) as comments,
                   jsonb_agg(DISTINCT jsonb_build_object(
                       'id', r.id,
                       'type', r.type
                   )) FILTER (WHERE r.id IS NOT NULL) as reactions,
                   jsonb_build_object(
                       'id', u.id,
                       'username', u.username,
                       'email', u.email
                   ) as author,
                   jsonb_agg(DISTINCT jsonb_build_object(
                       'id', t.id,
                       'name', t.name
                   )) FILTER (WHERE t.id IS NOT NULL) as tags
            FROM posts p
            LEFT JOIN post_attachments pa ON p.id = pa.post_id
            LEFT JOIN media m ON pa.media_id = m.id
            LEFT JOIN comments c ON p.id = c.post_id
            LEFT JOIN reactions r ON p.id = r.post_id
            LEFT JOIN users u ON p.author_id = u.id
            LEFT JOIN post_tags pt ON p.id = pt.post_id
            LEFT JOIN tags t ON pt.tag_id = t.id
            """;

    public PostRepository(JdbcClient jdbcClient, ObjectMapper objectMapper) {
        this.jdbcClient = jdbcClient;
        this.objectMapper = objectMapper;
    }

    List<Post> findAll() {
        return jdbcClient.sql(POST_WITH_RELATIONS_SQL + " GROUP BY p.id, u.id ORDER BY p.created_at DESC")
                .query(postRowMapper)
                .list();
    }


    Optional<Post> findById(Long id) {
        return jdbcClient.sql(POST_WITH_RELATIONS_SQL + " WHERE p.id = :id GROUP BY p.id, u.id")
                .param("id", id)
                .query(Post.class)
                .optional();
    }

    List<Post> findByAuthorId(Long authorId) {
        return jdbcClient.sql(POST_WITH_RELATIONS_SQL + " WHERE p.author_id = :authorId GROUP BY p.id, u.id ORDER BY p.created_at DESC")
                .param("authorId", authorId)
                .query(Post.class)
                .list();
    }

    Post save(Post post) {
        if (post.id() == null) {
            Long id = jdbcClient.sql("INSERT INTO posts (content, author_id, draft, visibility) VALUES (:content, :authorId, :draft, :visibility::post_visibility) RETURNING id")
                    .param("content", post.content())
                    .param("authorId", post.author().id())
                    .param("draft", post.draft())
                    .param("visibility", post.visibility().toString())
                    .query(Long.class)
                    .single();

            saveRelations(id, post);
            return findById(id).orElseThrow();
        } else {
            jdbcClient.sql("UPDATE posts SET content = :content, draft = :draft, visibility = :visibility::post_visibility WHERE id = :id")
                    .param("content", post.content())
                    .param("draft", post.draft())
                    .param("visibility", post.visibility().toString())
                    .param("id", post.id())
                    .update();

            saveRelations(post.id(), post);
            return findById(post.id()).orElseThrow();
        }
    }

    void deleteById(Long id) {
        jdbcClient.sql("DELETE FROM posts WHERE id = :id")
                .param("id", id)
                .update();
    }

    List<PostWithComments> findAllWithCommentsOptimized() {
        String sql = """
            SELECT p.*, 
                   array_agg(jsonb_build_object(
                       'id', c.id,
                       'content', c.content,
                       'createdAt', c.created_at,
                       'authorId', c.author_id
                   )) FILTER (WHERE c.id IS NOT NULL) as comments
            FROM posts p
            LEFT JOIN comments c ON p.id = c.post_id
            GROUP BY p.id
            ORDER BY p.created_at DESC
            """;

        return jdbcClient.sql(sql)
                .query(PostWithComments.class)
                .list();
    }

    List<Post> search(String keyword) {
        return jdbcClient.sql(POST_WITH_RELATIONS_SQL +
                        " WHERE p.content ILIKE :keyword GROUP BY p.id, u.id ORDER BY p.created_at DESC")
                .param("keyword", "%" + keyword + "%")
                .query(Post.class)
                .list();
    }

    private RowMapper<Post> postRowMapper = (rs, rowNum) -> {
        try {
            return new Post(
                    rs.getLong("id"),
                    rs.getString("content"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    parseJsonArray(rs.getString("attachments"), Media.class),
                    parseJsonArray(rs.getString("comments"), Comment.class),
                    parseJsonArray(rs.getString("reactions"), Reaction.class),
                    objectMapper.readValue(rs.getString("author"), User.class),
                    parseJsonArray(rs.getString("tags"), Tag.class),
                    rs.getBoolean("draft"),
                    Visibility.valueOf(rs.getString("visibility"))
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON data", e);
        }
    };

    private <T> List<T> parseJsonArray(String json, Class<T> type) throws JsonProcessingException {
        if (json == null) return List.of();
        return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, type));
    }

    private void saveRelations(Long postId, Post post) {
        // Save attachments
        post.attachments().forEach(media ->
                jdbcClient.sql("INSERT INTO post_attachments (post_id, media_id) VALUES (:postId, :mediaId)")
                        .param("postId", postId)
                        .param("mediaId", media.id())
                        .update()
        );

        // Save tags
        post.tags().forEach(tag ->
                jdbcClient.sql("INSERT INTO post_tags (post_id, tag_id) VALUES (:postId, :tagId)")
                        .param("postId", postId)
                        .param("tagId", tag.id())
                        .update()
        );
    }
}
