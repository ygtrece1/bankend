package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // 注册接口
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // 检查用户名是否已存在
        if (userService.findByUsername(user.getUsername()) != null) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }

        // 调用服务层注册用户
        userService.register(user);
        return ResponseEntity.ok("注册成功");
    }

    // 登录接口
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        return ResponseEntity.ok("登录成功");
    }
}