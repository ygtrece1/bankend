package com.example.backend.security;

import com.example.backend.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 1. 获取当前请求的路径
        String requestUri = request.getRequestURI();

        // 2. 定义需要放行的路径（静态资源、认证接口等）
        boolean isPublicPath =
                // 放行根路径（自动跳转index.html）
                requestUri.equals("/") ||
                        // 放行HTML页面
                        requestUri.equals("/index.html") ||
                        requestUri.equals("/login.html") ||
                        requestUri.equals("/register.html") ||
                        // 放行静态资源目录（css、js、img）
                        requestUri.startsWith("/css/") ||
                        requestUri.startsWith("/js/") ||
                        requestUri.startsWith("/img/") ||
                        // 放行认证接口（登录、注册）
                        requestUri.startsWith("/api/auth/");

        // 3. 如果是放行路径，直接跳过JWT验证，放行请求
        if (isPublicPath) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4. 非放行路径，执行原有JWT验证逻辑
        try {
            String jwt = getJwtFromRequest(request);

            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                String username = jwtTokenProvider.getUsernameFromJwt(jwt);

                UserDetails userDetails = userService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException ex) {
            // 处理token过期
            logger.warn("JWT token has expired", ex);
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    // 原有获取JWT的方法
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}