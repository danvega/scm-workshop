-- Sample Data
INSERT INTO users (username, email, hashed_password, role)
VALUES ('danvega', 'dan@danvega.dev', '$2a$12$encrypted', 'ADMIN'),
       ('javadev', 'java@example.com', '$2a$12$encrypted', 'USER'),
       ('springfan', 'spring@example.com', '$2a$12$encrypted', 'USER');

INSERT INTO profiles (user_id, display_name, bio)
VALUES (1, 'Dan Vega', 'Spring Developer Advocate & Content Creator'),
       (2, 'Java Developer', 'Passionate about JVM technologies'),
       (3, 'Spring Fan', 'Building awesome apps with Spring Boot');

INSERT INTO tags (name)
VALUES ('java'),
       ('spring'),
       ('ai'),
       ('springboot'),
       ('programming');

INSERT INTO posts (content, author_id, visibility)
VALUES ('Just released Spring Boot 3.2! The virtual threads support is amazing. Check out the new features! #spring #java',
        1, 'PUBLIC'),
       ('Working on migrating our application to Java 21. Pattern matching in switch statements is a game changer! #java',
        2, 'PUBLIC'),
       ('Building an AI-powered code review bot using Spring AI. The possibilities are endless! #ai #spring', 3,
        'PUBLIC'),
       ('Spring Security 6.2 brings excellent improvements to OAuth2 resource server support. Time to upgrade! #spring #security',
        1, 'PUBLIC'),
       ('Exploring Project Loom and virtual threads in Spring Boot 3.2. The performance improvements are incredible! #java #spring',
        2, 'PUBLIC');

INSERT INTO post_tags (post_id, tag_id)
SELECT p.id, t.id
FROM posts p
         CROSS JOIN tags t
WHERE t.name IN ('java', 'spring', 'ai')
  AND p.id <= 5;

INSERT INTO comments (content, post_id, author_id)
VALUES ('Great overview! Looking forward to trying virtual threads.', 1, 2),
       ('Pattern matching has simplified my code so much!', 2, 3),
       ('Are you using the ChatGPT API for this?', 3, 1),
       ('Security improvements are always welcome!', 4, 2),
       ('The throughput increase is remarkable!', 5, 3);

INSERT INTO reactions (type, user_id, post_id)
SELECT 'LIKE', u.id, p.id
FROM users u
         CROSS JOIN posts p
WHERE p.id <= 5;