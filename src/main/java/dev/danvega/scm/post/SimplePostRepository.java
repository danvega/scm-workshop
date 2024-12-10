package dev.danvega.scm.post;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SimplePostRepository {

    private static final Logger log = LoggerFactory.getLogger(SimplePostRepository.class);
    private final JdbcClient jdbcClient;

    public SimplePostRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<SimplePost> findAll() {
        return jdbcClient.sql("SELECT id,content,created_at FROM posts")
                .query(SimplePost.class)
                .list();
    }

    public List<Post> findAllPosts() {
        return jdbcClient.sql("select id, content, created_at, attachments, commments, reactions, author, tags, draft, visibility from posts")
                .query((rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        null, // attachments
                        null, // comments
                        null, // reactions
                        null, // author
                        null, // tags
                        rs.getBoolean("draft"),
                        Visibility.valueOf(rs.getString("visibility"))
                ))
                .list();
    }
}
