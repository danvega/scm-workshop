package dev.danvega.scm.post;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
class PostRepositoryTemplate {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Post> postRowMapper;

    public PostRepositoryTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.postRowMapper = (rs, rowNum) -> new Post(
                rs.getLong("id"),
                rs.getString("content"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                Collections.emptyList(), // attachments
                Collections.emptyList(), // comments
                Collections.emptyList(), // reactions
                null, // author - would need separate query
                Collections.emptyList(), // tags
                rs.getBoolean("draft"),
                Visibility.valueOf(rs.getString("visibility"))
        );
    }

    List<Post> findAll() {
        String sql = "SELECT * FROM posts ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, postRowMapper);
    }

    Optional<Post> findById(Long id) {
        String sql = "SELECT * FROM posts WHERE id = ?";
        List<Post> results = jdbcTemplate.query(sql, postRowMapper, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    List<Post> findByAuthorId(Long authorId) {
        String sql = "SELECT * FROM posts WHERE author_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, postRowMapper, authorId);
    }

    Post save(Post post) {
        if (post.id() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            String sql = "INSERT INTO posts (content, author_id, draft, visibility) VALUES (?, ?, ?, ?::post_visibility)";

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, post.content());
                ps.setLong(2, post.author().id());
                ps.setBoolean(3, post.draft());
                ps.setString(4, post.visibility().toString());
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            return key != null ? findById(key.longValue()).orElseThrow() : null;
        } else {
            String sql = "UPDATE posts SET content = ?, draft = ?, visibility = ?::post_visibility WHERE id = ?";
            int updated = jdbcTemplate.update(sql,
                    post.content(),
                    post.draft(),
                    post.visibility().toString(),
                    post.id()
            );
            return updated > 0 ? findById(post.id()).orElseThrow() : null;
        }
    }

    void deleteById(Long id) {
        String sql = "DELETE FROM posts WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
