package com.zjc.dto;

import lombok.Data;

/**
 * 登录响应DTO
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Data
public class LoginResponse {
    
    /**
     * JWT令牌
     */
    private String token;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 令牌过期时间
     */
    private String expiresAt;
    
    public LoginResponse(String token, String username, String expiresAt) {
        this.token = token;
        this.username = username;
        this.expiresAt = expiresAt;
    }
}