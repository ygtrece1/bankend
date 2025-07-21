package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.security.JwtTokenProvider;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 注册接口
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            if (userService.existsByUsername(user.getUsername())) {
                return ResponseEntity.badRequest().body("用户名已存在");
            }
            if (userService.existsByEmail(user.getEmail())) {
                return ResponseEntity.badRequest().body("邮箱已存在");
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole("USER");
            userService.register(user);
            return ResponseEntity.ok("注册成功");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("注册失败：" + e.getMessage());
        }
    }

    // 登录接口
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody User loginRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            // 打印登录请求参数
            System.out.println("登录请求：用户名=" + loginRequest.getUsername());

            // 执行认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // 打印认证成功信息
            System.out.println("认证成功：用户=" + authentication.getName());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenProvider.generateToken(authentication);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "登录成功");
            response.put("token", jwt);
            response.put("tokenType", "Bearer");
            return ResponseEntity.ok(response);

        } catch (UsernameNotFoundException e) {
            // 明确捕获用户不存在异常
            System.out.println("认证失败：用户不存在");
            return ResponseEntity.badRequest().body("用户名不存在");
        } catch (BadCredentialsException e) {
            // 明确捕获密码错误异常
            System.out.println("认证失败：密码错误");
            return ResponseEntity.badRequest().body("密码错误");
        } catch (ClassCastException e) {
            // 捕获类型转换异常
            System.out.println("类型转换异常：" + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("认证失败：类型转换错误");
        } catch (Exception e) {
            // 捕获其他异常
            System.out.println("未知异常：" + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("认证失败：" + e.getMessage());
        }
    }
}