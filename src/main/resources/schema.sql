DROP TABLE IF EXISTS reactions CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS post_tags CASCADE;
DROP TABLE IF EXISTS post_attachments CASCADE;
DROP TABLE IF EXISTS posts CASCADE;
DROP TABLE IF EXISTS tags CASCADE;
DROP TABLE IF EXISTS media CASCADE;
DROP TABLE IF EXISTS user_relationships CASCADE;
DROP TABLE IF EXISTS profiles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TYPE IF EXISTS reaction_type CASCADE;
DROP TYPE IF EXISTS post_visibility CASCADE;
DROP TYPE IF EXISTS media_type CASCADE;
DROP TYPE IF EXISTS user_role CASCADE;

CREATE TYPE user_role AS ENUM ('USER', 'MODERATOR', 'ADMIN');
CREATE TYPE media_type AS ENUM ('IMAGE', 'VIDEO', 'DOCUMENT');
CREATE TYPE post_visibility AS ENUM ('PUBLIC', 'PRIVATE', 'FOLLOWERS_ONLY');
CREATE TYPE reaction_type AS ENUM ('LIKE', 'LOVE', 'LAUGH', 'SAD', 'ANGRY');

CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       hashed_password VARCHAR(255) NOT NULL,
                       role user_role NOT NULL DEFAULT 'USER',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE profiles (
                          id BIGSERIAL PRIMARY KEY,
                          user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                          display_name VARCHAR(100),
                          bio TEXT,
                          joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_relationships (
                                    follower_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                                    following_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    PRIMARY KEY (follower_id, following_id)
);

CREATE TABLE media (
                       id BIGSERIAL PRIMARY KEY,
                       url VARCHAR(255) NOT NULL,
                       media_type media_type NOT NULL,
                       size BIGINT NOT NULL,
                       content_type VARCHAR(100) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tags (
                      id BIGSERIAL PRIMARY KEY,
                      name VARCHAR(50) UNIQUE NOT NULL,
                      usage_count INTEGER DEFAULT 0,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE posts (
                       id BIGSERIAL PRIMARY KEY,
                       content TEXT NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       author_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                       draft BOOLEAN DEFAULT false,
                       visibility post_visibility DEFAULT 'PUBLIC'
);

CREATE TABLE post_attachments (
                                  post_id BIGINT REFERENCES posts(id) ON DELETE CASCADE,
                                  media_id BIGINT REFERENCES media(id) ON DELETE CASCADE,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  PRIMARY KEY (post_id, media_id)
);

CREATE TABLE post_tags (
                           post_id BIGINT REFERENCES posts(id) ON DELETE CASCADE,
                           tag_id BIGINT REFERENCES tags(id) ON DELETE CASCADE,
                           PRIMARY KEY (post_id, tag_id)
);

CREATE TABLE comments (
                          id BIGSERIAL PRIMARY KEY,
                          content TEXT NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          post_id BIGINT REFERENCES posts(id) ON DELETE CASCADE,
                          author_id BIGINT REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE reactions (
                           id BIGSERIAL PRIMARY KEY,
                           type reaction_type NOT NULL,
                           user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                           post_id BIGINT REFERENCES posts(id) ON DELETE CASCADE,
                           comment_id BIGINT REFERENCES comments(id) ON DELETE CASCADE,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           CHECK (
                               (post_id IS NOT NULL AND comment_id IS NULL) OR
                               (post_id IS NULL AND comment_id IS NOT NULL)
                               )
);

CREATE INDEX IF NOT EXISTS idx_profiles_user_id ON profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_posts_author_id ON posts(author_id);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_comments_post_id ON comments(post_id);
CREATE INDEX IF NOT EXISTS idx_comments_author_id ON comments(author_id);
CREATE INDEX IF NOT EXISTS idx_reactions_post_id ON reactions(post_id);
CREATE INDEX IF NOT EXISTS idx_reactions_comment_id ON reactions(comment_id);
CREATE INDEX IF NOT EXISTS idx_tags_name ON tags(name);