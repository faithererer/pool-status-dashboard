package com.zjc.dto;

import lombok.Data;

/**
 * 令牌信息DTO
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Data
public class TokenInfo {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 令牌过期时间
     */
    private String expiresAt;
    
    public TokenInfo(String username, String expiresAt) {
        this.username = username;
        this.expiresAt = expiresAt;
    }
}