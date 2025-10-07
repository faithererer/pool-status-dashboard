package com.zjc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjc.common.Result;
import com.zjc.service.impl.AuthServiceImpl;
import com.zjc.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 认证拦截器
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    
    private final JwtUtil jwtUtil;
    private final AuthServiceImpl authService;
    private final ObjectMapper objectMapper;
    
    /**
     * 需要认证的路径模式
     * 只有系统管理类型的接口需要鉴权，监控接口允许游客访问
     */
    private static final List<String> PROTECTED_PATTERNS = Arrays.asList(
        "/api/pools", // 号池管理（POST, PUT, DELETE）
        "/api/virtual-pools", // 虚拟池管理（POST, PUT, DELETE）
        "/api/config", // 系统配置管理
        "/api/tasks" // 任务管理
    );
    
    /**
     * 不需要认证的路径模式
     */
    private static final List<String> PUBLIC_PATTERNS = Arrays.asList(
        "/api/auth/login", // 登录接口
        "/api/pools/public", // 公开号池列表
        "/api/pools/enabled", // 启用的号池列表
        "/api/pools/strategy/", // 按策略获取号池
        "/api/pool-status", // 号池状态查询（GET）
        "/api/virtual-pools/status", // 虚拟池状态查询
        "/api/datasource/types", // 数据源类型查询
        "/api/config/status", // 系统状态查询
        "/actuator" // 健康检查
    );
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        log.debug("请求拦截: {} {}", method, requestURI);
        
        // 检查是否为公开接口
        if (isPublicEndpoint(requestURI, method)) {
            log.debug("公开接口，允许访问: {} {}", method, requestURI);
            return true;
        }
        
        // 检查是否为需要保护的接口
        if (!isProtectedEndpoint(requestURI, method)) {
            log.debug("非保护接口，允许访问: {} {}", method, requestURI);
            return true;
        }
        
        // 需要认证的接口，检查令牌
        String authHeader = request.getHeader("Authorization");
        String token = jwtUtil.extractTokenFromHeader(authHeader);
        
        if (token == null) {
            log.warn("访问保护接口缺少认证令牌: {} {}", method, requestURI);
            sendUnauthorizedResponse(response, "缺少认证令牌");
            return false;
        }
        
        // 检查令牌是否在黑名单中
        if (authService.isTokenBlacklisted(token)) {
            log.warn("使用已失效的令牌访问: {} {}", method, requestURI);
            sendUnauthorizedResponse(response, "令牌已失效");
            return false;
        }
        
        // 验证令牌
        if (!jwtUtil.validateToken(token)) {
            log.warn("使用无效令牌访问: {} {}", method, requestURI);
            sendUnauthorizedResponse(response, "令牌无效");
            return false;
        }
        
        // 检查令牌是否过期
        if (jwtUtil.isTokenExpired(token)) {
            log.warn("使用过期令牌访问: {} {}", method, requestURI);
            sendUnauthorizedResponse(response, "令牌已过期");
            return false;
        }
        
        // 将用户信息添加到请求属性中
        String username = jwtUtil.getUsernameFromToken(token);
        request.setAttribute("username", username);
        
        log.debug("认证成功，允许访问: {} {} (用户: {})", method, requestURI, username);
        return true;
    }
    
    /**
     * 检查是否为公开接口
     */
    private boolean isPublicEndpoint(String requestURI, String method) {
        return PUBLIC_PATTERNS.stream().anyMatch(pattern -> 
            requestURI.startsWith(pattern) || requestURI.contains(pattern)
        );
    }
    
    /**
     * 检查是否为需要保护的接口
     */
    private boolean isProtectedEndpoint(String requestURI, String method) {
        // 对于号池和虚拟池接口，只有修改操作需要认证
        if (requestURI.startsWith("/api/pools") || requestURI.startsWith("/api/virtual-pools")) {
            return "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method);
        }
        
        // 配置管理接口的PUT操作需要认证
        if (requestURI.startsWith("/api/config")) {
            return "PUT".equals(method);
        }
        
        // 任务管理接口需要认证
        if (requestURI.startsWith("/api/tasks")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 发送未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        Result<Void> result = Result.error(401, message);
        String jsonResponse = objectMapper.writeValueAsString(result);
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}