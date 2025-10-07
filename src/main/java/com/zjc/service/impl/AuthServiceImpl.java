package com.zjc.service.impl;

import com.zjc.dto.LoginRequest;
import com.zjc.dto.LoginResponse;
import com.zjc.dto.TokenInfo;
import com.zjc.service.AuthService;
import com.zjc.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证服务实现类
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final JwtUtil jwtUtil;
    
    /**
     * 默认管理员用户名
     */
    @Value("${app.auth.admin.username:admin}")
    private String adminUsername;
    
    /**
     * 默认管理员密码（BCrypt加密后的值）
     * 默认密码: password123
     */
    @Value("${app.auth.admin.password:$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa}")
    private String adminPassword;
    
    /**
     * 密码编码器
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 黑名单令牌集合（用于登出）
     */
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("用户登录尝试: {}", loginRequest.getUsername());
        
        // 验证用户名和密码
        if (!adminUsername.equals(loginRequest.getUsername())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), adminPassword)) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 生成JWT令牌
        String token = jwtUtil.generateToken(loginRequest.getUsername());
        String expiresAt = jwtUtil.getExpirationFromToken(token);
        
        log.info("用户登录成功: {}", loginRequest.getUsername());
        
        return new LoginResponse(token, loginRequest.getUsername(), expiresAt);
    }
    
    @Override
    public TokenInfo verifyToken(String token) {
        // 检查令牌是否在黑名单中
        if (blacklistedTokens.contains(token)) {
            throw new RuntimeException("令牌已失效");
        }
        
        // 验证令牌
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("令牌无效");
        }
        
        // 检查令牌是否过期
        if (jwtUtil.isTokenExpired(token)) {
            throw new RuntimeException("令牌已过期");
        }
        
        String username = jwtUtil.getUsernameFromToken(token);
        String expiresAt = jwtUtil.getExpirationFromToken(token);
        
        return new TokenInfo(username, expiresAt);
    }
    
    @Override
    public void logout(String token) {
        // 将令牌加入黑名单
        blacklistedTokens.add(token);
        log.info("用户登出，令牌已加入黑名单");
        
        // 清理过期的黑名单令牌（简单实现）
        cleanupExpiredTokens();
    }
    
    /**
     * 清理过期的黑名单令牌
     */
    private void cleanupExpiredTokens() {
        blacklistedTokens.removeIf(token -> {
            try {
                return jwtUtil.isTokenExpired(token);
            } catch (Exception e) {
                // 如果令牌解析失败，也认为是过期的
                return true;
            }
        });
    }
    
    /**
     * 检查令牌是否在黑名单中
     * 
     * @param token JWT令牌
     * @return 是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}