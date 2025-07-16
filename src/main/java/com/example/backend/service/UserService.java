package com.example.backend.service;

import com.example.backend.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User register(User user);
    User findByUsername(String username);
}