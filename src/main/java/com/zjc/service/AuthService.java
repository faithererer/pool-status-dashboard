package com.zjc.service;

import com.zjc.dto.LoginRequest;
import com.zjc.dto.LoginResponse;
import com.zjc.dto.TokenInfo;

/**
 * 认证服务接口
 * 
 * @author zjc
 * @since 2024-01-06
 */
public interface AuthService {
    
    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest loginRequest);
    
    /**
     * 验证令牌
     * 
     * @param token JWT令牌
     * @return 令牌信息
     */
    TokenInfo verifyToken(String token);
    
    /**
     * 用户登出
     * 
     * @param token JWT令牌
     */
    void logout(String token);
}