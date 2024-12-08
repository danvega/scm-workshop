package dev.danvega.scm.user;

import java.util.List;

public record User(
        Long id,
        String username,
        String email,
        String hashedPassword,
        Profile profile,
        List<User> following,
        List<User> followers,
        Role role
) {}

