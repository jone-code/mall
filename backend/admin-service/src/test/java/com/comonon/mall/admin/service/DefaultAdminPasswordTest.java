package com.comonon.mall.admin.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultAdminPasswordTest {

    @Test
    void defaultAdminHashMatchesDocumentedPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String hash = "$2a$12$ny2X4pDbFoKzTD35g.DeauVw2z5D1g6LNoaqhkSqh9138v5UNW7gG";
        assertTrue(encoder.matches("Admin@12345", hash), "mysql-init default hash must match Admin@12345");
    }
}
